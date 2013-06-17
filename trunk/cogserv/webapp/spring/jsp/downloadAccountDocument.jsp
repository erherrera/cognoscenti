<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/include.jsp"
%><%
/*
Required parameters:

    1. accountId : This is the id of an account and here it is used to retrieve NGBook (Account's Details).
    2. aid : This is document/attachment id which is used to get information of the attachment being downloaded.

*/

    String accountId = ar.reqParam("accountId");
    String aid = ar.reqParam("aid");

%><%!String pageTitle="";%><%

    NGBook ngb = (NGBook)NGPageIndex.getContainerByKeyOrFail(accountId);

    AttachmentRecord attachment = ngb.findAttachmentByIDOrFail(aid);

    String access = "Member Only";
    if (attachment.getVisibility()<=1)
    {
        access = "Public";
    }

    String accessName = attachment.getNiceName();

    String relativeLink = "$/a/" + SectionUtil.encodeURLData(accessName)+"?version="+attachment.getVersion();
    String permaLink = ar.getResourceURL(ngb, relativeLink);

    AddressListEntry ale = new AddressListEntry(attachment.getModifiedBy());

    pageTitle = ngb.getFullName() + " / "+ attachment.getNiceNameTruncated(48);

    UserProfile uProf = ar.getUserProfile();

    if("URL".equals(attachment.getType())){
        if(attachment.getStorageFileName().startsWith("http")){
            permaLink = attachment.getStorageFileName();
        }else{
            permaLink = "http://"+attachment.getStorageFileName();
        }
    }
%>
<script type="text/javascript" src="<%=ar.retPath%>jscript/attachment.js"></script>
<body class="yui-skin-sam">
    <div class="generalHeading">Download Document</div>
    <div class="pageSubHeading">You can view the details of the document and also download it from here.</div>
    <div class="generalSettings">
        <table border="0px solid red" class="popups">
            <tr>
                <td class="gridTableColummHeader_2">Document Name:</td>
                <td style="width:20px;"></td>
                <td><b><% ar.writeHtml(attachment.getNiceName()); %></b></td>
            </tr>
            <tr><td style="height:5px"></td></tr>
            <tr>
                <td class="gridTableColummHeader_2">Description:</td>
                <td style="width:20px;"></td>
                <td><% writeHtml(out, attachment.getComment()); %></td>
            </tr>
            <tr><td style="height:5px"></td></tr>
            <tr>
                <td class="gridTableColummHeader_2">Uploaded by:</td>
                <td style="width:20px;"></td>
                <td><% ale.writeLink(ar); %></td>
            </tr>
            <tr><td style="height:5px"></td></tr>
            <tr>
                <td class="gridTableColummHeader_2">Accessibility:</td>
                <td style="width:20px;"></td>
                <td><%ar.writeHtml(access);%></td>
            </tr>
            <tr><td style="height:5px"></td></tr>
            <tr>
                <td class="gridTableColummHeader_2">Version:</td>
                <td style="width:20px;"></td>
                <td><%ar.writeHtml(String.valueOf(attachment.getVersion()));%></td>
            </tr>
            <tr><td style="height:10px"></td></tr>
            <tr>
                <td></td>
                <td></td>
                <%
                    if (attachment.getVisibility() == SectionDef.PUBLIC_ACCESS || (attachment.getVisibility() == SectionDef.MEMBER_ACCESS && ar.isLoggedIn()))
                    {
                %>
                <td>
                    <%if("URL".equals(attachment.getType())){ %>
                        <a href="<%ar.writeHtml(permaLink);%>"><img src="<%=ar.retPath%>assets/btnAccessLinkURL.gif" border="0"></a>
                    <%}else { %>
                        <a href="<%=ar.retPath%><%ar.writeHtml(permaLink); %>"><img src="<%=ar.retPath%>download.gif" border="0"></a>
                    <%} %>
                </td>
            </tr>
            <tr><td style="height:20px"></td></tr>
            <tr>
                <td></td>
                <td></td>
                <td>
                    <%if("FILE".equals(attachment.getType())){ %>
                    <input type="button" class="inputBtn" onclick="return openWin('<%=ar.retPath%>t/sendNoteByEmail.htm?p=<%ar.writeHtml(accountId);%>&oid=x&selectedAttachemnt=attach<%ar.writeHtml(attachment.getId()); %>'); " value="Send Email"/>
                    &nbsp;
                    <input type="button" class="inputBtn" onclick="return  getUploadRevisedDocForm(<%ar.writeQuote4JS(attachment.getId());%>,<%ar.writeQuote4JS(attachment.getNiceName());%>,<%ar.writeQuote4JS(attachment.getComment());%>, <%ar.writeQuote4JS(String.valueOf(attachment.getVersion()));%>);" value="Upload New Version"/>

                    <%}%>
                    </td>
            </tr>
                <%
                    }
                    else{
                %>
            <tr>
                <td></td>
                <td></td>
                <td>
                    <a href="#"><img src="<%=ar.retPath%>downloadInactive.gif" border="0"></a><br />
                    <span class="red">* You need to log in to download this document.</span>
                </td>
            </tr>
                <%
                    }
                %>
            <tr><td style="height:10px"></td></tr>
            <tr>
                <td></td>
                <td></td>
                <td><span class="tipText">This web page is a secure and convenient way to send documents to others
                    collaborating on projects.
                    The email message does not carry the document, but
                    only a link to this page, so that email is small.
                    Then, from this page, you can get the very latest
                    version of the document.  Documents can be protected
                    by access controls.</span>
                </td>
            </tr>
        </table>
    </div>
<%@ include file="functions.jsp"%>
</body>