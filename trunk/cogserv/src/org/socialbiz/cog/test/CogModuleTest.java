package org.socialbiz.cog.test;

import java.io.File;

import org.socialbiz.cog.Cognoscenti;

public class CogModuleTest {
    
    public CogModuleTest(String path) throws Exception {
        File folder = new File(path);
        if (!folder.exists()) {
            throw new Exception("The test data folder does not exist: "+path);
        }
        Cognoscenti.initializeAll(folder, null);
    }

    public void runTests() {
        TestBuildSite.main(new String[0]);
    }
    
    public static void main(String[] args) {
        try {
            CogModuleTest cmt = new CogModuleTest("c:/ApacheTomcat7/webapps/cog/");
            cmt.runTests();
        }
        catch (Exception e) {
            System.out.print("\n\nFATAL ERROR EXIT PROGRAM:\n");
            e.printStackTrace();
        }
    }
}
