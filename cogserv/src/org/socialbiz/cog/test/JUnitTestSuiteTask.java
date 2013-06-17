
package org.socialbiz.cog.test;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;


public class JUnitTestSuiteTask extends Task {

    /*
     * (non-Javadoc)
     *
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {

        try {
            Project project = this.getProject();


            ConTestEnvironment
                    .setGlobal("basedir", project.getProperty("basedir"));

            // set admin user name and password in global variable
            // which can be used later on when test case param does not have
            // these values
            ConTestEnvironment.setGlobal("adminuser", project
                    .getProperty("adminuser"));
            ConTestEnvironment.setGlobal("adminpassword", project
                    .getProperty("adminpassword"));

            // set user name and password in global variable
            // which can be used later on when test case param does not have
            // these values
            ConTestEnvironment.setGlobal("cognosecntiuser", project
                    .getProperty("user"));
            ConTestEnvironment.setGlobal("password", project
                    .getProperty("userpassword"));

        } catch (Exception ex) {
            throw new BuildException("property not set",ex);

        }

    }

    public void setProperty(String property) {
    }



}