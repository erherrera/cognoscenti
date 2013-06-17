package org.socialbiz.cog;

import org.socialbiz.cog.AddressListEntry;

/**
* This is for email messages which are sent to the Super Admin
* and you really can't opt out of that responsibility.
* So this makes a message that says that.
*/
public class OptOutSuperAdmin extends OptOutAddr {

    public OptOutSuperAdmin(AddressListEntry _assignee) {
        super(_assignee);
    }

    public void writeUnsubscribeLink(AuthRequest clone) throws Exception {
        writeSentToMsg(clone);
        clone.write("You have received this message because you are a registered 'super admin' for this server. ");
        clone.write("If you want to avoid getting these messages in the future please");
        clone.write("work with the server administrator to change that.</font></p>");
    }

}
