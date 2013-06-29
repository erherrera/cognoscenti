package org.socialbiz.cog.spring;

import java.io.StringWriter;
import java.util.Vector;

import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.AddressListEntry;
import org.socialbiz.cog.AuthDummy;
import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.EmailSender;
import org.socialbiz.cog.HistoryRecord;
import org.socialbiz.cog.License;
import org.socialbiz.cog.LicenseForProcess;
import org.socialbiz.cog.NGContainer;
import org.socialbiz.cog.NGPage;
import org.socialbiz.cog.OptOutAddr;
import org.socialbiz.cog.ReminderMgr;
import org.socialbiz.cog.ReminderRecord;

public class ReminderEmailHelper {

    public static void reminderEmail(AuthRequest ar, String pageId,String reminderId, String emailto, NGContainer ngp)
            throws Exception {

        ReminderMgr rMgr = ngp.getReminderMgr();
        ReminderRecord rRec = rMgr.findReminderByID(reminderId);
        if (rRec == null) {
            throw new NGException("nugen.exception.unable.to.find.reminder", new Object[]{reminderId});
        }
        String subject = "Reminder to Upload: " + rRec.getSubject();
        Vector<AddressListEntry> addressList = AddressListEntry.parseEmailList(emailto);
        for (AddressListEntry ale : addressList)
        {
            OptOutAddr ooa = new OptOutAddr(ale);
            StringWriter bodyWriter = new StringWriter();
            AuthRequest clone = new AuthDummy(ar.getUserProfile(), bodyWriter);
            clone.write("<html><body>");
            writeReminderEmailBody(clone, ngp, rRec);
            NGWebUtils.standardEmailFooter(clone, ar.getUserProfile(), ooa, ngp);
            clone.write("</body></html>");
            EmailSender.containerEmail(ooa, ngp, subject, bodyWriter.toString(), null, new Vector<String>());
        }
        if (ngp instanceof NGPage)
        {
            HistoryRecord.createHistoryRecord(ngp, reminderId,
                    HistoryRecord.CONTEXT_TYPE_DOCUMENT, ar.nowTime,
                    HistoryRecord.EVENT_DOC_UPDATED, ar, "Reminder Emailed to "
                            + emailto);
        }
        ngp.saveContent(ar, "sending reminder email");
    }

    public static void writeReminderEmailBody(AuthRequest ar, NGContainer ngp,
            ReminderRecord rRec) throws Exception {
        String userName = "Guest User";

        userName = rRec.getModifiedBy();
        AddressListEntry ale = new AddressListEntry(userName);
        License lic = null;
        // this is temporary fix
        if (ngp instanceof NGContainer) {
            lic = new LicenseForProcess(((NGPage) ngp).getProcess());
        }

        ar.write("<table>");
        ar.write("<tr><td>From:</td><td>");
        ale.writeLink(ar);
        ar.write("</td></tr>\n<tr><td>Subject:</td><td>");
        ar.writeHtml(rRec.getSubject());
        ar.write("</td></tr>\n<tr><td>Project: </td><td>");
        ngp.writeContainerLink(ar, 100);
        ar.write("</td></tr>\n</table>\n<hr/>\n");
        ar.write("\n<p>You have been invited by ");
        ale.writeLink(ar);
        ar.write(" to upload a file so that it can ");
        ar.write("be shared (in a controlled manner) with others on ");
        ar.write("the project \"");
        ar.writeHtml(ngp.getFullName());
        ar.write("\". Uploading the file will stop the email reminders. </p>");
        ar.write("\n<p><b>Instructions:</b> ");
        ar.writeHtml(rRec.getInstructions());
        ar.write("</p>");
        ar.write("\n<p><b>Description of File:</b> ");
        ar.writeHtml(rRec.getFileDesc());
        ar.write("</p>");
        ar
                .write("\n<p>Click on the following link or cut and paste the URL into a ");
        ar.write("web browser to access the page for uploading the file:</p>");
        ar.write("\n<p><a href=\"");
        ar.write(ar.baseURL);
        ar.write(ar.getResourceURL(ngp, ""));
        ar.write("remindAttachment.htm?lic=");
        ar.writeURLData(lic.getId());
        ar.write("&rid=");
        ar.writeURLData(rRec.getId());
        ar.write("\">");

        ar.write(ar.baseURL);
        ar.write(ar.getResourceURL(ngp, ""));
        ar.write("remindAttachment.htm?lic=");
        ar.writeURLData(lic.getId());
        ar.write("&rid=");
        ar.writeURLData(rRec.getId());

        ar.write("</a>");
        ar.write("</p>");
        ar.write("\n<p>Thank you.</p>");
        ar.write("\n<hr/>");
    }

}
