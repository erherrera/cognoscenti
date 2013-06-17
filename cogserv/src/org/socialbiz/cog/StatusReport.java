package org.socialbiz.cog;

import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* A StatusReport is a record that holds the specification of a status
* report that can be generated at any time.  It will point to a set of
* projects, and will also have a set of tasks to exclude.
*/
public class StatusReport extends DOMFace
{

    public StatusReport(Document nDoc, Element nEle, DOMFace p)
    {
        super(nDoc, nEle, p);
    }

    public String getId()
        throws Exception
    {
        return getAttribute("id");
    }

    public void setId(String newVal)
        throws Exception
    {
        setAttribute("id", newVal);
    }

    public String getName()
        throws Exception
    {
        return getScalar("name");
    }

    public void setName(String newVal)
        throws Exception
    {
        setScalar("name", newVal);
    }

    public String getDescription()
        throws Exception
    {
        return getScalar("desc");
    }

    public void setDescription(String newVal)
        throws Exception
    {
        setScalar("desc", newVal);
    }


    public List<ProjectLink> getProjects() throws Exception {
        return getChildren("projLink", ProjectLink.class);
    }

    public ProjectLink getOrCreateProject(String key) throws Exception {

        //first lets make sure that this project is not already in the set
        for (ProjectLink pl : getProjects()) {
            if (pl.getKey().equals(key)) {
                return pl;
            }
        }

        ProjectLink newPl = (ProjectLink) createChild("projLink", ProjectLink.class);
        newPl.setKey(key);
        return newPl;
    }

    public void deleteProject(String key) throws Exception {

        ProjectLink found = null;
        for (ProjectLink stat : getProjects()) {
            if (key.equals(stat.getKey())) {
                found = stat;
            }
        }

        if (found != null) {
            removeChild(found);
        }
    }
}
