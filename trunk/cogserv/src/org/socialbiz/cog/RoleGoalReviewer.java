package org.socialbiz.cog;

/**
 * This is a role that extacts the assignees of a task, and returns that using
 * an interface of a role.
 *
 * This class is an interface class -- it does not hold any information but it
 * simply reads and write information to/from the GoalRecord itself without
 * caching anything.
 */
public class RoleGoalReviewer extends RoleGoalAssignee {

    RoleGoalReviewer(GoalRecord newTask) {
        super(newTask);
    }

    public String getName() {
        return "Reviewer of goal: " + taskName();
    }

    /**
     * A description of the purpose of the role, suitable for display to user.
     */
    public String getDescription() {
        return "Reviewer of goal: " + taskName();
    }

}
