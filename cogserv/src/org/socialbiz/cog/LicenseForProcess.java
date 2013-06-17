package org.socialbiz.cog;


/**
* A license is also known as a "free pass".  Actually there can be
* many kinds of license, some of which might be free.  The point is
* that an access using a license ID in the parameters will then give
* the requester the information that is specified as being allowed in
* the license.
*
* Initially a license will be used to give non-authenticated users
* access to a single page or to just the process on that page.
*
* Note: processes now carry a LicenseRecord with some of this information
* but this class fills in the rest with fixed values.
*/
public class LicenseForProcess implements License
{

    public ProcessRecord proc;

    public LicenseForProcess(ProcessRecord newProc)
    {
        proc = newProc;
    }

    public String getId()
        throws Exception
    {
        return proc.accessLicense().getId();
    }

    public String getNotes()
        throws Exception
    {
        return "This license automatically created for the process.";
    }

    public void setNotes(String newVal)
        throws Exception
    {
        //ignore this
    }

    public String getCreator()
        throws Exception
    {
        return "* Process *";
    }

    public void setCreator(String newVal)
        throws Exception
    {
        //ignore this
    }

    public long getTimeout()
        throws Exception
    {
        return System.currentTimeMillis() + 86000000;
    }

    public void setTimeout(long timeout)
        throws Exception
    {
        //ignore this
    }

    public String getRole() throws Exception {
        //we have to return somthing.
        //Member is pretty general.
        return "Member";
    }

    public void setRole(String newRole) throws Exception {
        //ignore this
    }

    public boolean isReadOnly() throws Exception {
        return false;
    }
    public void setReadOnly(boolean isReadOnly) throws Exception {
        //ignore this
    }

}
