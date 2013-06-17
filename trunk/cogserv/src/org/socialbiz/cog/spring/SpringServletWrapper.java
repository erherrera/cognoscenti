package org.socialbiz.cog.spring;

import org.socialbiz.cog.AuthDummy;
import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.EmailListener;
import org.socialbiz.cog.EmailSender;
import org.socialbiz.cog.MicroProfileMgr;
import org.socialbiz.cog.NGPageIndex;
import org.socialbiz.cog.SendEmailTimerTask;
import org.socialbiz.cog.util.SSLPatch;
import java.net.URLEncoder;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;


/**
* The purpose of this class is to wrap the Spring DispatcherServlet
* object in a way that conver the HTTPResponse parameter into a
* HttpServletResponseWithoutBug object, in order to avoid problems
* with getting the output stream more than one time.
*
* See HttpServletResponseWithoutBug for more details.
*/
@SuppressWarnings("serial")
public class SpringServletWrapper extends HttpServlet

{
    private DispatcherServlet wrappedServlet;

    //there is only one of these created, and this is a pointer to it
    private static SpringServletWrapper instance;
    private static boolean isInitializedOthers = false;

    public SpringServletWrapper()
    {
        wrappedServlet = new DispatcherServlet();
    }

    /**
    * According to the java doc, all the http requests come through this method
    * and later get redirected to the other requests by type: GET, PUT, etc.
    * All we want to do is to wrap the response object, and this should
    * do it for all request types.
    */
    protected void service(HttpServletRequest req,
                       HttpServletResponse resp)
                throws ServletException,
                       java.io.IOException
    {
        AuthRequest ar = AuthRequest.getOrCreate(req, resp);
        String requestAddr = "unknown";

        try{
            requestAddr = ar.getCompleteURL();

            //test for initialized, and if not redirect to config page
            if (!NGPageIndex.isInitialized()) {
                try {
                    String configDest = ar.retPath + "init/config.htm?go="
                            +URLEncoder.encode(requestAddr,"UTF-8");
                    resp.sendRedirect(configDest);
                    return;
                }
                catch (Exception e) {
                    throw new ServletException("Error while attempting to redirect to the configuration page", e);
                }
            }

            if(!isInitializedOthers){
                initOthers();
            }

            System.out.println("[Web URL: "+requestAddr+"]");
            wrappedServlet.service(ar.req, ar.resp); //must use the versions from AuthRequest
        }
        catch (Exception e) {
            ar.logException("Unable to handle web request to URL ("+requestAddr+")", e);
            throw new ServletException("Unable to handle web request to URL ("+requestAddr+")", e);
        }
        finally{
            NGPageIndex.clearAllLock();
        }
        ar.logCompletedRequest();
    }

    /**
    * must reflect the init method to the wrapped class
    */
    public void init(ServletConfig config)
          throws ServletException
    {

        wrappedServlet.init(config);
        try
        {
            //by default the Java SSL support will fail if the server does not have a
            //valid certificate, but this prevents the ability to read data privately
            //from servers that do not have a certificate.  For password protection and
            //for OpenID validation, we need to be able to read data from servers, over
            //an SSL connection, even if the server does not have a certificate.
            //This diables the validation and prevents the exception from being thrown
            //at any time after this point in this VM.
            SSLPatch.disableSSLCertValidation();

            ServletContext sc = config.getServletContext();
            NGPageIndex.initialize(sc);
            AuthDummy.initializeDummyRequest( config );
            EmailSender.initSender(sc, wrappedServlet.getWebApplicationContext());
            EmailListener.initListener();
        }
        catch (Exception e)
        {
            throw new ServletException("Spring Servlet Wrapper while initializing.", e);
        }
        initOthers();
        //store a pointer to this object AFTER it is initialized
        instance = this;
    }


    /**
    * Can generate any page in the system.
    * Page is generated to the Writer in the AuthRequest object, and according
    * the URL parameters in the AuthRequest object.
    *
    * For static site generation and testing, create a new nested auth request
    * so the original request will not be disturbed.
    * The nested auth request object takes a relative URL to that page desired.
    * It also takes a Writer to write the output to.
    */
    public static void generatePage(AuthRequest ar)
        throws Exception
    {
        instance.wrappedServlet.service(ar.req, ar.resp);
    }


    public static void initOthers()//throws Exception
    {
        try
        {
            MicroProfileMgr.loadMicroProfilesInMemory();
            SendEmailTimerTask.initEmailSender();
            isInitializedOthers = true;
        }catch (Exception e)
        {
            isInitializedOthers = false;
            //throw new ServletException("Spring Servlet Wrapper while loading micro-profiles or SendEmailTimer.", e);
        }
    }

}
