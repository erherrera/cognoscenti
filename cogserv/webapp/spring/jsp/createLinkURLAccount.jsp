<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/attachment_forms_account.jsp" %>
<div class="content tab01">
    <b><%ar.writeHtmlMessage("nugen.attachment.uploadattachment.LinkURL",null); %></b>
    <br><br>
    <p>
    <%ar.writeHtmlMessage("nugen.attachment.uploadattachment.LinkURLtoProject",new Object[]{ngb.getFullName()}); %>
    </p>
    <table width="600">
        <form action="createLinkURL.form" method="post" onsubmit="return checkVal('link');">
            <input type="hidden" name="encodingGuard" value="%E6%9D%B1%E4%BA%AC"/>
            <tr><td colspan="2">&nbsp;</td></tr>
            <tr>
                <td>
                    <%ar.writeHtmlMessage("nugen.attachment.uploadattachment.DescriptionOf",null); %>
                    <%ar.writeHtmlMessage("nugen.attachment.uploadattachment.WebPage",null); %>
                </td>
                <td>
                    <textarea name="comment" id="link_comment" style="WIDTH:95%;"></textarea>
                </td>
            </tr>
            <tr><td colspan="2">&nbsp;</td></tr>
            <tr>
                <td><%ar.writeHtmlMessage("nugen.attachment.uploadattachment.Accessibility",null); %></td>
                <td>
                    <input type="radio" name="visibility" value="*PUB*"/> <%ar.writeHtmlMessage("nugen.attachment.uploadattachment.Public",null); %>
                    <input type="radio" name="visibility" value="*MEM*" checked="checked"/> <%ar.writeHtmlMessage("nugen.attachment.uploadattachment.Member",null); %>
                </td>
            </tr>
            <tr><td colspan="2">&nbsp;</td></tr>
            <input type="hidden" id="ftype" name="ftype" value="URL"/>
            <tr>
                <td><%ar.writeHtmlMessage("nugen.attachment.uploadattachment.URL",null); %></td>
                <td><input type="text" id="taskUrl" name="taskUrl" size="60"/></td>
            </tr>
            <tr><td colspan="2">&nbsp;</td></tr>
            <tr>
                <td></td>
                <td>
                    <input type="submit" name="action" class="inputBtn" value="<%ar.writeHtmlMessage("nugen.attachment.uploadattachment.button.AttachWebURL",null); %>">
                    <input type="button"  class="inputBtn"  name="action" value="<%ar.writeHtmlMessage("nugen.button.general.cancel",null); %>" onclick="cancel();"/>
                </td>
            </tr>
        </form>
    </table>
</div>
</div></div></div>
