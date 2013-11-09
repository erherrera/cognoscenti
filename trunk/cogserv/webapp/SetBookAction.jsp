<%@page errorPage="error.jsp"
%><%@page contentType="text/html;charset=UTF-8" pageEncoding="ISO-8859-1"
%><%@page import="org.socialbiz.cog.AuthRequest"
%><%@page import="org.socialbiz.cog.NGBook"
%><%@page import="org.socialbiz.cog.NGPage"
%><%@page import="org.socialbiz.cog.NGPageIndex"
%><%@page import="org.socialbiz.cog.NGSection"
%><%@page import="org.socialbiz.cog.SectionDef"
%><%@page import="org.socialbiz.cog.SectionFormat"
%><%@page import="org.socialbiz.cog.SectionUtil"
%><%@page import="java.net.URLEncoder"
%><%@page import="java.util.Enumeration"
%><%@page import="java.util.Vector"
%><%@page import="org.w3c.dom.Element"
%><%AuthRequest ar = AuthRequest.getOrCreate(request, response, out);
    ar.assertLoggedIn("Unable to set site.");

    String p = ar.reqParam("p");
    String key = ar.reqParam("key");

    NGPage ngp = NGPageIndex.getProjectByKeyOrFail(p);
    ar.setPageAccessLevels(ngp);
    ar.assertAuthor("Unable to set the site for this project.");

    String go = ar.defParam("go", null);
    if (go==null)
    {
        go = ar.getResourceURL(ngp,"admin.htm");
    }


    NGBook oldBook = ngp.getAccount();
    NGBook ngb = null;

    if (key.equals("*"))
    {
        String newName = request.getParameter("newName");
        if (newName == null)
        {
            throw new Exception("Parameter newName is empty.  You must fill in a name for the project, if you desire to create a project.");
        }
        ngb = NGBook.createNewBook(newName);
        UserProfile up = ar.getUserProfile();
        ngb.getPrimaryRole().addPlayer(up);
        ngb.getSecondaryRole().addPlayer(up);
        ngb.saveBookAs(ngb.getKey(), ar.getUserProfile(), "Set Site Action");
    }
    else if (key.equals("/"))
    {
        throw new Exception("Setting default book no longer allowed");
    }
    else
    {
        ngb = NGPageIndex.getAccountByKeyOrFail(key);
    }

    if (ngb==null)
    {
        throw new Exception("program logic error: did not find the site '"+key+"'");
    }

    ngp.setAccount(ngb);
    ngp.saveFile(ar, "Set Site");
    response.sendRedirect(go);%>

<%@ include file="functions.jsp"%>
