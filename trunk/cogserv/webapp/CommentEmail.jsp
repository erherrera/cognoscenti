<%@page errorPage="error.jsp"
%><%@page contentType="text/html;charset=UTF-8" pageEncoding="ISO-8859-1"
%><%@page import="org.socialbiz.cog.AuthRequest"
%><%@page import="org.socialbiz.cog.NGBook"
%><%@page import="org.socialbiz.cog.NGPage"
%><%@page import="org.socialbiz.cog.NGPageIndex"
%><%@page import="org.socialbiz.cog.NoteRecord"
%><%@page import="org.socialbiz.cog.SectionComments"
%><%@page import="org.socialbiz.cog.SectionDef"
%><%@page import="org.socialbiz.cog.SectionFormat"
%><%@page import="org.socialbiz.cog.SectionUtil"
%><%@page import="org.socialbiz.cog.UserProfile"
%><%@page import="org.socialbiz.cog.UtilityMethods"
%><%@page import="java.net.URLEncoder"
%><%@page import="java.util.Enumeration"
%><%@page import="java.util.Vector"
%><%@page import="org.w3c.dom.Element"
%><%ar = AuthRequest.getOrCreate(request, response, out);
    ar.assertLoggedIn("Can not send email.");

    String p = ar.reqParam("p");
    String oid = ar.reqParam("oid");
    String note = ar.defParam("note", "Sending this note to let you know about a recent update to this web page has information that is relevant to you.  Follow the link to see the most recent version.");
    String emailto = ar.defParam("emailto", null);
    boolean pagemem = (ar.defParam("pagemem", null)!=null);
    boolean bookmem = (ar.defParam("bookmem", null)!=null);
    boolean exclude = (ar.defParam("exclude", null)!=null);
    boolean tempmem = (ar.defParam("tempmem", null)!=null);
    boolean includeBody = (ar.defParam("includeBody", null)!=null);

    assureNoParameter(ar, "s");

    ngp = NGPageIndex.getProjectByKeyOrFail(p);
    ar.setPageAccessLevels(ngp);
    ar.assertMember("Can not send email.");

    pageTitle = "Send Email: "+ngp.getFullName();

    //there is a special value for oid,
    //if it equals "X" it means there is NO note, and to just include attachments from the project
    String subject = "Documents from Project "+ngp.getFullName();
    if (!oid.equals("x"))
    {
        NoteRecord noteRec = ngp.getNoteOrFail(oid);
        subject = noteRec.getSubject();
    }

    UserProfile upx = ar.getUserProfile();
    AddressListEntry sampleUser = new AddressListEntry(upx);%>

<%@ include file="Header.jsp"%>

<!--  here is where the content goes -->


<table width="600">
<col width="130">
<col width="470">

<form action="CommentEmailAction.jsp" method="post">
<input type="hidden" name="encodingGuard" value="<%ar.writeHtml("\u6771\u4eac");%>"/>
<input type="hidden" name="p"       value="<%ar.writeHtml(p);%>"/>
<input type="hidden" name="oid"     value="<%ar.writeHtml(oid);%>"/>
<input type="hidden" name="go"      value="closeWindow.htm"/>
<tr>
  <td></td>
  <td>
    <input type="submit" name="action"  value="Preview Mail"/> &nbsp;
    <input type="submit" name="action"  value="Send Mail"/> &nbsp;
  </td>
</tr>
<tr>
  <td>To:</td>
  <td>
    <input type="checkbox" name="pagemem" value="true" <%if (pagemem) {out.write("checked=\"checked\"");}%>> Project Members,
    <input type="checkbox" name="bookmem" value="true" <%if (bookmem) {out.write("checked=\"checked\"");}%>> Executives
  </td>
</tr>
<tr>
  <td></td>
  <td>
    <input type="checkbox" name="exclude" value="true" <%if (exclude) {out.write("checked=\"checked\"");}%>> Exclude Responders,
    <input type="checkbox" name="tempmem" value="true" <%if (tempmem) {out.write("checked=\"checked\"");}%>> Provide Temporary Membership
  </td>
</tr>
<tr>
  <td>Also To:</td><td>
    <textarea rows="1" cols="50" name="emailto"><% if (emailto!=null) {ar.writeHtml(emailto);}%></textarea>
  </td>
</tr>
<tr>
  <td>
    Subject:
  </td>
  <td>
    <b><%ar.writeHtml(subject);%></b>
  </td>
</tr>
<tr>
  <td>Improv:</td><td>
    <textarea rows="4" cols="50" name="note"><%ar.writeHtml(note);%></textarea>
  </td>
</tr>
<% if (!oid.equals("x")) { %>
<tr>
  <td>Contents:</td><td>
    <input type="checkbox" name="includeBody" value="true" <%if (includeBody) {out.write("checked=\"checked\"");}%>> Include project note into email
  </td>
</tr>
<% } %>
<tr>
  <td colspan="2">Include these Attachments</td>
</tr>
<tr>
  <td></td><td>
  <%
    for (AttachmentRecord att : ngp.getAllAttachments())
    {
        String paramId = "attach"+att.getId();
        boolean attSelected = (ar.defParam(paramId, null)!=null);
        String niceName = att.getNiceName();
        %><input type="checkbox" name="<%=paramId%>" value="true" <%if(attSelected) { ar.write("checked=\"checked\"");}%>>
        <%
        if (niceName.length()>30)
        {
            niceName = niceName.substring(0,30)+"...";
        }
        ar.writeHtml(niceName);
        %><br/><%
    }
  %>
  </td>
</tr>
<tr>
  <td colspan="2">
    <hr/>
  </td>
</tr>
</form>
</table>

<br/>
<%@ include file="FooterNoLeft.jsp"%>
<%@ include file="functions.jsp"%>
