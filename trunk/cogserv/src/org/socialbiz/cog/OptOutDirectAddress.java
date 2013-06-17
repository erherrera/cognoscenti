package org.socialbiz.cog;

import org.socialbiz.cog.AddressListEntry;

/**
* This is for email messages which are sent to the Super Admin
* and you really can't opt out of that responsibility.
* So this makes a message that says that.
*/
public class OptOutDirectAddress extends OptOutAddr {

    public OptOutDirectAddress(AddressListEntry _assignee) {
        super(_assignee);
    }

    public void writeUnsubscribeLink(AuthRequest clone) throws Exception {
        writeSentToMsg(clone);
        clone.write("You have received this message because the sender entered your email address directly into the "+
            "address prompt and not due to any automated email mechanism. ");
        writeConcludingPart(clone);
    }

}
