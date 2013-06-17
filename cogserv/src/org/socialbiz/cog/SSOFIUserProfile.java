package org.socialbiz.cog;

import com.fujitsu.loginapplication.interfaces.GlobalId;

/**
 * implementation of the UserProfile interface required by SSOFI
 * as a wrapper on the cognoscenti UserProfile class
 */
public class SSOFIUserProfile implements com.fujitsu.loginapplication.interfaces.UserProfile {

    //This is the Cognoscenti user profile
    private UserProfile user;

    public SSOFIUserProfile(UserProfile _user) {
        user = _user;
    }

    public UserProfile getWrappedUser() {
        return user;
    }

    /**
     * User key is unique identification key of a user profile.
     *
     */
    public String getUserKey() {
        return user.getKey();
    }

    /**
     * Display name can represent the first name
     * or the combination of first name and last which depends upon the implementation class
     *
     */

    public String getDisplayName() {
        return user.getName();
    }

    /**
     * This method checks whether the user profile contains the provided global id.
     * If it contains then return true else return a false.
     *
     */
    public boolean hasID(GlobalId globalId) {
        String id = SSOFIUserManager.processEmailType(globalId.getValue());
        return user.hasAnyId(id);
    }

    /**
     * This method is to add the provided global id to a user profile.
     * Method returns a true when the id is successfully added and false if not. The implementation class
     * should check that if provided global id already exists in user's profile then it should not
     * perform any further operation and return false.
     */
    public boolean addID(GlobalId globalId) {
        String id = SSOFIUserManager.processEmailType(globalId.getValue());
        try {
            user.addId(id);
            return true;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is to remove the provided global id to a user profile.
     * Method returns a true when the id is successfully removed and false if not. The implementation class
     * should check that if provided global id exists in user's profile or not if exists then only it should
     * perform the operation else return false.
     */
    public boolean removeID (GlobalId globalId) {
        String id = SSOFIUserManager.processEmailType(globalId.getValue());
        try {
            user.removeId(id);
            return true;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
