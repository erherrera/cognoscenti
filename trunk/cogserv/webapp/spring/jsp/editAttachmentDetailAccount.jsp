<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="EditAttachmentAccount.jsp"
%>
<div class="content tab01">
    <form name="attachmentForm" method="post" action="editAttachment.form" enctype="multipart/form-data" onSubmit="return enableAllControls()">
        <input type="hidden" name="aid" value="<% writeHtml(out, aid); %>">
        <table width="100%" class="">
            <tr><td style="height:20px"></td></tr>
            <tr>
                <td  class="gridTableColummHeader_2">Type:</td>
                <td style="width:20px;"></td>
                <td class="Odd">
                    <%
                    if (isFile)
                    {
                        out.write("File");
                    }
                    else if (isURL)
                    {
                        out.write("URL");
                    }
                    %>
                    <input type="hidden" name="ftype" value="<%ar.writeHtml(type);%>">
                </td>
            </tr>
            <tr><td style="height:2px"></td></tr>
            <tr>
                <td  class="gridTableColummHeader_2">
                    <label id="pathLbl">Local File Path:</label>
                </td>
                <td style="width:20px;"></td>
                <td class="Odd">
                    <div id="fnamediv"><input type="file" name="fname" value="<% writeHtml(out, fname); %>" id="fname" style="WIDTH:95%;"/></div>
                </td>
            </tr>
            <tr><td style="height:2px"></td></tr>
            <tr>
                <td align="left" class="gridTableColummHeader_2">
                    <label id="nameLbl"  >Access Name:</label>
                </td>
                <td style="width:20px;"></td>
                <td class="Odd">
                    <input type="text" name="name" value="<% writeHtml(out, name); %>" id="name" style="WIDTH:95%;"/>
                </td>
            </tr>
            <tr><td style="height:2px"></td></tr>
            <tr>
                <td  class="gridTableColummHeader_2">Description:</td>
                <td style="width:20px;"></td>
                <td class="Odd">
                    <textarea name="comment" style="WIDTH:95%; HEIGHT:74px;"><% writeHtml(out, comment); %></textarea>
                </td>
            </tr>
            <tr><td style="height:2px"></td></tr>
            <tr>
                <td  class="gridTableColummHeader_2">Last Modified By:</td>
                <td style="width:20px;"></td>
                <td class="Odd"><% writeHtml(out, SectionUtil.cleanName(muser)); %></td>
            </tr>
            <tr><td colspan="2">&nbsp;</td></tr>
            <tr>
                <td  class="gridTableColummHeader_2">Last Modified Date:</td>
                <td style="width:20px;"></td>
                <td class="Odd"><% SectionUtil.nicePrintTime(out, mdate, ar.nowTime); %></td>
            </tr>
            <tr><td style="height:2px"></td></tr>
            <tr>
                <td  class="gridTableColummHeader_2">Permission:</td>
                <td style="width:20px;"></td>
                <td class="Odd">
                <% if (attachment.getVisibility()>1) { %>
                    <input type="radio" name="visibility"  value="PUB"/> Public Access
                    <input type="radio" name="visibility" value="MEM" checked="checked"/> Member Only Access
                <% } else { %>
                    <input type="radio" name="visibility" value="PUB" checked="checked"/> Public Access
                    <input type="radio" name="visibility" value="MEM"/> Member Only Access
                <% } %>
                </td>
            </tr>
            <tr><td style="height:2px"></td></tr>
            <tr>
                <td  class="gridTableColummHeader_2">Storage Name:</td>
                <td style="width:20px;"></td>
                <td class="Odd"><% writeHtml(out, attachment.getStorageFileName()); %></td>
            </tr>
            <tr><td style="height:2px"></td></tr>
            <tr>
                <td  class="gridTableColummHeader_2">Mime Type:</td>
                <td style="width:20px;"></td>
                <td class="Odd">
                <%
                String mimeType=MimeTypes.getMimeType(attachment.getNiceName());
                ar.writeHtml(mimeType);
                %>
                </td>
            </tr>
        </table>
        <br/>
        <center>
            <button type="submit"  class="inputBtn"  name="action" value="Update">Update</button>&nbsp;&nbsp;
            <button type="submit"  class="inputBtn"  name="action" value="Cancel"><fmt:message key='nugen.button.general.cancel'/></button>&nbsp;&nbsp;
            <button type="submit" onclick="return confirmRemove();" class="inputBtn" name="action" value="Remove">Delete Attachment</button>
            <input type="hidden" id="removebtn" value="no" />
            <input type="checkbox" name="confirmdel"/> Check to confirm delete
        </center>
        <br/>
    </form>
</div>
</div>
</div></div></div>
    <script>

        changeControls('<%=type.toUpperCase()%>');
        function confirmRemove(){
            document.getElementById("removebtn").value = "yes";
            if (document.attachmentForm.confirmdel.checked == false){
                if(confirm("Do you really want to remove this attachment?")){
                    return true;
                }else{
                    return false;
                }
            }
        }
        function enableAllControls() {
            document.attachmentForm.fname.disabled = false;
            if(document.getElementById("removebtn").value == "no"){
                if (document.attachmentForm.chgFile.checked == false){
                    var flag = check("fname", "Local File");
                    if(flag){
                        return checkVisibility();
                    }else{
                        return flag;
                    }
                }else{
                    return checkVisibility();
                }
            }
        }
        function checkVisibility(){

            var btn = valButton(document.attachmentForm.visibility);
            if (btn == null){
                alert('No Visibility selected');
                return false;
            }else{
                return true;
            }

        }
        function valButton(btn) {
            var cnt = -1;
            for (var i=btn.length-1; i > -1; i--) {
                if (btn[i].checked) {cnt = i; i = -1;}
            }
            if (cnt > -1) return btn[cnt].value;
            else return null;
        }
    </script>
