package org.socialbiz.cog;

import org.socialbiz.cog.AddressListEntry;

/**
* This is for email messages which are send in order to satisfy a
* request that a user makes themselves.  For example, requesting
* to be in a particular role, you get the message confirming that,
* there is nothing you can (or would want) to do to avoid that.
*/
public class OptOutIndividualRequest extends OptOutAddr {

    public OptOutIndividualRequest(AddressListEntry _assignee) {
        super(_assignee);
    }

    public void writeUnsubscribeLink(AuthRequest clone) throws Exception {
        writeSentToMsg(clone);
        clone.write("You have received this message in order to carry out the request that you made. ");
        writeConcludingPart(clone);
    }

}
