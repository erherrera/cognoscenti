package org.socialbiz.cog;

import java.io.Writer;

/**
* Deprecated Class to support Polls.  Instead, just use a note
*/
public class SectionPoll extends SectionUtil implements SectionFormat
{


    public SectionPoll()
    {

    }

    public String getName()
    {
        return "Poll Format";
    }


    public static void addPoll(NGSection section, String proposition)
    {
        throw new RuntimeException("Polls are no longer supported, and adding of them even less supported.");
    }

    public static PollRecord[] getPolls(NGSection section)
        throws Exception
    {
        throw new RuntimeException("Polls are no longer supported.");
    }


    public static PollRecord getPollById(NGSection section, String id)
        throws Exception
    {
        throw new RuntimeException("Polls are no longer supported.");
    }

    public void addVote(NGSection section, String id, String who, String choice,
                        String comment, long newTime)
        throws Exception
    {
        throw new RuntimeException("Polls are no longer supported.");
    }

    public void addVote(NGSection section, int pollnum, String who, String choice,
                        String comment, long newTime)
        throws Exception
    {

        throw new RuntimeException("Polls are no longer supported.");
    }

    public void addVote(NGSection section, PollRecord poll, String who, String choice,
                        String comment, long newTime)
        throws Exception
    {
        throw new RuntimeException("Polls are no longer supported.");
    }

    public void removePoll(String pollId, NGSection section)
    {
        throw new RuntimeException("Polls are no longer supported.");
    }

    public void writePlainText(NGSection section, Writer out) throws Exception
    {
        throw new RuntimeException("Polls are no longer supported.");
    }

    /**
    * returns null if there is no poll, or if all of the existing polls have
    * a vote from this user.  If there is a poll that needs an answer, this
    * wil return the string description of the first such poll.  If there are
    * two or more, there is nothing from the latter polls.
    */
    public static String responseRequired(AuthRequest ar, NGSection ngs, UserProfile up)
        throws Exception
    {
        throw new RuntimeException("Polls are no longer supported.");
    }


    /**
    * A poll times out at a particular time.  This can be set to date of now or
    * in the past in order to disable the prompting for answering a poll.
    */
    public static void setPollEndDate(NGSection ngs, int pollNum, long dateOfClosing)
        throws Exception
    {
        throw new RuntimeException("Polls are no longer supported.");
    }



    public String editButtonName()
    {
        return "Polls are no longer supported";
    }

    public boolean isEmpty(NGSection section) throws Exception
    {
        return false;
    }


}
