package org.socialbiz.cog;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
* Holds data for a Recently Used list
*/
public final class RUElement
{

    public long   timestamp;
    public String displayName;
    public String key;

    public RUElement(String nDisplayName, String nKey, long nTimestamp)
    {
        timestamp   = nTimestamp;
        displayName = nDisplayName;
        key         = nKey;
    }


    /**
    * Use this method to maintain a Recently Used list.
    *
    * This will scan the vector to see if the given element is in it.
    * If so, it updates the timestamp to the passed in current time.
    * If not, it adds it to the vector.
    * If the total number of entries is greater than max, then it
    * finds the oldest entry, and kicks it out.
    *
    * Takes the current time as a variable so that an entire interaction can
    * be at a specific time, not just the time that it happens to hit the routine
    * at.  This avoids possible paradoxes when things that happened during one
    * transaction have slightly different times.
    */
    public static void addToRUVector(Vector<RUElement> v, RUElement newElement, long currentTime, int max)
    {
        //check to see if it is already present, and look for oldest
        long oldestTimestamp = currentTime;
        RUElement oldest = null;
        for (RUElement rue : v)
        {
            if (rue.key.equals(newElement.key))
            {
                rue.timestamp = currentTime;
                return;
            }
            //if they are all equal to the current time,
            //then this will at least find one of them
            if (rue.timestamp <= oldestTimestamp)
            {
                oldestTimestamp = rue.timestamp;
                oldest = rue;
            }
        }
        if (v.size()>max)
        {
            v.remove(oldest);
        }
        newElement.timestamp = currentTime;
        v.add(newElement);
    }


    public static void sortByDisplayName(Vector<RUElement> v)
    {
        Collections.sort(v, new RUElementByName());
    }

    private static class RUElementByName implements Comparator<RUElement> {
        RUElementByName() {
        }

        public int compare(RUElement o1, RUElement o2) {
            String n1 = o1.displayName;
            String n2 = o2.displayName;
            return n1.compareTo(n2);
        }

    }

}