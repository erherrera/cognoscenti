/*
 * Copyright 2013 Keith D Swenson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors Include: Shamim Quader, Sameer Pradhan, Kumar Raja, Jim Farris,
 * Sandia Yang, CY Chen, Rajiv Onat, Neal Wang, Dennis Tam, Shikha Srivastava,
 * Anamika Chaudhari, Ajay Kakkar, Rajeev Rastogi
 */

package org.socialbiz.cog.spring;

import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.Cognoscenti;
import org.socialbiz.cog.NGPageIndex;
import org.socialbiz.cog.rest.ServerInitializer;
import org.socialbiz.cog.util.SSLPatch;

import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.DispatcherServlet;


/**
* The purpose of this class is to wrap the Spring DispatcherServlet
* object in a way that convert the HTTPResponse parameter into a
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
            if (!Cognoscenti.isInitialized) {
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

            System.out.println("[Web URL: "+requestAddr+"]");
            wrappedServlet.service(ar.req, ar.resp); //must use the versions from AuthRequest
        }
        catch (Exception e) {
            ar.logException("Unable to handle web request to URL ("+requestAddr+")", e);
            throw new ServletException("Unable to handle web request to URL ("+requestAddr+")", e);
        }
        finally{
            NGPageIndex.clearLocksHeldByThisThread();
        }
        ar.logCompletedRequest();
    }

    /**
    * Initializes the entire Cognoscenti system by calling SystemInitializer
    */
    public void init(ServletConfig config)
          throws ServletException
    {
        // first reflect the init method to the wrapped class
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

            //This should initialize EVERYTHING.  Most importantly, it starts a thread
            //that allows subsequence initializations automatically.
            ServerInitializer.startTheServer(config);
        }
        catch (Exception e)
        {
            throw new ServletException("Spring Servlet Wrapper while initializing.", e);
        }

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


}
