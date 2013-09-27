/*
 * Copyright 2013 Keith D Swenson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors Include: Shamim Quader, Sameer Pradhan, Kumar Raja, Jim Farris,
 * Sandia Yang, CY Chen, Rajiv Onat, Neal Wang, Dennis Tam, Shikha Srivastava,
 * Anamika Chaudhari, Ajay Kakkar, Rajeev Rastogi
 */

package org.socialbiz.cog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* A TaskRef is a reference to a Task.  It is a record that can be placed
* in one document, that points to a task that exists in a Project (NGPage).
* The purpose is to allow a user's profile to contain a list of task
* references, and allow that user to reorganize them and manipulate
* them, without messing with the original task or the way that task
* appears in the project task list.
*/
public class TaskRef extends DOMFace
{

    //this is a temporary (non persistent) marker that can be used
    //to garbage collect left over dangling task references.
    public boolean touchFlag = false;

    public TaskRef(Document nDoc, Element nEle, DOMFace p)
    {
        super(nDoc, nEle, p);
    }

    public String getProjectKey()
        throws Exception
    {
        return getAttribute("projKey");
    }

    public void setProjectKey(String newVal)
        throws Exception
    {
        setAttribute("projKey", newVal);
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

    /**
    * syncFromTask picks up the common values from the task record
    */
    public void syncFromTask(GoalRecord tr) throws Exception {

        setSynopsis(tr.getSynopsis());
        setDescription(tr.getDescription());
        setDueDate(tr.getDueDate());
        setPriority(tr.getPriority());
        setDuration(tr.getDuration());
        setState(tr.getState());
        setStatus(tr.getStatus());
        setPercentComplete(tr.getPercentComplete());

    }


    public String getSynopsis()
        throws Exception
    {
        return getScalar("synopsis");
    }

    public void setSynopsis(String newVal)
        throws Exception
    {
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

    public long getDueDate()
        throws Exception
    {
        String endDate = getScalar("dueDate");
        return safeConvertLong(endDate);
    }
    public void setDueDate(long newVal)
        throws Exception
    {
        setScalar("dueDate", Long.toString(newVal));
    }

    public long getStartDate()
        throws Exception
    {
        String startDate = getScalar("startDate");
        return safeConvertLong(startDate);
    }
    public void setStartDate(long newVal)
        throws Exception
    {
        setScalar("startDate", Long.toString(newVal));
    }

    public long getEndDate()
        throws Exception
    {
        String endDate = getScalar("endDate");
        return safeConvertLong(endDate);
    }
    public void setEndDate(long newVal)
        throws Exception
    {
        setScalar("endDate", Long.toString(newVal));
    }

    public int getPriority()
        throws Exception
    {
        String priority = getScalar("priority");
        return safeConvertInt(priority);
    }
    public static String getPriorityStr(int priority)
    throws Exception
    {
        switch (priority)
        {
            case 0:
                return BaseRecord.PRIORITY_HIGH_STR;
            case 1:
                return BaseRecord.PRIORITY_MIDIUM__STR;
            case 2:
                return BaseRecord.PRIORITY_LOW__STR;
            default:
        }
        return BaseRecord.PRIORITY_LOW__STR;
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

    public int getState()
        throws Exception
    {
        String stateVal = getScalar("state");
        return safeConvertInt(stateVal);
    }

    public void setState(int newVal)
        throws Exception
    {
        setScalar("state", Integer.toString(newVal));
    }

    public int getRank()
        throws Exception
    {
        String rank = getScalar("rank");
        return safeConvertInt(rank);
    }
    public void setRank(int newVal)
        throws Exception
    {
        setScalar("rank", Integer.toString(newVal));
    }

    public String getStatus()
        throws Exception
    {
        return getScalar("status");
    }
    public void setStatus(String newVal)
        throws Exception
    {
        setScalar("status", newVal);
    }


    /**
    * A user is allowed to specify what percentage that the task is complete.
    * This is rolled up into the values of the parent tasks
    */
    public int getPercentComplete()
        throws Exception
    {
        String stateVal = getScalar("percent");
        return safeConvertInt(stateVal);
    }
    /**
    * A user is allowed to specify what percentage that the task is complete.
    * The value must be 0 at the lowest, and 100 at the highest.
    */
    public void setPercentComplete(int newVal)
        throws Exception
    {
        if (newVal<0 || newVal>100) {
            throw new Exception("Percent complete value must be between 0% and 100%, instead received "+newVal+"%");
        }
        setScalar("percent", Integer.toString(newVal));
    }

    public static void sortTasksByRank(List<TaskRef> tasks)
    {
        Collections.sort(tasks, new TaskRefRankComparator());
    }


    static class TaskRefRankComparator implements Comparator<TaskRef> {
        public TaskRefRankComparator() {
        }

        public int compare(TaskRef o1, TaskRef o2) {
            try {
                int rank1 = o1.getRank();
                int rank2 = o2.getRank();
                if (rank1 == rank2) {
                    return 0;
                }
                if (rank1 < rank2) {
                    return -1;
                }
                return 1;
            }
            catch (Exception e) {
                return 0;
            }
        }
    }

}
