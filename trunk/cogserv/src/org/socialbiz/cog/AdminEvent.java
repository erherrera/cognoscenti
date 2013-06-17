package org.socialbiz.cog;


import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class AdminEvent extends DOMFace {


    public static final String ACCOUNT_CREATED="1";
    public static final String ACCOUNT_DENIED="2";
    public static final String NEW_USER_REGISTRATION="3";



    public AdminEvent(Document doc, Element ele, DOMFace p) {
        super(doc, ele, p);

        //schema migration, unique id was being put in a child tag, but an attribute
        //is more efficient for this sort of thing
        //Schema migration can be removed one year after Jun 2011
        String testCase = getScalar("uniqueId");
        if (testCase!=null && testCase!="" )
        {
            //if we see a child named uniqueId, move that value to the attribute
            setAttribute("id", testCase);

            //remove the child
            setScalar("uniqueId", null);

            //if this file is saved, then these changes are save.  But if not
            //then the conversion happens again until ultimately it is saved.
        }
    }


    /**
    * This is the ID of the object being referred to.
    * If this is a user registration, then this is the key of the user profile.
    * If this is a account creation, then this holds the key to the account.
    */
    public String getObjectId() {
        return getAttribute("id");
    }
    public void setObjectId(String uniqueId) {
        setAttribute("id", uniqueId);
    }

    public void setContext(String context) {
        setAttribute("context", context);
    }
    public String getContext() {
        return getAttribute("context");
    }

    /**
    * Set the user who modifed the record, and the time of modification
    * at the same operation, because both should be set at the same time
    */
    public void setModified(String userId, long time) {
        setAttribute("modUser", userId);
        setAttribute("modTime", Long.toString(time));
    }
    public long getModTime() {
        return safeConvertLong(getAttribute("modTime"));
    }
    public String getModUser() {
        return getAttribute("modUser");
    }

}
