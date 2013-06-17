package org.socialbiz.cog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LeafletResponseRecord extends DOMFace
{

    public LeafletResponseRecord(Document definingDoc, Element definingElement, DOMFace lr)
    {
        super(definingDoc, definingElement, lr);

        //verify that there is a user attribute on the tag
        String user = getAttribute("user");
        if (user==null || user.length()==0)
        {
            throw new RuntimeException("A note response tag MUST have an "
                         +"attribute 'user' with a valid value");
        }
    }

    /**
    * The user is the key to this record.  There is no way to change
    * the key (no way to set it).  It must be created with a particular
    * key.  You can read it with this method.
    */
    public String getUser()
    {
        return getAttribute("user");
    }
    public void setUser(String u)
    {
        setAttribute("user", u);
    }

    public long getLastEdited()
    {
        return safeConvertLong(getAttribute("edited"));
    }
    public void setLastEdited(long newCreated)
    {
        setAttribute("edited", Long.toString(newCreated));
    }

    public String getData()
    {
        return getScalar("data");
    }
    public void setData(String newData)
    {
        setScalar("data", newData);
    }
    public String getChoice()
    {
        return getScalar("choice");
    }
    public void setChoice(String newData)
    {
        setScalar("choice", newData);
    }

}
