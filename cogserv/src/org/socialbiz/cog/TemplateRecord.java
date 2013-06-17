package org.socialbiz.cog;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TemplateRecord extends DOMFace
{
    //I figure this will be hit quite a bit, so remembering the
    //id when fetched the first time will probably make a difference,
    //and should not cause any additional memory bloat
    String cachedId;

    public TemplateRecord(Document doc, Element upEle, DOMFace p)
    {
        super(doc,upEle, p);
    }

    /**
    * Pattern is "create" and the class name, is the proper way to
    * create a new element in the DOM tree, and return the wrapper class
    * Must pass the user that this is an ID of.
    */
    public static TemplateRecord createTemplateRecord(UserProfile user, String newId)
        throws Exception
    {
        if (newId==null)
        {
            throw new RuntimeException("null value for newId passed to createTemplateRecord");
        }
        TemplateRecord newSR = user.createChildWithID("template",
                TemplateRecord.class, "pagekey", newId);

        return newSR;
    }

    public void removeTemplateRecord(UserProfile user)
        throws Exception
    {
        user.removeChild(this);
    }


    public static List<TemplateRecord> getAllTemplateRecords(UserProfile user)
        throws Exception
    {
        List<TemplateRecord> templateList = new ArrayList<TemplateRecord>();
        Vector<TemplateRecord> chilluns = user.getChildren("template", TemplateRecord.class);
        Enumeration<TemplateRecord> e = chilluns.elements();
        while (e.hasMoreElements())
        {
            templateList.add(e.nextElement());
        }
        return templateList;
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

