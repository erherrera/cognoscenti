<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/attachment_forms_account.jsp"
%>
<div class="content tab01">
    <B><fmt:message key="nugen.attachment.uploadattachment.SendEmailReminder" /></B>
    <br/><br/>
    <p><fmt:message key="nugen.attachment.uploadattachment.information.text" /></p>
    <table width="600">
        <col width="130">
        <col width="470">
        <tr>
            <td colspan="2">&nbsp;</td>
        </tr>
        <form action="emailReminder.form" method="post" onsubmit="return checkVal('email');">
            <input type="hidden" name="encodingGuard" value="<%writeHtml(out,"\u6771\u4eac");%>"/>
            <tr>
                <td><fmt:message key="nugen.attachment.uploadattachment.To" /></td>
                <td>
                    <input type="text" id="assignee" name="assignee" size="60"> <fmt:message key="nugen.attachment.uploadattachment.Email" />
                </td>
            </tr>
            <tr><td colspan="2">&nbsp;</td></tr>
            <tr>
                <td><fmt:message key="nugen.attachment.uploadattachment.Subject" /></td>
                <td><fmt:message key="nugen.attachment.uploadattachment.PleaseUploadFile" /> <input type="text"  id="subj"  name="subj" size="40"></td>
            </tr>
            <tr><td colspan="2">&nbsp;</td></tr>
            <tr>
                <td><fmt:message key="nugen.attachment.uploadattachment.Instructions" /></td>
                <td>
                    <textarea name="instruct" id="instruct" style="WIDTH:95%;">
                        <fmt:message key="nugen.attachment.uploadattachment.PleaseUploadFile.text" />
                    </textarea>
                </td>
            </tr>
            <tr><td colspan="2">&nbsp;</td></tr>
            <tr>
                <td><fmt:message key="nugen.attachment.uploadattachment.DescriptionAttachFile" /></td>
                <td>
                  <textarea name="comment" id="email_comment" style="WIDTH:95%;"></textarea>
                </td>
            </tr>
            <tr><td colspan="2">&nbsp;</td></tr>
            <tr>
                <td><fmt:message key="nugen.attachment.uploadattachment.Accessibility" /></td>
                <td>
                    <input type="radio" name="destFolder" value="*PUB*"/> <fmt:message key="nugen.attachment.uploadattachment.Public" />
                    <input type="radio" name="destFolder" value="*MEM*" checked="checked"/><fmt:message key="nugen.attachment.uploadattachment.Member" />
                </td>
            </tr>
            <tr><td colspan="2">&nbsp;</td></tr>
            <tr>
                <td>
                   <fmt:message key="nugen.attachment.uploadattachment.ProposedName" />
                </td>
                <td>
                    <input type="text" id="pname" name="pname" size="60">
                </td>
            </tr>
            <tr><td colspan="2">&nbsp;</td></tr>
            <tr>
                <td></td><td><fmt:message key="nugen.attachment.uploadattachment.EnterProposedName.text" /></td>
            </tr>
            <tr><td colspan="2">&nbsp;</td></tr>
            <tr>
                <td></td>
                <td>
                    <input type="submit" name="action" class="inputBtn" value="<fmt:message key='nugen.attachment.uploadattachment.button.CreateEmailReminder'/>">
                    <input type="button"  class="inputBtn"  name="action" value="<fmt:message key='nugen.button.general.cancel'/>" onclick="cancel();"/>
                </td>
            </tr>
        </form>
    </table>
</div>
</div>
</div>
</div>
</div>
</div>
