package org.socialbiz.cog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class UserInfoRecord extends DOMFace
{


    public UserInfoRecord(Document nDoc, Element nEle, DOMFace p)
    {
        super(nDoc, nEle, p);
    }

    public long getModTime()
    {
        return safeConvertLong(getAttribute("modTime"));
    }
    public void setModTime(long newTime)
    {
        setAttribute("modTime", Long.toString(newTime));
    }

    public String getModUser()
    {
        return getAttribute("modUser");
    }
    public void setModUser(String newUser)
    {
        setAttribute("modUser", newUser);
    }


}
