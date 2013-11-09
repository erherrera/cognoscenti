<%@page errorPage="error.jsp"
%><%@page contentType="text/html;charset=UTF-8" pageEncoding="ISO-8859-1"
%><%@page import="org.socialbiz.cog.AuthRequest"
%><%@page import="org.socialbiz.cog.ConfigFile"
%><%@page import="org.socialbiz.cog.NGBook"
%><%@page import="org.socialbiz.cog.NGPage"
%><%@page import="org.socialbiz.cog.NGPageIndex"
%><%@page import="org.socialbiz.cog.NGSection"
%><%@page import="org.socialbiz.cog.SectionDef"
%><%@page import="org.socialbiz.cog.SectionFormat"
%><%@page import="java.io.File"
%><%@page import="java.net.URLEncoder"
%><%@page import="java.util.Enumeration"
%><%@page import="java.util.Properties"
%><%@page import="java.util.Vector"
%><%@page import="org.w3c.dom.Element"
%><%
    AuthRequest ar = AuthRequest.getOrCreate(request, response, out);
    ar.assertLoggedIn("Unable to set site.");

    String p = ar.reqParam("p");
    String go = ar.reqParam("go");

    NGPage ngp = NGPageIndex.getProjectByKeyOrFail(p);
    ar.setPageAccessLevels(ngp);

    NGBook ngb = ngp.getAccount();
    String selKey = "";
    if (ngb!=null)
    {
        selKey = ngb.getKey();
    }

    //search and find all the book files on disk
    String dataFolder = ar.getSystemProperty("dataFolder");
    File root = ConfigFile.getFolderOrFail(dataFolder);
    File[] children = root.listFiles();



%>
<html>
<head>
    <title>Set Site</title>
    <link href="mystyle.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<h3>Select the Site for this Page</h3>
<%
    if (!ar.isAdmin())
    {
        %><p><b>Note: you are not Admin of this project, you will not be able to change the site setting.</b></p><%
    }
    if (ngb!=null)
    {
        %><p>Page has an acount: <%ar.writeHtml(ngb.getName());%> with key <%ar.writeHtml(ngb.getKey());%></p><%
    }
    else
    {
        %><p>site is null</p><%
    }
%>
<table>
<form action="SetBookAction.jsp" method="post">
<input type="hidden" name="p" value="<%ar.writeHtml(p);%>">
<input type="hidden" name="go" value="<%ar.writeHtml(go);%>">
<input type="hidden" name="encodingGuard" value="<%ar.writeHtml("\u6771\u4eac");%>"/>
        <%
    for (int i=0; i<children.length; i++)
    {
        File child = children[i];
        String fileName = child.getName();
        if (!fileName.endsWith(".book"))
        {
            //ignore all files except those that end in .book
            continue;
        }
        String key = fileName.substring(0,fileName.length()-5);

        NGBook aBook = NGPageIndex.getAccountByKeyOrFail(key);
        String aKey = aBook.getKey();
        %><tr><td><input type="radio" name="key" value="<%
        ar.writeHtml(aKey);
        %>"<%
        if (selKey.equals(aKey))
        {
            %> checked="checked"<%
        }
        %>> <%
        ar.writeHtml(aBook.getName());
        %> -- <%
        ar.writeHtml(aKey);
        %></td></tr>
        <%
    }
%>
<tr><td><input type="radio" name="key" value="/">
Clear Setting (Project has no site) </td></tr>
<tr><td><input type="radio" name="key" value="*">
Create New Site: <input type="text" name="newName" value=""></td></tr>


<tr><td>
<input type="submit" value="Update Site Setting">
</td></tr>
</form>
</table>
<hr/>
</body>
</html>
<%@ include file="functions.jsp"%>
