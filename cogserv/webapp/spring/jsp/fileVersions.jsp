<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/EditAttachment.jsp"
%><%@page import="org.socialbiz.cog.AttachmentVersion"
%>
<div class="content tab01">
    <%
    List<AttachmentVersion>  versionList = attachment.getVersions(ngp);
    %>
    <br>
    <B>Attachment Name : <%ar.writeHtml(attachment.getDisplayName());%></B>
    <br>
    <div class="scrollableOverflow">
        <div id="listofpagesdiv" width="100%">
            <table id="pagelist" width="100%">
                <thead>
                    <tr>
                        <th >Version</th>
                        <th >Modified Date</th>
                        <th>Time Period</th>
                    </tr>
                </thead>
                <tbody>
                <%
                String rowStyleClass = "";
                if(versionList != null){
                    for(int index=0 ; index<versionList.size();index++){
                        String contentLink = "";
                        String ftype = attachment.getType();
                        if (ftype.equals("URL"))
                        {
                            contentLink = fname; // URL.
                        }
                        else
                        {
                            contentLink = "a/" + SectionUtil.encodeURLData(attachment.getNiceName())+"?version="+versionList.get(index).getNumber();
                        }
                        %>
                    <tr >
                        <td>
                            <a href="<%ar.writeHtml(contentLink); %>" title="Access the content of this attachment">
                            <% writeHtml(out,String.valueOf(versionList.get(index).getNumber()));  %>
                            </a>
                        </td>
                        <td><% SectionUtil.nicePrintTime(out, versionList.get(index).getCreatedDate(), ar.nowTime); %></td>
                        <td style='display:none'><%= (ar.nowTime - versionList.get(index).getCreatedDate())/1000%></td>
                    </tr>
                    <%
                    }
                }
                %>
                </tbody>
            </table>
        </div>
        <form action="updateAttachment.form" enctype="multipart/form-data" method="post">
            <button class="inputBtn"  value="Cancel" onclick="return Cancel();" ><fmt:message key='nugen.button.general.goback'/></button>
        </form>
    </div>
</div>
</div></div></div></div></div>
