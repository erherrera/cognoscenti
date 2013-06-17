package org.socialbiz.cog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.Flags.Flag;
import javax.swing.text.html.HTMLEditorKit;

import org.socialbiz.cog.exception.NGException;

public class EmailListener extends TimerTask{

    private static EmailListener singletonListener = null;

    public static Exception threadLastCheckException = null;

    //expressed in milliseconds
    private final static long EVERY_TWENTY_SECONDS = 1000*20;

    private static Session session = null;
    private Store store = null;
    private static Folder popFolder = null;

    private File emailPropFile = null;
    private static Properties emailProperties = null;
    private AuthRequest ar;

    private boolean exceptionThrownAlready = false;

    public static boolean propertiesChanged = false;

    private EmailListener() throws Exception
    {
        this.ar = AuthDummy.serverBackgroundRequest();
        this.emailPropFile = ConfigFile.getFile("EmailNotification.properties");
        setEmailProperties();
    }

    /**
     * This is an initialization routine, and should only be called once, when the
     * server starts up.  There are some error checks to make sure that this is the case.
     */
     public static void initListener() throws Exception
     {
         //nothing else should create the EmailListener
         if (singletonListener!=null)
         {
             return;
         }
         singletonListener = new EmailListener();
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(singletonListener, 60000, EVERY_TWENTY_SECONDS);
     }

     public void run()
     {
         // make sure that this method doesn't throw any exception
         try
         {
             handlePOP3Folder();
         }
         catch(Exception e)
         {
             if(!exceptionThrownAlready){

                 Exception failure = new Exception("Failure in the EmailListener thread run method. Thread died.", e);
                 ar.logException("Failure in the EmailListener thread run method. Thread died.", failure);
                 threadLastCheckException = failure;
                 exceptionThrownAlready = true;
                 try {
                     SuperAdminLogFile.setEmailListenerPropertiesFlag(false);
                     SuperAdminLogFile.setEmailListenerProblem(failure);
                 } catch (Exception ex) {
                     ar.logException("Could not set EmailListenerPropertiesFlag in superadmin.logs file.", ex);
                 }
             }
         }
     }

    public Session getSession()throws Exception {
        try {
            if(emailProperties == null){
                throw new NGException("nugen.exception.email.config.file.not.found",
                        new Object[]{emailPropFile.getAbsolutePath()});
            }

            String user = emailProperties.getProperty("mail.pop3.user");
            String pwd = emailProperties.getProperty("mail.pop3.password");

            if (user == null || user.length() == 0 || pwd == null || pwd.length() == 0) {
                throw new NGException("nugen.exception.email.config.incorrect.invalid.user.or.password",
                        new Object[]{emailPropFile.getAbsolutePath()});
            }

            return Session.getInstance(emailProperties, new EmailAuthenticator(user, pwd));
        }catch (Exception e) {
            throw new NGException("nugen.exception.email.unable.to.create.session",null,e);
        }
    }

    public Store getPOP3Store()throws Exception {
        try {

            if(session == null || propertiesChanged ){
                session = getSession();
                propertiesChanged = false;
            }
            return session.getStore("pop3");

        }catch (MessagingException me) {
            throw new NGException("nugen.exception.email.unable.to.create.pop3store",null,me);
        }
    }

    public void  connectToMailServer()throws Exception {
        try {

            store = getPOP3Store();
            store.connect();

            popFolder = store.getFolder("INBOX");
            popFolder.open(Folder.READ_WRITE);

            exceptionThrownAlready = false;
            SuperAdminLogFile.setEmailListenerPropertiesFlag(true);

        }catch (MessagingException me) {
            throw new NGException("nugen.exception.email.unable.to.connect.to.mail.server",null,me);
        } finally {
            // close the store.
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException me) {
                    /* ignore this exception */
                }
            }
        }
    }

    private void handlePOP3Folder() throws Exception {
        try {

            connectToMailServer();

            Message[] messages = popFolder.getMessages();
            if (messages == null || messages.length == 0) {
                // nothing to process.
                return;
            }

            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID);
            popFolder.fetch(messages, fp);

            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                // most of the POP mail servers/providers does not support flags
                // for other then delete
                if (message.isSet(Flag.DELETED)) {
                    continue;
                }

                try {
                    processEmailMsg(message);
                } catch (Exception processingError) {
                    processingError.printStackTrace();
                    // handle the exception here. but don't throw rethrow.
                    throw new NGException("nugen.exception.email.listner.thread.process.fail",null, processingError);
                }
            }
        }catch (Exception e) {
            throw new NGException("nugen.exception.email.listner.thread.read.fail",null, e);
        }finally {
            try {
                if(popFolder != null){
                    popFolder.close(true);
                }
            } catch (Exception e) {
                /* ignore this exception */
            }
        }
    }

    private void processEmailMsg(Message message) throws Exception {
        try{

            List<String> toAddresses = new ArrayList<String>();
            Address[] recipients = message.getRecipients(Message.RecipientType.TO);
            for (Address address : recipients) {
                toAddresses.add(address.toString());
            }

            createNoteFromMail(message);

            handleEmailAttachments(message);

        }catch (Exception e) {
            //May be in this case we should also send reply to sender stating that 'note could not be created due to some reason'.
            throw new NGException("nugen.exception.could.not.process.email", new Object[]{message.getSubject()},e);
        }finally{
            message.setFlag(Flag.DELETED, true);
        }
    }

    public void createNoteFromMail(Message message) throws Exception {
        try{
            Address[] recipientAdrs= message.getAllRecipients();
            String pageId =  getPageId(recipientAdrs[0].toString());
            if(pageId == null){
                throw new NGException("nugen.exception.project.id.not.found",null);
            }
            NGPage ngp = NGPageIndex.getProjectByKeyOrFail(pageId);

            NoteRecord note = ngp.createNote();

            String subject = message.getSubject();
            note.setSubject( subject );

            note.setVisibility(1);
            note.setEditable(1);

            String bodyText = getEmailBody(message);
            HtmlToWikiConverter htmlToWikiConverter = new HtmlToWikiConverter();
            String wikiText = htmlToWikiConverter.htmlToWiki(ar.baseURL,bodyText);

            note.setData(wikiText);

            note.setEffectiveDate(System.currentTimeMillis());
            note.setLastEdited(System.currentTimeMillis());
            note.setSaveAsDraft("no");

            Address[] fromAdrs = message.getFrom();
            String fromAdd = getFromAddress(fromAdrs[0].toString());
            note.setLastEditedBy(fromAdd);

            ngp.save(fromAdrs[0].toString(), ar.nowTime,"");
            NGPageIndex.releaseLock(ngp);

        }catch(Exception e){
            throw new NGException("nugen.exception.cant.create.note.from.msg", new Object[]{message},e);
        }
    }

    private String getFromAddress(String fromAdr) {
        String fromAddress = null;
        if(fromAdr.contains("<") && fromAdr.contains(">")){
            fromAddress = fromAdr.substring(fromAdr.indexOf("<")+1, fromAdr.indexOf(">"));
        }
        return fromAddress;
    }

    private String getPageId(String recipientAdr) {
        String pageId = null;
        if(recipientAdr.contains("+") && recipientAdr.contains("@")){
            pageId = recipientAdr.substring(recipientAdr.indexOf("+")+1, recipientAdr.indexOf("@"));
        }
        return pageId;
    }

    private static String getEmailBody(Message msg) throws Exception {
        return getText(msg);
    }

    private static String getText(Part p) throws Exception {

        if (p.isMimeType("text/*")) {
            String str = (String) p.getContent();
            boolean textIsHtml = p.isMimeType("text/html");
            if (textIsHtml) {
                str = getPlainText(p.getInputStream()).toString();
            }
            return str;
        }else if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null) {
                        text = getText(bp);
                    }
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String str = getText(bp);
                    if (str != null) {
                        return str;
                    }
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String str = getText(mp.getBodyPart(i));
                if (str != null) {
                    return str;
                }
            }
        }
        return "";
    }

    private static StringBuffer getPlainText(InputStream is) throws Exception {
        StringWriter out = new StringWriter();
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        char[] buf = new char[3333];
        int len = isr.read(buf);
        while (len>0) {
        	out.write(buf, 0, len);
        	len = isr.read(buf);
        }
        return out.getBuffer();
    }

    private void handleEmailAttachments(Message message) throws Exception {
        try{
            Address[] fromAdrs = message.getFrom();
            String fromAdd = getFromAddress(fromAdrs[0].toString());

            Multipart mp = (Multipart) message.getContent();
            String pageId =  getPageId(message.getAllRecipients()[0].toString());
            for (int i = 0, n = mp.getCount(); i < n; i++) {
                Part part = mp.getBodyPart(i);
                String disposition = part.getDisposition();
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT) || (disposition.equals(Part.INLINE))))) {

                    InputStream is = part.getInputStream();
                    String fileName = part.getFileName();

                    createDocumentRecord(pageId,is, fileName, fromAdd);
                }
            }
        }catch (Exception e) {
            throw new NGException("nugen.exception.cant.handle.email.att", new Object[]{message},e);
        }
    }

    private void createDocumentRecord(String pageId,InputStream is,String fileName,String fromAdd) throws Exception {
        try{
            if(pageId == null){
                throw new NGException("nugen.exception.project.id.not.found",null);
            }
            NGPage ngp = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.assertContainerFrozen(ngp);
            ar.setPageAccessLevels(ngp);
            String fileExtension = fileName.substring(fileName.indexOf("."));

            //AttachmentHelper.uploadNewDocument(ar, ngp, tempFile, fileName, 1, "Uploaded through received Email.");

            int visibility = 1;
            int version = 0;
            AttachmentRecord attachment = null;
            List<AttachmentRecord> att = ngp.getAllAttachments();
            for (AttachmentRecord attachmentRecord : att) {
                if(attachmentRecord.getDisplayName().equals(fileName)){
                    attachment = attachmentRecord;
                    version = attachment.getVersion();
                    visibility = attachment.getVisibility();
                    break;
                }
            }
            if(attachment == null){
                attachment =  ngp.createAttachment();
            }

            attachment.setDisplayName(fileName);
            attachment.setComment("Uploaded through received Email.");
            attachment.setModifiedBy(fromAdd);
            attachment.setModifiedDate(System.currentTimeMillis());
            attachment.setType("FILE");
            attachment.setVersion(version+1);
            attachment.setVisibility(visibility);

            saveUploadedFile(ar, attachment, is,fileExtension,fromAdd,ngp);

            ngp.save(fromAdd,ar.nowTime, "Uploaded through received Email.");

            NGPageIndex.releaseLock(ngp);
        }catch(Exception e){
            throw new NGException("nugen.exception.cant.carete.doc.from.email",null,e);
        }
    }

    public static String saveUploadedFile(AuthRequest ar, AttachmentRecord att,
           InputStream is,String fileExtension, String fromAdd, NGContainer ngp) throws Exception {

        // first make sure that the server is configured properly
        String attachFolder = ar.getSystemProperty("attachFolder");
        if (attachFolder == null) {
            throw new NGException("nugen.exception.system.configured.incorrectly", new Object[]{"attachFolder"});
        }
        File localRoot = new File(attachFolder);
        if (!localRoot.exists()) {
            throw new NGException("nugen.exception.incorrect.setting.for.attachfolder", new Object[]{attachFolder});
        }
        if (!localRoot.isDirectory()) {
            throw new NGException("nugen.exception.incorrectfile.setting.for.attachfolder", new Object[]{attachFolder});
        }


        File tempFile = File.createTempFile("~editaction",  fileExtension);
        tempFile.delete();
        saveToFileEML(is, tempFile);
        FileInputStream fis = new FileInputStream(tempFile);
        tempFile.delete();
        AttachmentVersion av = AttachmentVersionSimple.getNewSimpleVersion(ngp.getKey(), att.getId(),
                fileExtension, fis);

        //update the record
        att.setVersion(av.getNumber());
        att.setStorageFileName(av.getLocalFile().getName());
        att.setModifiedDate(System.currentTimeMillis());
        att.setModifiedBy(fromAdd);

        return fileExtension;
    }

    public static void saveToFileEML(InputStream is, File destinationFile)throws Exception {
        if (destinationFile == null) {
            throw new IllegalArgumentException("Can not save file.  Destination file must not be null.");
        }

        if (destinationFile.exists()) {
            throw new NGException("nugen.exception.file.already.exist", new Object[]{destinationFile});
        }
        File folder = destinationFile.getParentFile();
        if (!folder.exists()) {
            throw new NGException("nugen.exception.folder.not.exist", new Object[]{destinationFile});
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(destinationFile);
            int ret = 0;
            byte[] buff = new byte[2048];
            while( (ret = is.read(buff)) > 0 ){
                fileOut.write(buff, 0, ret);
            }
            fileOut.close();
        } catch (Exception e) {
            throw new NGException("nugen.exception.failed.to.save.file", new Object[]{destinationFile}, e);
        }
    }

    private Properties setEmailProperties() throws Exception {

        emailProperties = new Properties();
        if (!emailPropFile.exists()) {
            throw new NGException("nugen.exception.incorrect.sys.config", new Object[]{emailPropFile.getAbsolutePath()});
        }

        FileInputStream fis = new FileInputStream(emailPropFile);
        emailProperties.load(fis);

        emailProperties.setProperty("mail.pop3.connectionpooltimeout", "500");
        emailProperties.setProperty("mail.pop3.connectiontimeout", "500");
        emailProperties.setProperty("mail.pop3.timeout", "500");

        return emailProperties;
    }

    public static EmailListener getEmailListener(){
        return singletonListener;
    }

    public static Properties getEmailProperties(){
        return emailProperties;
    }
    public File getEmailPropertiesFile(){
        return emailPropFile;
    }

    public void reStart() {
        propertiesChanged = true;
        run();
    }

}

class EmailAuthenticator extends Authenticator {
    private PasswordAuthentication auth;

    public EmailAuthenticator(String username, String password) {
        auth = new PasswordAuthentication(username, password);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return auth;
    }
}

class Outliner extends HTMLEditorKit.ParserCallback {

    private Writer out;

    public Outliner(Writer out) {
        this.out = out;
    }

    public void handleText(char[] text, int position) {
        try {
            out.write(text);
            out.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            /* Ignore this Exception */
        }
    }
}
