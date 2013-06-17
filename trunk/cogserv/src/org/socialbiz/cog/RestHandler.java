/*
 * NGLeafServlet.java
 */
package org.socialbiz.cog;

import org.socialbiz.cog.exception.ProgramLogicError;

/**

 */

public class RestHandler {

    AuthRequest ar;
    String accountId;
    String projectId;
    String resource;
    NGBook account;
    NGPage ngp;
    
    /**
     * This servlet handles REST style requests for XML content
     */
    public RestHandler(AuthRequest _ar) {
        ar = _ar;
    }

    public void doAuthenticatedGet()  throws Exception {
        
        findResource();
        CaseExchange.sendCaseFormat(ar, ngp);
        
    }
    
    private void findResource()  throws Exception {
        //if this servlet is mapped with /r/*
        //getPathInfo return only the path AFTER the r
        String path = ar.req.getPathInfo();
        // TEST: check to see that the servlet path starts with /
        if (!path.startsWith("/")) {
            throw new ProgramLogicError("Path should start with / but instead it is: "
                            + path);
        }
        
        int slashPos = path.indexOf("/",1);
        if (slashPos<1) {
            throw new ProgramLogicError("could not find a second slash in: " + path);
        }
        accountId = path.substring(1, slashPos);
        account = NGPageIndex.getAccountByKeyOrFail(accountId);
        int nextSlashPos = path.indexOf("/",slashPos+1);
        if (nextSlashPos<0) {
            throw new ProgramLogicError("could not find a third slash in: " + path);
        }
        projectId = path.substring(slashPos+1, nextSlashPos);
        ngp = NGPageIndex.getProjectByKeyOrFail(projectId);
        
        resource = path.substring(nextSlashPos+1);
        if (!"case.xml".equals(resource)) {
            throw new ProgramLogicError("the only resource supported is case.xml, but got: "+resource);
        }
    }
}
