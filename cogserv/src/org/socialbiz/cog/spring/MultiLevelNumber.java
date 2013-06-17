package org.socialbiz.cog.spring;

/**
* This is a class that produces multi level numbers like this:
*
*  1.               L0
*  2.               L0
*  2.1              L1
*  2.2              L1
*  3.               L0
*  3.1              L1
*  3.1.1            L2
*  3.1.2            L2
* etc.
*
* Each request is made by asking for the next number, AT A PARTICULAR LEVEL.
* The numbers on the right represent the level of number that was asked for.
* The first call is for a number at level 0, then next at level 0 again.
* The third request is for a number at level 1.  In this case it leaves the
* top level 0 number the same, but increments the next level, and returns a
* two digit number.
*
* All you need to know is the level (depth) of the thing being numbered, and
* this will return the correct next number for that level.
*/

class MultiLevelNumber
{
    int[] tracker = new int[20];

    public MultiLevelNumber() {
        for (int i=0; i<20; i++) {
            tracker[i] = 0;
        }
    }


    /**
    * Top level number is level 0.
    * Specify the level, and a combined number is returned
    */
    public String nextAtLevel(int level) {
        if (level<0 || level>=20) {
            throw new RuntimeException("nextAtLevel can not handle more than level 19");
        }

        //increment the number at the specified level
        tracker[level]++;

        //zero all the numbers for higher levels
        for (int i=level+1; i<20; i++) {
            tracker[i] = 0;
        }

        //now generate the string value
        StringBuffer seq = new StringBuffer();
        for (int i=0; i<=level; i++) {
            seq.append(Integer.toString(tracker[i]));
            seq.append(".");
        }
        return seq.toString();
    }
}
