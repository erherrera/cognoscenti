package org.socialbiz.cog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* A StatusReport contains a set of ProjectLinks which tell the specific
* information about a project to include in the status report.
*/
public class ProjectLink extends DOMFace
{

    //this is a temporary (non persistent) marker that can be used
    //to garbage collect left over dangling project references.
    public boolean touchFlag = false;

    public ProjectLink(Document nDoc, Element nEle, DOMFace p)
    {
        super(nDoc, nEle, p);
    }

    public String getKey()
        throws Exception
    {
        return getAttribute("key");
    }

    public void setKey(String newVal)
        throws Exception
    {
        setAttribute("key", newVal);
    }

    /**
    * Convenience function that looks up and returns the index record for the
    * associated project.  If the project does not exist, this returns null.
    */
    public NGPageIndex getPageIndexOrNull() throws Exception {
        return NGPageIndex.getContainerIndexByKey(getKey());
    }

}
