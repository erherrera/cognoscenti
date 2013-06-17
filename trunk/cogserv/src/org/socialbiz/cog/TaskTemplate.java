package org.socialbiz.cog;

import org.socialbiz.cog.exception.NGException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* This is a container for a task, a task template
*/
public class TaskTemplate extends DOMFace
{

    public TaskTemplate(Document definingDoc, Element definingElement, DOMFace p)
        throws Exception
    {
        super(definingDoc, definingElement, p);
    }

    public void readFromTask(GoalRecord tr)
        throws Exception
    {
        setSynopsis(tr.getSynopsis());
        setDescription(tr.getDescription());
        setActionScripts(tr.getActionScripts());
        setPriority(tr.getPriority());
        setDuration(tr.getDuration());
        setAssignee(tr.getAssigneeCommaSeparatedList());
        setReviewers(tr.getReviewers());
        setCreator(tr.getCreator());
    }

    public void sendToTask(GoalRecord tr)
        throws Exception
    {
        tr.setSynopsis(getSynopsis());
        tr.setDescription(getDescription());
        tr.setActionScripts(getActionScripts());
        tr.setPriority(getPriority());
        tr.setDuration(getDuration());
        tr.setAssigneeCommaSeparatedList(getAssignee());
        tr.setReviewers(getReviewers());
        tr.setCreator(tr.getCreator());
    }


    public String getId()
        throws Exception
    {
        return getAttribute("id");
    }

    public void setId(String newVal)
        throws Exception
    {
        if (newVal.length()!=4)
        {
            throw new NGException("nugen.exception.invalid.id",null);
        }
        for (int i=0; i<4; i++)
        {
            if (newVal.charAt(i)<'0' || newVal.charAt(i)>'9')
            {
                throw new NGException("nugen.exception.invalid.id",null);
            }
        }
        setAttribute("id", newVal);
    }

    public String getSynopsis()
        throws Exception
    {
        return getScalar("synopsis");
    }

    public void setSynopsis(String newVal)
        throws Exception
    {
        if (newVal == null) {
            newVal = "";
        }
        setScalar("synopsis", newVal);
    }

    public String getDescription()
        throws Exception
    {
        return getScalar("description");
    }
    public void setDescription(String newVal)
        throws Exception
    {
        if (newVal == null) {
            newVal = "";
        }
        setScalar("description", newVal);
    }

    public String getActionScripts()
        throws Exception
    {
        return getScalar("actionScripts");
    }
    public void setActionScripts(String newVal)
        throws Exception
    {
        if (newVal == null)
        {
            newVal = "";
        }
        setScalar("actionScripts", newVal);
    }

    public int getPriority()
        throws Exception
    {
        String priority = getScalar("priority");
        return safeConvertInt(priority);
    }
    public void setPriority(int newVal)
        throws Exception
    {
        setScalar("priority", Integer.toString(newVal));
    }

    public long getDuration()
        throws Exception
    {
        String duration = getScalar("duration");
        return safeConvertLong(duration);
    }
    public void setDuration(long newVal)
        throws Exception
    {
        setScalar("duration", Long.toString(newVal));
    }


    public String getAssignee()
        throws Exception
    {
        return getScalar("assignee");
    }
    public void setAssignee(String newVal)
        throws Exception
    {
        setScalar("assignee", newVal);
    }

    public String getReviewers()
        throws Exception
    {
        return getScalar("reviewers");
    }
    public void setReviewers(String newVal)
        throws Exception
    {
        setScalar("reviewers", newVal);
    }
    public void setCreator(String newVal)
    throws Exception
    {
    setScalar("creator", newVal);
    }
    public String getCreator()
    throws Exception
    {
        return getScalar("creator");
    }


}
