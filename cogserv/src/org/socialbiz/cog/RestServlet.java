/*
 * NGLeafServlet.java
 */
package org.socialbiz.cog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet serves up pages using the following URL format:
 * 
 * http://machine:port/{application}/p/{pageid}/leaf.htm
 * 
 * {application} is whatever you install the application to on Tomcat could be
 * multiple levels deep.
 * 
 * "p" is fixed. This is the indicator within the nugen application that says
 * this servlet will be invoked.
 * 
 * {pageid} unique identifier for the page. Obviously depends on the page
 * 
 * leaf.htm is the resource-id of the main page presented as HTML page. This is
 * a fixed resource id for the page. There are other resources as well.
 * 
 * http://machine:port/{application}/p/{pageid}/leaf.xml
 * http://machine:port/{application}/p/{pageid}/process.xml
 * http://machine:port/{application}/p/{pageid}/process.xpdl
 * http://machine:port/{application}/p/{pageid}/process.txt
 * 
 * leaf.xml retrieves the page information as XML process.xml retrieves the
 * process on the page as xml process.xpdl represents that same process as xpdl
 * 
 * There is a subspace for attachments using the name "a" Thus an attachment
 * "MyReport.doc" would be found at:
 * 
 * http://machine:port/{application}/p/{pageid}/a/MyReport.doc
 * 
 */
@SuppressWarnings("serial")
public class RestServlet extends javax.servlet.http.HttpServlet {

    /**
     * This servlet handles REST style requests for XML content
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        AuthRequest ar = AuthRequest.getOrCreate(req, resp);
        try {
            if (!NGPageIndex.isInitialized()) {
                throw new Exception("not initialized", NGLeafServlet.initializationException);
            }

            RestHandler rh = new RestHandler(ar);
            rh.doAuthenticatedGet();
        }
        catch (Exception e) {
            //do something better
        }
        finally {
            NGPageIndex.clearLocksHeldByThisThread();
        }
        ar.logCompletedRequest();
    }
/*
    private void doAuthenticatedGet(AuthRequest ar) {
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
            throw new ProgramLogicError("Path should start with / but instead it is: "
                    + path);
        
        }

    }
    */
}
