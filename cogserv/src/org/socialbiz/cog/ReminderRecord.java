package org.socialbiz.cog;

import java.io.StringWriter;
import java.util.Vector;

import org.socialbiz.cog.spring.NGWebUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
* ReminderRecord hold the information about a reminder to attach a file
* to a page.  IT is assigned to a person (email address) and then email
* messages can be sent to that person reminding them that they have a
* file that needs to be uploaded to the shared place.
*/
public class ReminderRecord extends DOMFace
{
    public ReminderRecord(Document doc, Element definingElement, DOMFace p)
    {
        super (doc, definingElement, p);
    }


    public String getId()
    {
        return checkAndReturnAttributeValue("id");
    }
    public void setId(String id)
    {
        setAttribute("id", id);
    }

    /**
    * Subject is like an email subject
    * This serves as the display name of the request
    */
    public String getSubject()
    {
        return checkAndReturnAttributeValue("subject");
    }
    public void setSubject(String desc)
    {
        setAttribute("subject", desc);
    }

    /**
    * When a virtual attachments is created to be filled in, a person
    * is assigned to fill int he attachment, and reminders will be sent
    * until it is filled.
    */
    public String getAssignee()
    {
        return getAttribute("assignee");
    }
    public void setAssignee(String assignee)
    {
        setAttribute("assignee", assignee);
    }
    public boolean isAssignee(UserProfile up)
    {
        return up.hasAnyId(getAttribute("assignee"));
    }


    /**
    * The instructions are a place that the requester can explain
    * a bit more about what is needed, where they saw the file
    * and anything else that might not be approrpriate in the
    * eventual persistent file description.
    */
    public String getInstructions()
    {
        return checkAndReturnAttributeValue("instructions");
    }
    public void setInstructions(String instructions)
    {
        setAttribute("instructions", instructions);
    }

    /**
    * The file name can be proposed by the requester, in
    * the case that a particular naming convention is required
    * on the attachment leaf.
    */
    public String getFileName()
    {
        return checkAndReturnAttributeValue("fileName");
    }
    public void setFileName(String fname)
    {
        setAttribute("fileName", fname);
    }


    /**
    * FileDescription will eventually be the description on the
    * file itself (by default)
    */
    public String getFileDesc()
    {
        return checkAndReturnAttributeValue("fileDesc");
    }
    public void setFileDesc(String description)
    {
        setAttribute("fileDesc", description);
    }


    /**
    * The destination folder is where the file is to be
    * placed.  It is a path notation that indicates
    * the folder name, and the path within it.
    */
    public String getDestFolder()
    {
        return checkAndReturnAttributeValue("folder");
    }
    public void setDestFolder(String fname)
    {
        setAttribute("folder", fname);
    }


    public String getModifiedBy()
    {
        return checkAndReturnAttributeValue("modifiedBy");
    }
    public void setModifiedBy(String modifiedBy)
    {
        setAttribute("modifiedBy", modifiedBy);
    }

    public long getModifiedDate()
    {
        return safeConvertLong(checkAndReturnAttributeValue("modifiedDate"));
    }
    public void setModifiedDate(long modifiedDate)
    {
        setAttribute("modifiedDate", Long.toString(modifiedDate));
    }

    private String checkAndReturnAttributeValue(String attrName)
    {
        String val = getAttribute(attrName);
        if (val==null)
        {
            return "";
        }
        return val;
    }

    public boolean isOpen()
    {
        String isOpen = getAttribute("isOpen");
        if (isOpen!=null && "closed".equals(isOpen))
        {
            return false;
        }
        return true;
    }
    public void setOpen()
    {
        setAttribute("isOpen", "open");
    }
    public void setClosed()
    {
        setAttribute("isOpen", "closed");
    }

    public void setSendNotification(String sendNotification)
    {
        setAttribute("sendNotification", sendNotification);
    }

    public String getSendNotification()
    {
        String sendNotification = getAttribute("sendNotification");
        if(sendNotification.length() == 0){
            return "yes";
        }
        return sendNotification;
    }

    public void createHistory(AuthRequest ar, NGPage ngp, int event,
        String comment) throws Exception
    {
        HistoryRecord.createHistoryRecord(ngp,getId(),
            HistoryRecord.CONTEXT_TYPE_DOCUMENT,
            getModifiedDate(), event, ar, comment);
    }


    public void writeReminderEmailBody(AuthRequest ar, NGContainer ngp) throws Exception {
        String userName = getModifiedBy();
        AddressListEntry ale = new AddressListEntry(userName);

        ar.write("<table>");
        ar.write("<tr><td>From:</td><td>");
        ale.writeLink(ar);
        ar.write("</td></tr>\n<tr><td>Subject:</td><td>");
        ar.writeHtml(getSubject());
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
        ar.writeHtml(getInstructions());
        ar.write("</p>");
        ar.write("\n<p>Click on the following link or cut and paste the URL into a ");
        ar.write("web browser to access the page for uploading the file:</p>");
        ar.write("\n<p><a href=\"");
        ar.write(ar.baseURL);
        ar.write(ar.getResourceURL(ngp, ""));
        ar.write("remindAttachment.htm?");
        ar.write(AccessControl.getAccessReminderParams(ngp, this));
        ar.write("&rid=");
        ar.writeURLData(getId());
        ar.write("\">");

        ar.write(ar.baseURL);
        ar.write(ar.getResourceURL(ngp, ""));
        ar.write("remindAttachment.htm?");
        ar.write(AccessControl.getAccessReminderParams(ngp, this));
        ar.write("&rid=");
        ar.writeURLData(getId());

        ar.write("</a>");
        ar.write("</p>");
        ar.write("\n<p><b>Description of File:</b> ");
        ar.writeHtml(getFileDesc());
        ar.write("</p>");
        ar.write("\n<p>Thank you.</p>");
        ar.write("\n<hr/>");
    }


    public static void reminderEmail(AuthRequest ar, String pageId,String reminderId,
            String emailto, NGContainer ngp) throws Exception {

        ReminderMgr rMgr = ngp.getReminderMgr();
        ReminderRecord rRec = rMgr.findReminderByIDOrFail(reminderId);
        String subject = "Reminder to Upload: " + rRec.getSubject();
        Vector<AddressListEntry> addressList = AddressListEntry.parseEmailList(emailto);
        for (AddressListEntry ale : addressList) {
            OptOutAddr ooa = new OptOutAddr(ale);
            StringWriter bodyWriter = new StringWriter();
            AuthRequest clone = new AuthDummy(ar.getUserProfile(), bodyWriter);
            clone.write("<html><body>");
            rRec.writeReminderEmailBody(clone, ngp);
            NGWebUtils.standardEmailFooter(clone, ar.getUserProfile(), ooa, ngp);
            clone.write("</body></html>");
            EmailSender.containerEmail(ooa, ngp, subject, bodyWriter.toString(), null, new Vector<String>());
        }
        if (ngp instanceof NGPage) {
            HistoryRecord.createHistoryRecord(ngp, reminderId,
                    HistoryRecord.CONTEXT_TYPE_DOCUMENT, ar.nowTime,
                    HistoryRecord.EVENT_DOC_UPDATED, ar, "Reminder Emailed to "
                            + emailto);
        }
        ngp.saveContent(ar, "sending reminder email");
    }

}
