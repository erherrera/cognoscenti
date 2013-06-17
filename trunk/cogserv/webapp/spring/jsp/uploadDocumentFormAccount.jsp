<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/attachment_forms_account.jsp" %>
<div class="content tab01">
  <b><fmt:message key="nugen.attachment.uploadattachment.UploadNewAttachment"/></b>
  <br><br>
  <p><fmt:message key="nugen.attachment.uploadattachment.UseformToUploadFile.text">
      <fmt:param value='<%=ngb.getFullName()%>'/>
  </fmt:message></p>
  <div class="generalSettings">
  <form action="upload.form" method="post" enctype="multipart/form-data" onsubmit="return checkVal('attachment');">
    <table width="100%" border="0" cellpadding="2" cellspacing="2" >
      <tr>
          <td>
            <fmt:message key="nugen.attachment.uploadattachment.Description.text" />
          </td>
          <td>
            <textarea name="comment" id="comment" style="WIDTH:95%;"></textarea>
          </td>
      </tr>
      <tr>
          <td>&nbsp;</td>
        </tr>
      <tr>
          <td><fmt:message key="nugen.attachment.uploadattachment.permission" /></td>
          <td>
              <input type="radio" name="visibility" value="*PUB*"/> <fmt:message key="nugen.attachment.uploadattachment.Public" />
              <input type="radio" name="visibility" value="*MEM*" checked="checked"/> <fmt:message key="nugen.attachment.uploadattachment.Member" />
        </td>
      </tr>
      <tr>
          <td>&nbsp;</td>
        </tr>
        <input type="hidden" id="ftype"   name="ftype" value="FILE">
      <tr>
          <td><fmt:message key="nugen.attachment.uploadattachment.LocalFile" /></td>
          <td>
            <input type="file"   name="fname"   id="fname" size="60" onblur="writeAccessName(this.value);"/>
          </td>
      </tr>
      <tr>
          <td>&nbsp;</td>
        </tr>
      <tr>
          <td><fmt:message key="nugen.attachment.uploadattachment.AccessName" /></td>
          <td>
            <input type="text" id="name"   name="name"   size="60"/>
          </td>
      </tr>
      <tr>
          <td>&nbsp;</td>
        </tr>
      <!--<tr>
           <td colspan="2"><I><fmt:message key="nugen.attachment.uploadattachment.LeaveAccesssNameEmpty.text" /></I></td>
      </tr>
      -->
      <tr>
          <td>&nbsp;</td>
        </tr>
      <tr>
          <td>&nbsp;</td>
          <td>
            <input type="submit" name="action" class="inputBtn" value="<fmt:message key='nugen.attachment.uploadattachment.button.UploadAttachmentFile'/>">&nbsp;&nbsp;
            <input type="button"  class="inputBtn"  name="action" value="<fmt:message key='nugen.button.general.cancel'/>" onclick="cancel();"/>
          </td>
      </tr>
    </table>
  </form>
</div></div>

</div>
</div>
</div>
</div>

