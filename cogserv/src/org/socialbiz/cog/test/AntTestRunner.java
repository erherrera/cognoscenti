package org.socialbiz.cog.test;
import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.socialbiz.cog.exception.NGException;


 class AntTestRunner {

    private Project cognoscenti;

    public void init(String _buildFile, String _baseDir) throws Exception {
        cognoscenti = new Project();
        try {
            cognoscenti.init();
        } catch (BuildException e) {
            throw new NGException("nugen.exception.task.list.not.loaded",null,e);
        }
        // Set the base directory. If none is given, "." is used.
        if (_baseDir == null)
            _baseDir = new String(".");
        try {
            cognoscenti.setBasedir(_baseDir);
        } catch (BuildException e) {
            throw new NGException("nugen.exception.basedir.not.exist",null,e);
        }
        if (_buildFile == null)
            _buildFile = new String("a.xml");
        try {
            
            ProjectHelper.getProjectHelper().parse(cognoscenti,
                    new File(_baseDir+_buildFile));
        } catch (BuildException e) {
            throw new NGException("nugen.exception.config.file.invalid",new Object[]{_buildFile},e);
        }
    }

    public void runTarget(String _target) throws Exception {
        // Test if the project exists
        if (cognoscenti == null)
            throw new NGException("nugen.exception.no.target.launched",null);
        // If no target is specified, run the default one.
        if (_target == null)
            _target = cognoscenti.getDefaultTarget();
        // Run the target
        try {
            cognoscenti.executeTarget(_target);
        } catch (Exception e) {
            throw new NGException("nugen.exception.cant.execute.target", new Object[]{_target}, e);
        }
    }
    
    public static void main(String[] args) {
        
        try {
            AntTestRunner unitTest = new AntTestRunner();
            unitTest.init("ReadOnlyModeTestsFile.xml", "./My Test Suite/");
            unitTest.runTarget("main");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

}
