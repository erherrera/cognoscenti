package org.socialbiz.cog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
* The user may choose to be on the notify list for a pages, in order to be informed of when
* they change.
*/
public class NotificationRecord extends DOMFace
{
    //I figure this will be hit quite a bit, so remembering the
    //id when fetched the first time will probably make a difference,
    //and should not cause any additional memory bloat
    String cachedId;

    public NotificationRecord(Document doc, Element upEle, DOMFace p)
    {
        super(doc,upEle, p);
    }

    /**
    * Pattern is "create" and the class name, is the proper way to
    * create a new element in the DOM tree, and return the wrapper class
    * Must pass the user that this is an ID of.
    */
    public static NotificationRecord createNotificationRecord(UserProfile user, String newId)
        throws Exception
    {
        if (newId==null)
        {
            throw new RuntimeException("null value for newId passed to createNotificationRecord");
        }
        NotificationRecord newSR = (NotificationRecord) user.createChildWithID("notification",
                NotificationRecord.class, "pagekey", newId);
        return newSR;
    }


    public String getPageKey()
    {
        if (cachedId==null)
        {
            cachedId = getAttribute("pagekey");
        }
        return cachedId;
    }



}
