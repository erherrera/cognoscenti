package org.socialbiz.cog;

import java.util.ArrayList;
import java.util.List;

import org.socialbiz.cog.exception.NGException;

/**
 * Each page can have a role that represents the members of the page, and this
 * object represents that as a NGRole object.
 */
public class RoleProjectAssignee extends RoleSpecialBase implements NGRole {
    NGPage ngp;

    RoleProjectAssignee(NGPage newPage) {
        ngp = newPage;
    }

    public String getName() {
        return "Assignees";
    }

    /**
     * A description of the purpose of the role, suitable for display to user.
     */
    public String getDescription() {
        return "Assignees of tasks in the project " + ngp.getFullName();
    }

    public List<AddressListEntry> getDirectPlayers() throws Exception {
        List<AddressListEntry> list = new ArrayList<AddressListEntry>();
        for (GoalRecord task : ngp.getAllGoals()) {
            if (task.getState() == BaseRecord.STATE_ACCEPTED) {
                NGRole assignees = task.getAssigneeRole();
                list.addAll(assignees.getDirectPlayers());
            }
        }
        return list;
    }

    public void addPlayer(AddressListEntry newMember) throws Exception {
        throw new NGException(
                "nugen.exception.cant.add.or.remove.role.directly", null);
    }

    public void removePlayer(AddressListEntry oldMember) throws Exception {
        throw new NGException(
                "nugen.exception.cant.add.or.remove.role.directly", null);
    }

}
