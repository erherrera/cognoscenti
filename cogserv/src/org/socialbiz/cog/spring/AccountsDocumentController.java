package org.socialbiz.cog.spring;

import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.AttachmentRecord;
import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.HistoryRecord;
import org.socialbiz.cog.NGBook;
import org.socialbiz.cog.NGPageIndex;
import org.socialbiz.cog.ReminderMgr;
import org.socialbiz.cog.ReminderRecord;
import org.socialbiz.cog.spring.AttachmentHelper;
import org.socialbiz.cog.spring.ReminderEmailHelper;

@Controller
public class AccountsDocumentController extends BaseController {

    public static final String TAB_ID = "tabId";
    public static final String ACCOUNT_ID = "accountId";

    @Autowired
    public void setContext(ApplicationContext context) {
    }

    protected void initBinder(HttpServletRequest request,
            ServletRequestDataBinder binder) throws ServletException {

        binder.registerCustomEditor(byte[].class,new ByteArrayMultipartFileEditor());

    }

    @RequestMapping(value = "/{accountId}/$/upload.form", method = RequestMethod.POST)
    protected ModelAndView uploadFile(@PathVariable String accountId,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("fname") MultipartFile file) throws Exception {

        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.upload.doc",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            //Handling special case for Multipart request
            ar.req = request;

            ar.setPageAccessLevels(ngb);

            request.setCharacterEncoding("UTF-8");

            if (file.getSize() == 0) {
                throw new NGException("nugen.exceptionhandling.no.file.attached",null);
            }

            if(file.getSize() > 500000000){
                throw new NGException("nugen.exceptionhandling.file.size.exceeded", new Object[]{"500000000"});
            }

            String fileName = file.getOriginalFilename();

            if (fileName == null || fileName.length() == 0) {
                throw new NGException("nugen.exceptionhandling.filename.empty", null);
            }

            String visibility = ar.defParam("visibility", "*MEM*");
            String comment = ar.defParam("comment", "");
            String name = ar.defParam("name", null);

            AttachmentHelper.uploadNewDocument(ar, ngb, file, name, visibility, comment,ar.getBestUserId());

            modelAndView = new ModelAndView(new RedirectView("account_attachment.htm"));
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.upload.document", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/getEditAttachmentForm.form", method = RequestMethod.GET)
    protected ModelAndView getEditAttachmentForm(@PathVariable String accountId,
            @PathVariable String pageId, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.edit.attachment.",null);
            }
            NGPageIndex.getAccountByKeyOrFail(accountId);
            modelAndView = createModelView(accountId,request, response,ar,"edit_attachment","Account Documents");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.edit.attachment.page", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/emailReminder.form", method = RequestMethod.POST)
    protected ModelAndView submitEmailReminderForAttachment(
            @PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.can.not.send.email",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);

            String comment = ar.reqParam("comment");
            String pname = ar.defParam("pname", "");
            String assignee = ar.reqParam("assignee");
            String instruct = ar.reqParam("instruct");
            String subj = ar.reqParam("subj");
            String destFolder = ar.reqParam("destFolder");

            ReminderMgr rMgr = ngb.getReminderMgr();
            ReminderRecord rRec = rMgr.createReminder(ngb.getUniqueOnPage());
            rRec.setFileDesc(comment);
            rRec.setInstructions(instruct);
            rRec.setAssignee(assignee);
            rRec.setFileName(pname);
            rRec.setSubject(subj);
            rRec.setModifiedBy(ar.getBestUserId());
            rRec.setModifiedDate(ar.nowTime);
            rRec.setDestFolder(destFolder);

            ngb.saveFile(ar, "Modified attachments");

            modelAndView = new ModelAndView(new RedirectView("account_attachment.htm"));
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.create.email.reminder", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/sendemailReminder.htm", method = RequestMethod.GET)
    protected ModelAndView sendEmailReminderForAttachment(
            @PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.can.not.send.email.reminder",null);
            }
            NGPageIndex.getAccountByKeyOrFail(accountId);
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            request.setAttribute(TAB_ID, "Account Documents");
            request.setAttribute(ACCOUNT_ID, accountId);
            request.setAttribute( "headerType", "account" );
            modelAndView = new ModelAndView("reminder_email");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.send.email.reminder", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/resendemailReminder.htm", method = RequestMethod.POST)
    protected ModelAndView resendEmailReminderForAttachment(
            @PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.can.not.resend.email.reminder",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);

            ar.setPageAccessLevels(ngb);

            String reminderId = ar.reqParam("rid");
            String emailto = ar.defParam("emailto", null);

            ReminderEmailHelper.reminderEmail(ar, accountId, reminderId, emailto, ngb);

            modelAndView = createModelView(accountId, request, response,ar,"account_attachment","Account Documents");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.resend.email.reminder", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/editAttachment.form", method = RequestMethod.POST)
    protected ModelAndView editAttachment(@PathVariable String accountId,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "fname", required = false) MultipartFile file)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.can.not.edit.an.attachment",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            //Handling special case for Multipart request
            ar.req = request;

            String visibility = ar.defParam("visibility", "*MEM*");
            String action = ar.defParam("action", "");

            modelAndView = new ModelAndView(new RedirectView("account_attachment.htm"));// createModelView(accountId, request, response,ar,"accountDocumentPage","Account Documents");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.documents");
            // first, handle cancel operation. "Go back" is used for ie7
            if ("Cancel".equals(action) || "Go back".equals(action)) {
                return modelAndView;
            }else{
                AttachmentHelper.updateAttachmentFile(accountId, request, file, ar, action, visibility, ngb);
            }
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.edit.attachment", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/createLinkURL.form", method = RequestMethod.POST)
    protected ModelAndView createLinkURL(@PathVariable String accountId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.can.not.create.link.url",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);

            String destFolder = ar.reqParam("visibility");
            String comment = ar.reqParam("comment");
            String taskUrl = ar.reqParam("taskUrl");
            String ftype = ar.reqParam("ftype");

            AttachmentRecord attachment = null;
            attachment = ngb.createAttachment();
            attachment.setComment(comment);
            attachment.setModifiedBy(ar.getBestUserId());
            attachment.setModifiedDate(ar.nowTime);
            attachment.setType(ftype);

            AttachmentHelper.setDisplayName(ngb, attachment, taskUrl);

            if (destFolder.equals("PUB")) {
                attachment.setVisibility(1);
            } else {
                attachment.setVisibility(2);
            }
            attachment.setStorageFileName(taskUrl);
            HistoryRecord.createHistoryRecord(ngb, attachment.getId(), HistoryRecord.CONTEXT_TYPE_DOCUMENT,
                    ar.nowTime, HistoryRecord.EVENT_DOC_ADDED, ar, "Created Link URL");

            ngb.saveContent(ar, "Created Link URL");

            modelAndView = new ModelAndView(new RedirectView("account_attachment.htm"));
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.create.link.url", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    private ModelAndView createModelView(String accountId,
            HttpServletRequest request, HttpServletResponse response,AuthRequest ar,
            String view,  String tabId)
            throws Exception {

        request.setAttribute(TAB_ID, tabId);
        request.setAttribute(ACCOUNT_ID, accountId);
        request.setAttribute( "headerType", "account" );
        ModelAndView modelAndView = new ModelAndView(view);
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/uploadDocument.htm", method = RequestMethod.GET)
    public ModelAndView uploadDocumentForm(@PathVariable String accountId, HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.must.login.to.open.upload.doc.form",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            request.setAttribute( "subTabId","nugen.projecthome.subtab.upload.document" );
            request.setAttribute( "realRequestURL", ar.getRequestURL() );
            request.setAttribute( "tabId", "Account Documents" );
            request.setAttribute( "accountId", accountId );
            request.setAttribute( "title", ngb.getFullName() );
            request.setAttribute( "headerType", "account" );
            request.setAttribute( "pageTitle", ngb.getFullName() );

            modelAndView = new ModelAndView("upload_document_form_account" );
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.upload.document.page", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/linkURLToProject.htm", method = RequestMethod.GET)
    protected ModelAndView getLinkURLToProjectForm(@PathVariable String accountId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.link.url",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("subTabId", "nugen.projecthome.subtab.link.url.to.project");
            request.setAttribute( "realRequestURL", ar.getRequestURL() );
            request.setAttribute( "tabId", "Account Documents" );
            request.setAttribute( "accountId", accountId );
            request.setAttribute( "title", ngb.getFullName() );
            request.setAttribute( "pageTitle", ngb.getFullName() );

            modelAndView = createModelView(accountId, request, response,ar,"linkurlproject_form_account","Account Documents");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.link.project.page", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/emailReminder.htm", method = RequestMethod.GET)
    protected ModelAndView getEmailRemainderForm(@PathVariable String accountId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.open.email.reminder",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            request.setAttribute( "tabId", "Account Documents" );
            request.setAttribute("subTabId", "nugen.projecthome.subtab.emailreminder");
            request.setAttribute( "headerType", "account" );
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("title", ngb.getFullName());
            request.setAttribute( "pageTitle", ngb.getFullName() );
            modelAndView = createModelView(accountId, request, response,ar,"emailreminder_form_account","Account Documents");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.email.reminder.page", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/editDocumentForm.htm", method = RequestMethod.GET)
    protected ModelAndView getEditDocumentForm(@PathVariable String accountId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.modify.doc.setting",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("subTabId", "nugen.projectdocument.subtab.attachmentdetails");
            request.setAttribute("aid",ar.reqParam("aid"));
            request.setAttribute("title", ngb.getFullName());
            request.setAttribute( "pageTitle", ngb.getFullName() );
            modelAndView = createModelView(accountId, request, response,ar,"edit_document_form_account","Account Documents");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.edit.document.page", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/fileVersion.htm", method = RequestMethod.GET)
    protected ModelAndView getFileVersion(@PathVariable String accountId, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.file.version",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("subTabId", "nugen.projectdocument.subtab.fileversions");
            request.setAttribute("aid",ar.reqParam("aid"));
            request.setAttribute("title", ngb.getFullName());
            request.setAttribute( "pageTitle", ngb.getFullName() );
            modelAndView = createModelView(accountId, request, response,ar,"file_version_account","Account Documents");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.file.version.page", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/updateAttachment.form", method = RequestMethod.POST)
    protected ModelAndView updateAttachment(@PathVariable String accountId,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "fname", required = false) MultipartFile file)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.can.not.edit.an.attachment",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            //Handling special case for Multipart request
            ar.req = request;
            ar.setPageAccessLevels(ngb);

            String aid = ar.reqParam("aid");
            AttachmentRecord attachment = ngb.findAttachmentByIDOrFail(aid);

            String action = ar.reqParam("actionType");

            if(action.equalsIgnoreCase("renameDoc")){
                String accessName = ar.reqParam("accessName");
                String proposedName = AttachmentHelper.assureExtension(accessName, attachment.getDisplayName());
                attachment.setDisplayName(proposedName);

            }else if(action.equalsIgnoreCase("changePermission")){
                String visibility =  ar.reqParam("visibility");

                if (visibility.equals("PUB")) {
                    attachment.setVisibility(1);
                } else {
                    attachment.setVisibility(2);
                }

            }else if(action.equalsIgnoreCase("UploadRevisedDoc")){
                if(file.getSize() <= 0){
                    throw new NGException("nugen.exceptionhandling.file.length.zero",null);
                }
                String comment_panel = ar.reqParam("comment_panel");

                attachment.setComment(comment_panel);
                AttachmentHelper.setDisplayName(ngb, attachment,AttachmentHelper.assureExtension(attachment.getDisplayName(), file.getOriginalFilename()));
                AttachmentHelper.saveUploadedFile(ar, attachment, file);
            }
            ngb.saveFile(ar, "Modified attachments");
            modelAndView = new ModelAndView(new RedirectView("account_attachment.htm"));
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.edit.attachment", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

     public String loginCheckMessage(AuthRequest ar) throws Exception {
         String errorMsg = "";
        if (!ar.isLoggedIn()){
            String go = ar.getCompleteURL();
            errorMsg = "redirect:"+URLEncoder.encode(go,"UTF-8")+":"+URLEncoder.encode("Can not open form","UTF-8");
        }
        return errorMsg;
     }

    @RequestMapping(value = "/{accountId}/$/remindAttachment.htm", method = RequestMethod.GET)
    protected ModelAndView remindAttachment(@PathVariable String accountId,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception
    {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.open.remined.attachment",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            request.setAttribute("subTabId", "nugen.projecthome.subtab.upload.document");
            modelAndView = createModelView(accountId, request, response,ar,"remind_attachment","Account Documents");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.remind.attachment.page", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }


    @RequestMapping(value="/{accountId}/$/a/{docId}", method = RequestMethod.GET)
    public void loadDocument(@PathVariable String accountId, @PathVariable String docId,
        HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                sendRedirectToLogin(ar, "message.loginalert.download.document",null);
                return;
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);

            String path = request.getPathInfo();
            String attachmentName = path.substring(path.lastIndexOf("/")+1);

            ar.setPageAccessLevels(ngb);

            String version = ar.reqParam("version");
            if(version != null && version.length()!=0){
                AttachmentHelper.serveUpFileNewUI(ar, ngb, attachmentName , Integer.parseInt(version));
            }
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.download.document", new Object[]{docId, accountId}, ex);
        }
    }

    @RequestMapping(value = "/{accountId}/$/account_reminders.htm", method = RequestMethod.GET)
    public ModelAndView remindersTab(@PathVariable String accountId,
                                        HttpServletRequest request, HttpServletResponse response)
                                        throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.open.reminders",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            modelAndView=new ModelAndView("reminders_account");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.reminders");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("accountId", accountId);
            request.setAttribute("headerType", "account");
            request.setAttribute( "tabId", "Account Documents" );
            request.setAttribute( "title", ngb.getFullName() );
            request.setAttribute( "pageTitle", ngb.getFullName() );
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.remind.attachment.page", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    /**
    * @deprecated URL pattern - use docinfo###.htm instead
    * leaving in in case there are old URLs sitting around
    * deprecatd on MAy 15, 2011, remove about a year after that
    @RequestMapping(value = "/{accountId}/$/downloadAccountDocument.htm", method = RequestMethod.GET)
    protected ModelAndView getDownloadDocumentPage(@PathVariable String accountId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
        AuthRequest ar = getAuthRequest(request, response, "Must be logged in to open document download page.");
        String docId = ar.reqParam("aid");
        return accountDocInfo(accountId, docId, request, response);
    }
    */


    @RequestMapping(value = "/{accountId}/$/docinfo{docId}.htm", method = RequestMethod.GET)
    protected ModelAndView accountDocInfo(@PathVariable String accountId,
            @PathVariable String docId, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            //Note: this view displays for both logged in and not logged in people
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            request.setAttribute("aid", docId);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("accountId", accountId);
            request.setAttribute("headerType", "account");
            request.setAttribute("tabId", "Account Documents" );
            request.setAttribute("subTabId", "nugen.projectdocument.subtab.attachmentdetails");
            request.setAttribute( "pageTitle", NGPageIndex.getContainerByKey(accountId).getFullName() );

            modelAndView = new ModelAndView("download_account_document");

        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.download.document.page", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

     @RequestMapping(value = "/{accountId}/$/leaflet{lid}.htm", method = RequestMethod.GET)
     public ModelAndView displayZoomedLeaflet(@PathVariable String lid,@PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.must.be.login",null);
            }
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);

            modelAndView=new ModelAndView("AccountNoteZoomView");
            request.setAttribute("lid", lid);
            request.setAttribute("p", accountId);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Account Notes");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.note", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

}
