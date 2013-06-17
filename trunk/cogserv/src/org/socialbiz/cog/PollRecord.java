package org.socialbiz.cog;

import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PollRecord extends DOMFace
{
    public VoteRecord[] votes = null;


    public PollRecord(Document doc, Element ele, DOMFace p)
    {
        super(doc, ele, p);
        //some old polls have no id, so give them one....
        //a one will work if there is only one poll on a page
        //seems a reasonable assumption for now...
        String id = getId();
        if (id==null || id.length()==0)
        {
            setAttribute("id", "1");
        }
    }

    public String getProposition()
        throws Exception
    {
        return getScalar("proposition");
    }

    public String getId()
    {
        return getAttribute("id");
    }

    public String[] getChoices()
    {
        return new String[] {"Yes", "Maybe", "No"};
    }

    public long getEndDate()
        throws Exception
    {
        return safeConvertLong(getAttribute("endDate"));
    }

    public void setEndDate(long datetime)
        throws Exception
    {
        setAttribute("endDate", Long.toString(datetime));
    }


    public VoteRecord[] getVotes()
        throws Exception
    {
        if (votes!=null)
        {
            return votes;
        }
        Vector<VoteRecord> nl = getChildren("vote", VoteRecord.class);
        int last = nl.size();

        VoteRecord[] retVal = new VoteRecord[last];
        nl.copyInto(retVal);
        votes = retVal;
        return retVal;
    }

    public VoteRecord findVote(String name)
        throws Exception
    {
        if (votes==null)
        {
            getVotes();
        }
        int lastj = votes.length;
        for (int j=0; j<lastj; j++)
        {
            VoteRecord vote = votes[j];
            if (UtilityMethods.equalsOpenId(name, vote.getWho()))
            {
                return vote;
            }
        }
        return null;
    }

    public VoteRecord newVote(String name)
        throws Exception
    {
        VoteRecord vr = (VoteRecord) createChild("vote", VoteRecord.class);
        vr.setWho(name);
        vr.setTimestamp(System.currentTimeMillis());
        votes = null;   //clear out so recreated
        return vr;
    }

    public boolean voteRequired(UserProfile up)
        throws Exception
    {
        VoteRecord[] votes = getVotes();
        for (VoteRecord vote : votes)
        {
            if (up.hasAnyId(vote.getWho()))
            {
                return false;
            }
        }
        return true;
    }

}
