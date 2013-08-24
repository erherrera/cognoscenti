/*
 * NGLeafServlet.java
 */
package org.socialbiz.cog;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socialbiz.cog.exception.ProgramLogicError;
import org.workcast.streams.TemplateStreamer;
import org.workcast.streams.TemplateTokenRetriever;

/**
 * The emergency config servlet is used ONLY when the server is unable
 * to initialize itself.
 *
 * When the server starts up, it reads configuration files, and other files
 * as necessary, in order to start up.  It is possible for files to be
 * corrupted or otherwise changed so that it is impossible for the server
 * to start up.  The server is responsible not only to read the configuration
 * settings, but to test the values that are necessary for basic running.
 *
 * If it detects a problem, then the server goes into the "uninitialized"
 * state.  When it is uninitialized, then any request for a page MUST
 * redirect to this emergency config page.
 *
 * The emergency config page allows for particular settings to be adjusted
 * in order to get the server running again.
 *
 * It is important that it test ONLY the situation that absolutely must
 * be correct for the server to operate.  It is naturally possible that some
 * aspects of the server might not be available, and it is assumed that
 * that capability is controlled by some administrator function or administrator
 * user interface.  This emergeny config page is used when the server fails
 * to initialize itself, and can run without any assumptions about the
 * rest of the server running.
 *
 * SECURITY CONCERNS: Clearly we do not want unsecured access to be able to
 * change the configuration.  It might be possible to make the server point
 * at a different directory, and to serve up files it otherwise would
 * protect.  The security concerns are addressed because the emergency config
 * page is ONLY available when the server fails to initialize itself.
 * Any attempt to access this page when the server is initialized should
 * cause a redirect back to the original page, or an error report.
 * Any hacker who could cause the server to be mis-configured and fail to start,
 * would gain no additional capability from this page -- in other words
 * to cause the server to fail to start would require a far more intimate
 * access to the server than this emergency config form gives.
 *
 * Most people will see this form only the very first time a server starts,
 * and that is because the server is NOT YET configured, and this page allows
 * the user to do that.   Once configured, however, this page should never appear.
 *
 * IMPLEMENTATION: the page is very very simple: no graphics or colors, just the
 * most basic form possible.  This is to ensure that the page works without anything
 * else in the server working.
 *
 * TIME CONSIDERATIONS: there is a possibility that the server is hit with a number
 * of requests at the moment it is starting up.  This might lead people to get
 * the configuration page simply because they hit the server at exactly the
 * initialization time.   To guard against this, the requests should be delayed
 * and the initialization variable checked again after a delay.  Also, the browser
 * should be redirected to the config page, and if by the time the request for
 * the config page is made, the server is already initialized, then the browser
 * should be redirected back to the original page.
 *
 * USAGE: This servlet should be mapped to /init/*
 * All other servlets should test NGLeafIndex.isInitialized(), and if false
 * redirect to   /init/config.htm?go=<return address>
 *
 */
@SuppressWarnings("serial")
public class EmergencyConfigServlet extends javax.servlet.http.HttpServlet {

        //
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {

            String go = req.getParameter("go");

            if (go==null) {
                //accessing without a go parameter might be an attempt to crawl all the pages
                //and this is simply an error.
                throw new ProgramLogicError("Emergency config page needs a 'go' parameter, none found.");
            }

            if (NGPageIndex.isInitialized()) {

                //this is the case that possibly the server was just now initialized, and now it has become
                //initialized since the browser was directed here, so redirect back to where you came from.
                //It is also possible that someone bookmarked the config page, and attempting to reach it
                //again, but the server is initialized now, and we can not allow access to config page.
                resp.sendRedirect(go);
                return;
            }

            resp = new HttpServletResponseWithoutBug(resp);

            displayConfigPage(req, resp);

        }
        catch (Exception e) {
            handleException(e, req, resp);
        }
    }



    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {

            String go = req.getParameter("go");

            if (go==null) {
                //accessing without a go parameter might be an attempt to crawl all the pages
                //and this is simply an error.
                throw new ProgramLogicError("Emergency config page needs a 'go' parameter, none found.");
            }

            resp = new HttpServletResponseWithoutBug(resp);

            if (!NGPageIndex.isInitialized()) {

                //this is the case that possibly the server was just now initialized, and now it has become
                //initialized since the browser was directed here, so redirect back to where you came from.
                //Very important ... do NOT save any configuration information when server initialized.
                handleFormPost(req, resp);
            }

            resp.sendRedirect(go);

        } catch (Exception e) {
            handleException(e, req, resp);
        }
    }

    public void doPut(HttpServletRequest req, HttpServletResponse resp) {
        handleException(new Exception("Put operation not allowed on the emergency config servlet,"), req, resp);
    }

    public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        handleException(new Exception("Delete operation not allowed on the emergency config servlet,"), req, resp);
    }

    public void init(ServletConfig config)
          throws ServletException {
        //no initialization necessary
    }

    private void handleException(Exception e, HttpServletRequest req, HttpServletResponse resp) {
        try {
            Writer out = resp.getWriter();
            resp.setContentType("text/html;charset=UTF-8");
            out.write("<html><body><ul><li>Exception: ");
            writeHtml(out, e.toString());
            out.write("</li></ul>\n");
            out.write("<hr/>\n");
            out.write("<a href=\"../main.jsp\" title=\"Access the main page\">Main</a>\n");
            out.write("<hr/>\n<pre>");
            e.printStackTrace(new PrintWriter(new HTMLWriter(out)));
            out.write("</pre></body></html>\n");
            out.flush();
        } catch (Exception eeeee) {
            // nothing we can do here...
        }
    }


    /**
    * This method needs to display the page without requiring any other service from the system
    * It does assume that the ConfigFile class is working properly.
    * There should be no fancy embellishments to the page, just very plain config page.
    */
    private void displayConfigPage(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        Writer out = resp.getWriter();
        resp.setContentType("text/html;charset=UTF-8");

        String go = req.getParameter("go");
        ConfigTokenRetriever ctr = new ConfigTokenRetriever(go);

        File templateFile = ConfigFile.getFileFromRoot("init/InitErrorDisplay.htm");
        TemplateStreamer.streamTemplate(out, templateFile, "UTF-8", ctr);

/**
        //we know that the server did not initialize, and here is why
        Exception failure = NGPageIndex.initFailureException();
        String initFailure =failure.toString();
        StringBuffer mainMessage = new StringBuffer();

        String dataFolder = ConfigFile.getProperty("dataFolder");
        String attachFolder = ConfigFile.getProperty("attachFolder");
        String userFolder = ConfigFile.getProperty("userFolder");
        String pageNotification  = getOrDefault(req,"pageNotification");
        String PageNotifChecked = "";
        if (pageNotification!=null && "true".equals(pageNotification))
        {
            PageNotifChecked = " checked=\"checked\"";
        }

        String dataFolderMsg = getFolderMessage("dataFolder", dataFolder);
        String attachFolderMsg = getFolderMessage("attachFolder", attachFolder);
        String userFolderMsg = getFolderMessage("userFolder", userFolder);

        if (mainMessage.length() == 0) {
            mainMessage.append("This page displayed for unknown reason - Program logic error.");
        }

        out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n");
        out.write("<html>\n");
        out.write("<head>\n");
        out.write("<title>Initial Configuration Page</title>\n");
        out.write("</head>\n");
        out.write("<body>\n");
        out.write("<h3>Initial Configuration Page</h3>\n");
        out.write("<table border=\"0\" cellpadding=\"6\" cellspacing=\"0\">\n");
        out.write("<tr>\n");
        out.write("    <td alight=\"left\">Server Initialization Message<br/>\n");
        out.write("    <font class=\"arialfonts\"> ");
        writeHtml(out, initFailure);
        out.write(" </font></td>\n");
        out.write("</tr>\n");
        out.write("</table>\n");
        out.write("\n");
        out.write("<table border=\"0\" cellpadding=\"6\" cellspacing=\"0\">\n");
        out.write("<form action=\"configAction.htm\" method=\"post\" name=\"loginForm\">\n");
        out.write("<input type=\"hidden\" name=\"encodingGuard\" value=\"");
        writeHtml(out, "\u6771\u4eac");
        out.write("\"/>\n");
        out.write("<input type=\"hidden\" name=\"go\" value=\"");
        writeHtml(out, go);
        out.write("\">\n");
        out.write("\n");
        out.write("<tr>\n");
        out.write("    <td><font class=\"arialfonts\">Data Folder</font></td>\n");
        out.write("    <td><b>&nbsp;:&nbsp;</b></td>\n");
        out.write("    <td><input type=\"text\" name=\"dataFolder\" value=\"");
        writeHtml(out, dataFolder);
        out.write("\" size=\"50\"></td>\n");
        out.write("</tr>\n");
        out.write("<tr>\n");
        out.write("    <td colspan=\"3\"><font color=\"red\">");
        writeHtml(out, dataFolderMsg);
        out.write("</font></td>\n");
        out.write("</tr>\n");
        out.write("<tr>\n");
        out.write("    <td><font class=\"arialfonts\">Attachment Folder</font></td>\n");
        out.write("    <td><b>&nbsp;:&nbsp;</b></td>\n");
        out.write("    <td><input type=\"text\" name=\"attachFolder\" value=\"");
        writeHtml(out, attachFolder);
        out.write("\" size=\"50\"></td>\n");
        out.write("</tr>\n");
        out.write("<tr>\n");
        out.write("    <td colspan=\"3\"><font color=\"red\">");
        writeHtml(out, attachFolderMsg);
        out.write("</font></td>\n");
        out.write("</tr>\n");
        out.write("<tr>\n");
        out.write("    <td><font class=\"arialfonts\">User Folder</font></td>\n");
        out.write("    <td><b>&nbsp;:&nbsp;</b></td>\n");
        out.write("    <td><input type=\"text\" name=\"userFolder\" value=\"");
        writeHtml(out, userFolder);
        out.write("\" size=\"50\"></td>\n");
        out.write("</tr>\n");
        out.write("<tr>\n");
        out.write("    <td colspan=\"3\"><font color=\"red\">");
        writeHtml(out, userFolderMsg);
        out.write("</font></td>\n");
        out.write("</tr>\n");
        out.write("<tr>\n");
        out.write("    <td><font class=\"arialfonts\">Auto Refresh</font></td>\n");
        out.write("    <td><b>&nbsp;:&nbsp;</b></td>\n");
        out.write("    <td><input type=\"checkbox\" name=\"pageNotification\" value=\"true\"");
        out.write(PageNotifChecked);
        out.write("></td>\n");
        out.write("</tr>\n");

        out.write("<tr>\n");
        out.write("    <td colspan=\"3\" align=\"center\">\n");
        out.write("<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\">\n");
        out.write("<tr valign=\"center\" align=\"center\"><td class=core-button-border>\n");
        out.write("<input class=\"core-button\" name=\"option\" type=\"submit\" value=\"Reinitialize Without Changing Configuration\">\n");
        out.write("<input class=\"core-button\" name=\"option\" type=\"submit\" value=\"Update Configuration\">\n");
        out.write("</td>\n");
        out.write("</tr>\n");
        out.write("</table>\n");
        out.write("\n");
        out.write("</form>\n");
        out.write("\n");
        out.write("</td></tr>\n");
        out.write("</table>\n");
        if (failure != null) {
            out.write("<pre>\n");
            failure.printStackTrace(new PrintWriter(new HTMLWriter(out)));
            out.write("</pre>\n");
        }

        out.write("</body>\n");
        out.write("</html>\n");
        ***/
        out.flush();
    }


    public static String getOrDefault(HttpServletRequest req, String paramName) throws Exception
    {
        String val = req.getParameter(paramName);
        if (val==null) {
            val = ConfigFile.getProperty(paramName);
        }
        return val;
    }


    private String getFolderMessage(String folderName, String folderValue)
    {
        if (folderValue == null || folderValue.length()== 0)
        {
            return folderName + " setting has not been set yet.  Please set to the path of that folder.";
        }

        File testFile = new File(folderValue);
        if (!testFile.exists()) {
            return folderName+" ("+folderValue +") does not exist.";
        }

        if (!testFile.isDirectory()) {
            return folderName+" ("+folderValue +") is a file not a directory.";
        }
        return "";
    }

    private static synchronized void handleFormPost(HttpServletRequest req, HttpServletResponse resp) throws Exception
    {
        // IMPORTANT:
        // This method updates the configuration.   It is designed to work when the system is not properly
        // configured.  This means that we can not check authentication, and we can not assure that this is
        // being done by an authenticated user.  THEREFOR it is critical that this method works ONLY when
        // the configuration is broken.  In normal usage, the configuration is not broken, and no hacker,
        // can break in.   Actually, it works only when the server is not initialized, which is presumably
        // because the configuration is incorrect.

        if (NGPageIndex.isInitialized()) {
            //if server was initialized while waiting to get the synchronize lock
            //then just return without updating anything
            return;
        }
        String option = req.getParameter("option");
        if (option == null)
        {
            throw new ProgramLogicError("Post to config servlet must have an option parameter");
        }

        String dataFolder    = getOrDefault(req,"dataFolder");
        String attachFolder  = getOrDefault(req,"attachFolder");
        String userFolder    = getOrDefault(req,"userFolder");
        String pageNotification  = getOrDefault(req,"pageNotification");
        if (pageNotification==null || !"true".equals(pageNotification))
        {
            pageNotification = "false";
        }


        // make sure to append "/" at the end of the path.
        dataFolder = endStringWithBS(dataFolder);
        attachFolder = endStringWithBS(attachFolder);

        if (option.startsWith("Update"))
        {
            ConfigFile.setProperty("dataFolder",    dataFolder);
            ConfigFile.setProperty("attachFolder",  attachFolder);
            ConfigFile.setProperty("userFolder",    userFolder);
            ConfigFile.setProperty("pageNotification",pageNotification);

            //these settings no longer needed so clear them out in case any are left around
            ConfigFile.setProperty("cvsEnabled",    null);
            ConfigFile.setProperty("cvsConnectStr", null);
            ConfigFile.setProperty("cvsPwd",        null);

            ConfigFile.save();
        }

        resp.setContentType("text/html;charset=UTF-8");

        //initialize the data folder file as necessary
        File dataFolderFile = new File(dataFolder);
        if (dataFolderFile.exists())
        {
            //initialize important variables and index
            NGPageIndex.initIndex();
        }

        //reinitialize the server with these settings
        ServletContext sc = req.getSession().getServletContext();
        NGPageIndex.initialize(sc);
    }


    public static String endStringWithBS(String str)
    {
        if (str == null || str.length() == 0) {
            return str;
        }

        str = str.replace('\\', '/');

        if (str.length() > 0  && str.endsWith("/") == false) {
            str = str + "/";
        }

        return str;
    }



    public static void writeHtml(Writer w, String t)
        throws Exception
    {
        if (t==null)
        {
            return;  //treat it like an empty string, don't write "null"
        }
        for (int i=0; i<t.length(); i++)
        {
            char c = t.charAt(i);
            switch (c)
            {
                case '&':
                    w.write("&amp;");
                    continue;
                case '<':
                    w.write("&lt;");
                    continue;
                case '>':
                    w.write("&gt;");
                    continue;
                case '"':
                    w.write("&quot;");
                    continue;
                default:
                    w.write(c);
                    continue;
            }
        }
    }


    private class ConfigTokenRetriever implements TemplateTokenRetriever {
        String go;

        ConfigTokenRetriever(String _go) {
            go = _go;
        }

        @Override
        public void writeTokenValue(Writer out, String tokenName) throws Exception {

            Exception failure = NGPageIndex.initFailureException();

            if ("exceptionMsg".equals(tokenName)) {
                writeHtml(out, failure.toString());
            }
            else if ("exceptionTrace".equals(tokenName)) {
                failure.printStackTrace(new PrintWriter(out));
            }
            else if ("go".equals(tokenName)) {
                writeHtml(out, go);
            }
            else if (tokenName.startsWith("param ")) {
                //must be "param name" where name is the name of a param
                String paramName = tokenName.substring(6).trim();
                String paramValue = ConfigFile.getProperty(paramName);
                if (paramValue==null) {
                    out.write("<i>null value found</i>");
                }
                else if (paramValue.length()==0) {
                    out.write("<i>zero length value found</i>");
                }
                else {
                    writeHtml(out, paramValue);
                }
            }
            else if (tokenName.startsWith("pathtest ")) {
                //must be "param name" where name is the name of a config parameter
                String paramName = tokenName.substring(9).trim();
                String paramValue = ConfigFile.getProperty(paramName);
                if (paramValue==null) {
                    out.write("<img src=\"../assets/images/redcircle.jpg\" width=\"10px\" height=\"10px\"><i>null value found</i>");
                }
                else if (paramValue.length()==0) {
                    out.write("<img src=\"../assets/images/redcircle.jpg\" width=\"10px\" height=\"10px\"><i>zero length value found</i>");
                }
                else {
                    File thisPath = new File(paramValue);
                    if (!thisPath.exists()) {
                        out.write("<img src=\"../assets/images/redcircle.jpg\" width=\"10px\" height=\"10px\">");
                        writeHtml(out, paramValue);
                        out.write("<br/><i>This path does not exist on this server.</i>");
                    }
                    else {
                        out.write("<img src=\"../assets/images/greencircle.jpg\" width=\"10px\" height=\"10px\">");
                        writeHtml(out, paramValue);
                        out.write("<br/><i>This path exists on this server.</i>");
                    }
                }
            }
            else {
                out.write("##(");
                writeHtml(out, tokenName);
                out.write(")##");
            }
        }
    }

}
