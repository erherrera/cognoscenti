package org.socialbiz.cog;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a role that extacts the assignees of a task, and returns that using
 * an interface of a role.
 *
 * This class is an interface class -- it does not hold any information but it
 * simply reads and write information to/from the GoalRecord itself without
 * caching anything.
 */
public class RoleGoalAssignee extends RoleSpecialBase implements NGRole {
    GoalRecord task;

    RoleGoalAssignee(GoalRecord newTask) {
        task = newTask;
    }

    public String getName() {
        return "Assigned to goal: " + taskName();
    }

    /**
     * A description of the purpose of the role, suitable for display to user.
     */
    public String getDescription() {
        return "Assigned to the goal " + taskName();
    }

    public List<AddressListEntry> getDirectPlayers() throws Exception {
        List<AddressListEntry> list = new ArrayList<AddressListEntry>();
        String assigneeList = getList();
        if (assigneeList == null) {
            return list;
        }
        String[] assignees = UtilityMethods.splitOnDelimiter(assigneeList, ',');
        if (assignees == null) {
            return list;
        }
        for (String assignee : assignees) {
            if (assignee.length() > 0) {
                list.add(new AddressListEntry(assignee));
            }
        }
        return list;
    }

    public void addPlayer(AddressListEntry newMember) throws Exception {
        List<AddressListEntry> current = getDirectPlayers();
        StringBuffer newVal = new StringBuffer();
        for (AddressListEntry one : current) {
            if (one.equals(newMember)) {
                // person is already in the list, so leave without updating
                return;
            }
            newVal.append(one.getUniversalId());
            newVal.append(",");
        }
        newVal.append(newMember.getUniversalId());
        setList(newVal.toString());
    }

    public void removePlayer(AddressListEntry oldMember) throws Exception {
        List<AddressListEntry> current = getDirectPlayers();
        StringBuffer newVal = new StringBuffer();
        boolean needComma = false;
        boolean changed = false;
        for (AddressListEntry one : current) {
            if (oldMember.equals(one)) {
                // person was in the list, this will remove him from it
                changed = true;
            } else {
                if (needComma) {
                    newVal.append(",");
                }
                newVal.append(one.getUniversalId());
                needComma = true;
            }
        }
        if (changed) {
            setList(newVal.toString());
        }
    }

    private String getList() throws Exception {
        return task.getAssigneeCommaSeparatedList();
    }

    private void setList(String newVal) throws Exception {
        task.setAssigneeCommaSeparatedList(newVal);
    }

    protected String taskName() {
        try {
            return task.getSynopsis();
        } catch (Exception e) {
            return "(unspecified synopsis)";
        }
    }

    public void clear() {
        try {
            task.setAssigneeCommaSeparatedList("");
        } catch (Exception e) {
            // this is very unlikely ...
            throw new RuntimeException("Unable to clear the Goal assignees", e);
        }
    }

}
