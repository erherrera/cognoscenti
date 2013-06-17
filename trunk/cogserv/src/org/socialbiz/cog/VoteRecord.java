package org.socialbiz.cog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class VoteRecord extends DOMFace
{

    public VoteRecord(Document doc, Element ele, DOMFace p)
    {
        super(doc, ele, p);
    }

    public String getWho()
        throws Exception
    {
        return getScalar("who");
    }
    public void setWho(String id)
        throws Exception
    {
        setScalar("who", id);
    }

    public String getChoice()
        throws Exception
    {
        return getScalar("choice");
    }
    public void setChoice(String choice)
        throws Exception
    {
        setScalar("choice", choice);
    }
    public String getComment()
        throws Exception
    {
        return getScalar("comment");
    }
    public void setComment(String comment)
        throws Exception
    {
        setScalar("comment", comment);
    }
    public long getTimestamp()
    {
        String ts = getScalar("time");
        if (ts==null)
        {
            return 0;
        }
        return safeConvertLong(ts);
    }
    public void setTimestamp(long newTime)
    {
        setScalar("time", Long.toString(newTime));
    }
}

