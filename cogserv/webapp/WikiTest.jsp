<%@page errorPage="error.jsp"
%><%@page contentType="text/html;charset=UTF-8" pageEncoding="ISO-8859-1"
%><%@page import="org.socialbiz.cog.NGBook"
%><%@page import="org.socialbiz.cog.NGPageIndex"
%><%@page import="org.socialbiz.cog.NGSession"
%><%@page import="org.socialbiz.cog.AuthRequest"
%><%@page import="org.socialbiz.cog.WikiConverterForWYSIWYG"
%><%@page import="org.socialbiz.cog.HtmlToWikiConverter"
%><%@page import="java.io.File"
%><%@page import="java.util.Properties"
%><%@page import="java.io.StringWriter"
%><%
    AuthRequest ar = AuthRequest.getOrCreate(request, response, out);
    ar.assertLoggedIn("Can not run wiki converter test page");

    String wiki1 = ar.defParam("wiki1", "");

    String html2 = convertWikiToHtmlString(ar, wiki1);


    HtmlToWikiConverter hc = new HtmlToWikiConverter();
    String wiki3 = hc.htmlToWiki(html2);

    boolean successful = (wiki1.equals(wiki3));

    //do 10 more conversions to illustrate if there is a blow-up problem
    String html4 = convertWikiToHtmlString(ar, wiki3);
    String wiki5 = hc.htmlToWiki(html4);
    html4 = convertWikiToHtmlString(ar, wiki5);
    wiki5 = hc.htmlToWiki(html4);
    html4 = convertWikiToHtmlString(ar, wiki5);
    wiki5 = hc.htmlToWiki(html4);
    html4 = convertWikiToHtmlString(ar, wiki5);
    wiki5 = hc.htmlToWiki(html4);
    html4 = convertWikiToHtmlString(ar, wiki5);
    wiki5 = hc.htmlToWiki(html4);

    boolean stable = (wiki5.equals(wiki3));


%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
    <head>
        <title>wiki converter test page</title>
    </head>
    <body>
    <h3>wiki converter test page</h3>
    <form action="WikiTest.jsp" method="GET">
        <input type="hidden" name="encodingGuard" value="<%ar.writeHtml("\u6771\u4eac");%>"/>
        <textarea name="wiki1" rows="5" cols="80"><%ar.writeHtml(wiki1);%></textarea><br/>
        <input type="submit" value="Test">
        </form>

    <hr>
    <h3>html conversion</h3>
<form action="HtmlTest.jsp" method="GET">
<input type="hidden" name="encodingGuard" value="<%ar.writeHtml("\u6771\u4eac");%>"/>
<textarea name="html2" rows="7" cols="80"><%ar.writeHtml(html2);%></textarea><br/>
<input type="submit" value="Take this to Html Test">
</form>

    <hr>
    <h3>wiki conversion
    <%
    if (successful)
    {
        ar.write("(roundtrip successful)");
    }
    else
    {
        ar.write("<font color=\"red\">(roundtrip NOT successful)</font>");
    }
    %>
    </h3>

<textarea rows="7" cols="80"><%ar.writeHtml(wiki3);%></textarea><br/>

    <hr>

<h3>After 5 round trip conversions
    <%
    if (stable)
    {
        ar.write("(result stable)");
    }
    else
    {
        ar.write("<font color=\"red\">(unstable, blows up)</font>");
    }
    %>
    </h3>

<textarea rows="10" cols="80"><%ar.writeHtml(wiki5);%></textarea>

</body>
</html>


<%!


    public String convertWikiToHtmlString(AuthRequest ar, String sourceWiki)
        throws Exception
    {
        StringWriter stringDest = new StringWriter();
        AuthRequest clone1 = new AuthDummy(ar.getUserProfile(), stringDest);
        WikiConverterForWYSIWYG.writeWikiAsHtml(clone1, sourceWiki);
        return stringDest.toString();
    }


%>