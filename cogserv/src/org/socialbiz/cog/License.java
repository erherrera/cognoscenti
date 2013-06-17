package org.socialbiz.cog;


/**
* A license is also known as a "free pass".  Actually there can be
* many kinds of license, some of which might be free.  The point is
* that an access using a license ID in the parameters will then give
* the requester the information that is specified as being allowed in
* the license.
* Initially a license will be used to give non-authenticated users
* access to a single page or to just the process on that page.
*/
public interface License
{

    public String getId()
        throws Exception;

    public String getNotes()
        throws Exception;
    public void setNotes(String newVal)
        throws Exception;

    public String getCreator()
        throws Exception;
    public void setCreator(String newVal)
        throws Exception;

    public long getTimeout()
        throws Exception;
    public void setTimeout(long timeout)
        throws Exception;

    public String getRole()
        throws Exception;
    public void setRole(String newRole)
        throws Exception;

    public boolean isReadOnly()
        throws Exception;
    public void setReadOnly(boolean isReadOnly)
        throws Exception;

}
