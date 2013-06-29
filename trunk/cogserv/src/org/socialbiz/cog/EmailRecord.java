package org.socialbiz.cog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.socialbiz.cog.DOMFace;
import org.socialbiz.cog.exception.NGException;
import java.util.Vector;

public class EmailRecord extends DOMFace
{

    public static final String READY_TO_GO = "Ready";
    public static final String SENT = "Sent";
    public static final String FAILED = "Failed";

    public EmailRecord(Document doc, Element upEle, DOMFace p)
    {
        super(doc,upEle, p);
    }

    public String getId()
    {
        String val = getAttribute("id");
        if (val==null)
        {
            return "";
        }
        return val;
    }
    public void setId(String id)
    {
        setAttribute("id", id);
    }

    public String getFromAddress() {
        return getAttribute("fromAddress");
    }
    public void setFromAddress(String fromAddress) {
        setAttribute("fromAddress",fromAddress);
    }

    public Vector<OptOutAddr> getAddressees() throws Exception {
        Vector<DOMFace> children = (Vector<DOMFace>) getChildren("to", DOMFace.class);

        Vector<OptOutAddr> res = new Vector<OptOutAddr>();
        for (DOMFace assignee : children) {

            String ootype = assignee.getAttribute("ootype");
            String email = assignee.getAttribute("email");
            AddressListEntry ale = new AddressListEntry(email);
            if ("Role".equals(ootype)) {
                String containerID = assignee.getAttribute("containerID");
                String roleName = assignee.getAttribute("roleName");
                res.add(new OptOutRolePlayer(ale, containerID, roleName));
            }
            else if ("Super".equals(ootype)) {
                res.add(new OptOutSuperAdmin(ale));
            }
            else if ("Indiv".equals(ootype)) {
                res.add(new OptOutIndividualRequest(ale));
            }
            else if ("Direct".equals(ootype)) {
                res.add(new OptOutDirectAddress(ale));
            }
            else {
                res.add(new OptOutAddr(ale));
            }
        }
        return res;
    }


    public void setAddressees(Vector<OptOutAddr> inad) throws Exception {
        removeAllNamedChild("to");
        for (OptOutAddr ooa : inad) {

            DOMFace assignee = createChild("to", DOMFace.class);
            assignee.setAttribute("email", ooa.getEmail());
            if (ooa instanceof OptOutRolePlayer) {
                OptOutRolePlayer oorm = (OptOutRolePlayer) ooa;
                assignee.setAttribute("ootype", "Role");
                assignee.setAttribute("containerID", oorm.containerID);
                assignee.setAttribute("roleName", oorm.roleName);
            }
            else if (ooa instanceof OptOutSuperAdmin) {
                assignee.setAttribute("ootype", "Super");
            }
            else if (ooa instanceof OptOutIndividualRequest) {
                assignee.setAttribute("ootype", "Indiv");
            }
            else if (ooa instanceof OptOutDirectAddress) {
                assignee.setAttribute("ootype", "Direct");
            }
            else {
                assignee.setAttribute("ootype", "Gen");
            }
        }
    }

    public String getCcAddress() {
        return getScalar("ccAddress");
    }
    public void setCcAddress(String ccAddress) {
        setScalar("ccAddress",ccAddress);
    }

    /**
    * The body is the message without the unsubscribe part
    */
    public String getBodyText() {
        return getScalar("bodyText");
    }
    public void setBodyText(String bodyText) {
        setScalar("bodyText",bodyText);
    }

    public String getStatus() {
        return getAttribute("status");
    }
    public void setStatus(String status) {
        setAttribute("status",status);
    }
    public boolean statusReadyToSend() {
        return READY_TO_GO.equals(getAttribute("status"));
    }

    public long getLastSentDate() {
        return safeConvertLong(getAttribute("lastSentDate"));
    }
    public void setLastSentDate(long sentDate) {
        setAttribute("lastSentDate",String.valueOf(sentDate));
    }

    public String getSubject() {
        return getAttribute("subject");
    }
    public void setSubject(String subject) {
        setAttribute("subject",subject);
    }

    public String getProjectId() {
        return getAttribute("projectId");
    }
    public void setProjectId(String projectId) {
        setAttribute("projectId",projectId);
    }

    public long getCreateDate() {
        return safeConvertLong(getAttribute("createDate"));
    }
    public void setCreateDate(long createDate) {
        setAttribute("createDate",String.valueOf(createDate));
    }

    public String getExceptionMessage() {
        return getScalar("exception");
    }
    public void setExceptionMessage(Exception e) {
        setScalar("exception", NGException.getFullMessage(e));
    }


    /**
    * Either this sends the email message, and marks it as sent, or it fails
    * to send the message, and marks it as failed.
    */
    public void sendThisMessage() throws Exception {
        EmailSender emailSender = EmailSender.getInstance();
        emailSender.sendPreparedMessageImmediately(this);
    }

}