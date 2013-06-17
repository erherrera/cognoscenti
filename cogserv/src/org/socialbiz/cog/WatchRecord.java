package org.socialbiz.cog;

import java.util.Enumeration;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
* The user may watch a number of pages, in order to be informed of when
* they change.
*/
public class WatchRecord extends DOMFace
{
    //I figure this will be hit quite a bit, so remembering the
    //id when fetched the first time will probably make a difference,
    //and should not cause any additional memory bloat
    String cachedId;

    public WatchRecord(Document doc, Element upEle, DOMFace p)
    {
        super(doc,upEle, p);
    }

    /**
    * Pattern is "create" and the class name, is the proper way to
    * create a new element in the DOM tree, and return the wrapper class
    * Must pass the user that this is an ID of.
    */
    public static WatchRecord createWatchRecord(UserProfile user, String newId, long now)
        throws Exception
    {
        if (newId==null)
        {
            throw new RuntimeException("null value for newId passed to createWatchRecord");
        }
        WatchRecord newSR = (WatchRecord) user.createChildWithID("watch",
                WatchRecord.class, "pagekey", newId);
        newSR.setLastSeen(now);
        return newSR;
    }

    /**
    * Pattern is "remove" and the class name, is the proper way to
    * create a new element in the DOM tree, and return the wrapper class
    * Must pass the user that this is an ID of.
    */
    public void removeWatchRecord(UserProfile user)
        throws Exception
    {
        user.removeChild(this);
    }

    /**
    * Pattern is "find" and the class name, is the proper way to
    * read the DOM tree for all the elements of thsi type
    */
    public static void findWatchRecord(UserProfile user, Vector<WatchRecord> results)
        throws Exception
    {
        Vector<WatchRecord> chilluns = user.getChildren("watch", WatchRecord.class);
        Enumeration<WatchRecord> e = chilluns.elements();
        while (e.hasMoreElements())
        {
            results.add(e.nextElement());
        }
    }

    public String getPageKey()
    {
        if (cachedId==null)
        {
            cachedId = getAttribute("pagekey");
        }
        return cachedId;
    }


    public void setLastSeen(long seenTime)
    {
        setAttributeLong("lastseen",seenTime);
    }

    public long getLastSeen()
    {
        return getAttributeLong("lastseen");
    }

}
