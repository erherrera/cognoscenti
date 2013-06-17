package org.socialbiz.cog;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.socialbiz.cog.exception.ProgramLogicError;

/**
 * Each page can have a role that represents the members of the page, and this
 * object represents that as a NGRole object.
 */
public class RoleSpecialBase {

    RoleSpecialBase() {
    }

    public String getName() {
        throw new RuntimeException("SpecialRoleBase does not implement getName");
    }

    public void setName(String name) {
        throw new RuntimeException("The '" + getName()
                + "' role can not have the name set to something else");
    }

    /**
     * A description of the purpose of the role, suitable for display to user.
     */
    public String getDescription() {
        throw new RuntimeException(
                "SpecialRoleBase does not implement getDescription");
    }

    public void setDescription(String desc) {
        throw new RuntimeException("The '" + getName()
                + "' role can not have the description set to something else");
    }

    public List<AddressListEntry> getExpandedPlayers(NGContainer ngp)
            throws Exception {
        List<AddressListEntry> result = new ArrayList<AddressListEntry>();
        CustomRole.expandRoles(result, ngp, getDirectPlayers(), 4);
        return result;
    }

    public List<AddressListEntry> getDirectPlayers() throws Exception {
        throw new RuntimeException(
                "SpecialRoleBase does not implement getDirectPlayers");
    }

    public void addPlayer(AddressListEntry newMember) throws Exception {
        throw new RuntimeException(
                "SpecialRoleBase does not implement addPlayer");
    }

    public void removePlayer(AddressListEntry oldMember) throws Exception {
        throw new RuntimeException(
                "SpecialRoleBase does not implement removePlayer");
    }

    public void clear() {
        throw new RuntimeException(
                "not implemented yet ... and you probably don't want to do this to this role");
    }

    public boolean isExpandedPlayer(UserRef user, NGContainer ngp)
            throws Exception {
        if (user == null) {
            throw new ProgramLogicError(
                    "isExpandedPlayer called with null user object.");
        }
        return CustomRole.isPlayerOfAddressList(user, getExpandedPlayers(ngp));
    }

    public boolean isPlayer(UserRef user) throws Exception {
        return CustomRole.isPlayerOfAddressList(user, getDirectPlayers());
    }

    public String whichIDForUser(UserRef user) throws Exception {
        return CustomRole.whichIDForUserOfAddressList(user, getDirectPlayers());
    }

    /**
     * A descriptive statement to the users about the requirements of becoming a
     * member of this role. This will have whatever the users that set up the
     * role want, but it might include: required skills, required
     * certifications, a description of expected duties, how long to expect to
     * wait for approval, and whatever a person should know before attempting to
     * join the role.
     */
    public String getRequirements() {
        return "getRequirements not implemented on role '" + getName() + "'";
    }

    public void setRequirements(String reqs) {
        // don't do anything
    }

    public void addPlayerIfNotPresent(AddressListEntry member) throws Exception {
        // TODO Auto-generated method stub

    }

    public Vector<AddressListEntry> getMatchedFragment(String frag)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
