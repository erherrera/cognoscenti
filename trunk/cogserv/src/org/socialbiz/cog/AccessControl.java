package org.socialbiz.cog;

import java.net.URLEncoder;
import java.util.HashMap;

import org.socialbiz.cog.spring.AccountRequest;

public class AccessControl {

    //TODO: this is not a good idea, to cache the entire user page for every user
    //that touches the system.  This should be cleared occasionally or store a
    //smaller amount of data and should only cache a specified number of users.
    static HashMap<String, UserPage> userPageMap = new HashMap<String, UserPage>();

    /**
    * When the web request is trying to access a particular document, this will
    * say whether that document should be accessed, based on logged in user being
    * a member of the project, or whether a special session permission was granted.
    *
    * For documents, the localIdMagicNumber must be in the "mndoc" parameter
    * to get special session status.
    *
    * Side Effect: if the right magic number is found in URL, the session is marked
    * and this special access capability will persist as long as the session does.
    */
    public static boolean canAccessDoc(AuthRequest ar, NGContainer ngc, AttachmentRecord attachRec)
        throws Exception {
        //first, anyone can access a public document
        if (attachRec.getVisibility() == SectionDef.PUBLIC_ACCESS) {
            return true;
        }

        //then, if user is logged in, and is a member, then can access
        if (ar.isLoggedIn()) {
            UserProfile user = ar.getUserProfile();
            if (user!=null && ngc.primaryOrSecondaryPermission(user)) {
                return true;
            }
        }

        //then, check to see if there is any special condition in session
        String resourceId = "doc:"+ngc.getKey()+":"+attachRec.getId();
        if (ar.hasSpecialSessionAccess(resourceId)) {
            return true;
        }

        //now, check the query parameters, and if appropriate, set up the special access
        //url must have "mndoc"  (magic number for doc)
        String mndoc = ar.defParam("mndoc", null);
        if (mndoc != null) {
            String expectedMN = ngc.emailDependentMagicNumber(resourceId);
            if (expectedMN.equals(mndoc)) {
                ar.setSpecialSessionAccess(resourceId);
                return true;
            }
        }

        return false;
    }

    public static String getAccessDocParams(NGContainer ngc, AttachmentRecord attachRec) throws Exception{
        String accessDocParam = "mndoc=";
        String resourceId = "doc:"+ngc.getKey()+":"+attachRec.getId();
        String encodedValue = URLEncoder.encode(ngc.emailDependentMagicNumber(resourceId), "UTF-8");
        accessDocParam += encodedValue;
        return accessDocParam;
    }

    public static boolean canAccessReminder(AuthRequest ar, NGContainer ngc, ReminderRecord reminderRecord)
    throws Exception {

        //then, if user is logged in, and is a member, then can access
        if (ar.isLoggedIn()) {
            UserProfile user = ar.getUserProfile();
            if (user!=null && ngc.primaryOrSecondaryPermission(user)) {
                return true;
            }
        }

        //then, check to see if there is any special condition in session
        String resourceId = "reminder:"+ngc.getKey()+":"+reminderRecord.getId();
        if (ar.hasSpecialSessionAccess(resourceId)) {
            return true;
        }

        //now, check the query parameters, and if appropriate, set up the special access
        //url must have "mnremider"  (magic number for remider)
        String mndoc = ar.defParam("mnremider", null);
        if (mndoc != null) {
            String expectedMN = ngc.emailDependentMagicNumber(resourceId);
            if (expectedMN.equals(mndoc)) {
                ar.setSpecialSessionAccess(resourceId);
                return true;
            }
        }

        return false;
    }

    public static String getAccessReminderParams(NGContainer ngc, ReminderRecord reminderRecord) throws Exception{
        String resourceId = "reminder:"+ngc.getKey()+":"+reminderRecord.getId();
        String encodedValue = URLEncoder.encode(ngc.emailDependentMagicNumber(resourceId), "UTF-8");
        return "mnremider=" + encodedValue;
    }

    public static boolean canAccessGoal(AuthRequest ar, NGContainer ngc, GoalRecord gr)
    throws Exception {

        //then, if user is logged in, and is a member, then can access
        if (ar.isLoggedIn()) {
            UserProfile user = ar.getUserProfile();
            if (user!=null && ngc.primaryOrSecondaryPermission(user)) {
                return true;
            }
        }

        //then, check to see if there is any special condition in session
        String resourceId = "goal:"+ngc.getKey()+":"+gr.getId();
        if (ar.hasSpecialSessionAccess(resourceId)) {
            return true;
        }

        //now, check the query parameters, and if apprpriate, set up the special access
        //url must have "mntask"  (magic number for task)
        String mndoc = ar.defParam("mntask", null);
        if (mndoc != null) {
            String expectedMN = ngc.emailDependentMagicNumber(resourceId);
            if (expectedMN.equals(mndoc)) {
                ar.setSpecialSessionAccess(resourceId);
                return true;
            }
        }

        return false;
    }

    public static String getAccessTaskParams(NGContainer ngc, GoalRecord gr) throws Exception{
        String accessDocParam = "mntask=";
        String resourceId = "goal:"+ngc.getKey()+":"+gr.getId();
        String encodedValue = URLEncoder.encode(ngc.emailDependentMagicNumber(resourceId), "UTF-8");
        accessDocParam += encodedValue;
        return accessDocParam;
    }

    public static boolean canAccessNote(AuthRequest ar, NGContainer ngc, NoteRecord noteRec)
    throws Exception {
        //first, anyone can access a public note
        if (noteRec.getVisibility() == SectionDef.PUBLIC_ACCESS) {
            return true;
        }
        //then, if user is logged in, and is a member, then can access
        if (ar.isLoggedIn()) {
            UserProfile user = ar.getUserProfile();
            if (user!=null && ngc.primaryOrSecondaryPermission(user)) {
                return true;
            }
        }

        //then, check to see if there is any special condition in session
        String resourceId = "note:"+ngc.getKey()+":"+noteRec.getId();
        if (ar.hasSpecialSessionAccess(resourceId)) {
            return true;
        }

        //now, check the query parameters, and if appropriate, set up the special access
        //url must have "mnnote"  (magic number for note)
        String mndoc = ar.defParam("mnnote", null);
        if (mndoc != null) {
            String expectedMN = ngc.emailDependentMagicNumber(resourceId);
            if (expectedMN.equals(mndoc)) {
                ar.setSpecialSessionAccess(resourceId);
                return true;
            }
        }

        return false;
    }

    public static String getAccessNoteParams(NGContainer ngc, NoteRecord noteRec) throws Exception{
        String accessDocParam = "mnnote=";
        String resourceId = "note:"+ngc.getKey()+":"+noteRec.getId();
        String encodedValue = URLEncoder.encode(ngc.emailDependentMagicNumber(resourceId), "UTF-8");
        accessDocParam += encodedValue;
        return accessDocParam;
    }

    public static boolean canAccessRoleRequest(AuthRequest ar, NGContainer ngc, RoleRequestRecord roleRequestRecord)
    throws Exception {

        //then, if user is logged in, and is a member, then can access
        if (ar.isLoggedIn()) {
            UserProfile user = ar.getUserProfile();
            if (user!=null && ngc.primaryOrSecondaryPermission(user)) {
                return true;
            }
        }

        //then, check to see if there is any special condition in session
        String resourceId = "rolerequest:"+ngc.getKey()+":"+roleRequestRecord.getRequestId();
        if (ar.hasSpecialSessionAccess(resourceId)) {
            return true;
        }

        //now, check the query parameters, and if appropriate, set up the special access
        //url must have "mnrolerequest"  (magic number for role request)
        String mndoc = ar.defParam("mnrolerequest", null);
        if (mndoc != null) {
            String expectedMN = ngc.emailDependentMagicNumber(resourceId);
            if (expectedMN.equals(mndoc)) {
                ar.setSpecialSessionAccess(resourceId);
                return true;
            }
        }

        return false;
    }

    public static String getAccessRoleRequestParams(NGContainer ngc, RoleRequestRecord roleRequestRecord) throws Exception{
        String accessDocParam = "mnrolerequest=";
        String resourceId = "rolerequest:"+ngc.getKey()+":"+roleRequestRecord.getRequestId();
        String encodedValue = URLEncoder.encode(ngc.emailDependentMagicNumber(resourceId), "UTF-8");
        accessDocParam += encodedValue;
        return accessDocParam;
    }

    public static boolean canAccessAccountRequest(AuthRequest ar, String userKey, AccountRequest accountDetails)
    throws Exception {

        //then, if user is logged in, and is a super admin, then can always access
        if (ar.isLoggedIn()) {
            if (ar.isSuperAdmin()) {
                return true;
            }
        }

        //then, check to see if there is any special condition in session
        String resourceId = "accountrequest:"+userKey+":"+accountDetails.getRequestId();
        if (ar.hasSpecialSessionAccess(resourceId)) {
            return true;
        }

        //now, check the query parameters, and if appropriate, set up the special access
        //url must have "mnaccountrequest"  (magic number for account request)
        String mndoc = ar.defParam("mnaccountrequest", null);
        if (mndoc == null) {
            //no magic number, no luck
            return false;
        }
        if(userPageMap.containsKey(userKey)){
            UserPage userPage = userPageMap.get(userKey);
            String expectedMN = userPage.emailDependentMagicNumber(resourceId);
            if (expectedMN.equals(mndoc)) {
                ar.setSpecialSessionAccess(resourceId);
                return true;
            }
        }

        return false;
    }

    public static String getAccessAccountRequestParams(String userKey, AccountRequest accountDetails) throws Exception{
        String accessDocParam = "mnaccountrequest=";
        UserPage userPage = null;
        if(userPageMap.containsKey(userKey)){
            userPage = userPageMap.get(userKey);
        }else{
            userPage = UserPage.findOrCreateUserPage(userKey);
            userPageMap.put(userKey, userPage);
        }
        String resourceId = "accountrequest:"+userPage.getKey()+":"+accountDetails.getRequestId();
        String encodedValue = URLEncoder.encode(userPage.emailDependentMagicNumber(resourceId), "UTF-8");
        accessDocParam += encodedValue;
        return accessDocParam;
    }

}
