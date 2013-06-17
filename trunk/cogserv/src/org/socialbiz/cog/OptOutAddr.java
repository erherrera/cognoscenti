package org.socialbiz.cog;

import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.AddressListEntry;

/**
* The purpose of this class it to remember an assignee to an email message
* and to record WHY that person was assigned to a message, so that a link can
* be generated that allows them to "opt out" of getting the email in the
* future.
*
* There is more than one reason that a user might be assigned to receive
* an email address.  This base class provides the most basic, generic unsubscribe
* link (to the user unsubscribe page).
*
* More specialized classes should:
* 1. provide a way to remove yourself from a role when message was sent to role
* 2. complete or cancel an activity if assigned because of an activity
*/
public class OptOutAddr {

    AddressListEntry assignee;

    public OptOutAddr(AddressListEntry _assignee) {
        assignee = _assignee;
    }

    /**
    * Checks the current assignee, and throws a standard exception
    * if the assignee does not have an email address, or for any other
    * reason that it appears this addressee is not valid.
    */
    public void assertValidEmail() throws Exception {
        String useraddress = assignee.getEmail();
        if (useraddress==null || useraddress.length()==0) {
            throw new NGException("nugen.exception.email.address.is.empty", new Object[]{assignee.getUniversalId()});
        }
    }

    public AddressListEntry getAssignee() {
        return assignee;
    }

    public String getEmail() {
        return assignee.getEmail();
    }

    public boolean matches(OptOutAddr ooa) {
        return assignee.hasAnyId(ooa.getEmail());
    }
    public boolean matches(AddressListEntry ale) {
        return assignee.hasAnyId(ale.getUniversalId());
    }
    public boolean matches(String emailAddress) {
        return assignee.hasAnyId(emailAddress);
    }

    public boolean isUserWithProfile() {
        UserProfile up = UserManager.findUserByAnyId(getEmail());
        return (up!=null);
    }


    protected void writeSentToMsg(AuthRequest clone) throws Exception {
        assertValidEmail();
        clone.write("\n<hr/>\n<p><font size=\"-2\">This message was sent to ");
        clone.writeHtml(assignee.getEmail());
        clone.write(".  ");
    }


    public void writeUnsubscribeLink(AuthRequest clone) throws Exception {
        writeSentToMsg(clone);
        writeConcludingPart(clone);
    }

    protected void writeConcludingPart(AuthRequest clone) throws Exception {
        String emailId = assignee.getEmail();
        UserProfile up = UserManager.findUserByAnyId(emailId);
        if(up != null){
            clone.write("  To change the e-mail communication you receive from ");
            clone.write("Cognoscenti in future, you can ");
            clone.write("<a href=\"");
            clone.writeHtml(clone.baseURL);
            clone.write("v/unsubscribe.htm?accessCode=");
            clone.writeURLData(up.getAccessCode());
            clone.write("&userKey=");
            clone.writeURLData(up.getKey());
            clone.write("&emailId=");
            clone.writeURLData(emailId);
            clone.write("\">alter your subscriptions</a>.</font></p>");
        }
        else {
            clone.write("  You have not created a profile at Cognoscenti, or have not ");
            clone.write("associated this address with your existing profile.</font></p>");
        }
    }


}
