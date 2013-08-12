<%@page errorPage="error.jsp"
%><%@page contentType="text/html;charset=UTF-8" pageEncoding="ISO-8859-1"
%><%@page import="org.socialbiz.cog.AuthRequest"
%><%@page import="org.socialbiz.cog.NGBook"
%><%@page import="org.socialbiz.cog.NGPage"
%><%@page import="org.socialbiz.cog.NGPageIndex"
%><%@page import="org.socialbiz.cog.SectionUtil"
%><%@page import="org.socialbiz.cog.GoalRecord"
%><%@page import="org.socialbiz.cog.UserManager"
%><%
    AuthRequest ar = AuthRequest.getOrCreate(request, response, out);
    ar.assertLoggedIn("Can't use administration functions.");
    boolean createNewSubPage = false;

    String go = ar.reqParam("go");
    String action = ar.reqParam("action");

    String dataFolder = ar.getSystemProperty("dataFolder");

    if (action.equals("Garbage Collect Pages")) {
        deleteMarkedPages();
        action = "Reinitialize Index";
    }

    if (action.equals("Reinitialize Index") || action.equals("Start Email Sender"))
    {
        ServletContext sc = session.getServletContext();

        ar.getSession().flushConfigCache();
        NGPageIndex.initialize(sc);

        //attempt to garbage collect when we have as few objects as possible
        System.gc();

        UserManager.reloadUserProfiles();
    }
    else if (action.equals("Remove Disabled Users"))
    {
        UserManager.removeDisabledUsers();
        UserManager.reloadUserProfiles();
    }
    else if (action.equals("Send Test Email"))
    {
        EmailSender.sendTestEmail();
    }
    else
    {
        throw new Exception ("Unrecognized command: "+action);
    }

    response.sendRedirect(go);

%><%!

public void deleteMarkedPages()
        throws Exception
{
    Vector v = NGPageIndex.getDeletedContainers();
    Enumeration e = v.elements();
    while (e.hasMoreElements())
    {
        NGPageIndex ngpi = (NGPageIndex) e.nextElement();
        File deadFile = new File(ngpi.containerPath);
        if (deadFile.exists())
        {
            deadFile.delete();
        }
    }
}

%><%@ include file="functions.jsp"
%><%

    NGPageIndex.clearLocksHeldByThisThread();
%>
