<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="ISO-8859-1"
%><%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/include.jsp"
%><%@ include file="functions.jsp"
%><%@page import="org.socialbiz.cog.NGRole"
%><%
/*
Required parameters:

    1. pageId : This is the id of an project and here it is used to retrieve NGPage (Project's Details).
    2. roleName : This request parameter is required to get NGRole detail of given role.

    3. roles    : This parameter is used to get List of all existing roles , this list is required to check
                  if provided 'role name' is already exists in the system or not.
*/

    String pageId   = ar.reqParam("pageId");
    String roleName = ar.reqParam("roleName");

    List roles=(List)request.getAttribute("roles");

%><%!
    String pageTitle="";
%><%

    NGPage ngp =NGPageIndex.getProjectByKeyOrFail(pageId);
    String projectKey = ngp.getKey();

    UserProfile uProf = ar.getUserProfile();
    NGRole role = ngp.getRoleOrFail(roleName);
    pageTitle = roleName+" Role of "+ngp.getFullName();
    NGBook ngb = ngp.getAccount();

    ar.setPageAccessLevels(ngp);
    ar.assertMember("Unable to edit the roles of this page");

    String go = ar.getCompleteURL();

    %>
<script type="text/javascript" language = "JavaScript">
    function submitRole(){
        var rolename =  document.getElementById("rolename");

        if(!(!rolename.value=='' || !rolename.value==null)){
                alert("Role Name Required");
                    return false;
            }
             <%if(roles!=null){
               Iterator  it=roles.iterator();
                while(it.hasNext()){%>
                    if(rolename.value=='<%=UtilityMethods.quote4JS(((NGRole)it.next()).getName())%>'){
                        alert("Role Name already exist");
                       return false;
                   }
              <%} }%>
            document.forms["createRoleForm"].submit();
        }

    function updateRole(op,id){
        var remove = true;
        if(op =="Remove"){
            remove = confirmRemoval(id);
        }
        if(remove){
            document.forms["updateRoleForm"].action = '<%=ar.retPath%>t/<%ar.writeURLData(ngp.getAccount().getKey());%>/<%ar.writeURLData(ngp.getKey());%>/pageRoleAction.form?r=<%ar.writeURLData(roleName);%>&op='+op+'&id='+id;
            document.forms["updateRoleForm"].submit();
        }
    }

    function removeRole(op,id){
        document.getElementById('id').value=id;
        document.forms["updateRoleForm"].action = '<%=ar.retPath%>t/<%ar.writeURLData(ngp.getAccount().getKey());%>/<%ar.writeURLData(ngp.getKey());%>/pageRoleAction.form?r=<%ar.writeURLData(roleName);%>&op='+op;
        document.forms["updateRoleForm"].submit();
    }

    function confirmRemoval(id){
        return confirm("Do you really want to remove this User '"+id+"' from Role: '<%ar.writeHtml(roleName);%>'?")
    }

    function addRoleMember(op,id){

        if(!(!id.value=='' || !id.value==null)){
            alert("Email Required");
            return false;
        }

         if(validateMultipleEmails(id)){

           id=id.value.replace(new RegExp("\n|," , "gi"), ";");
           updateRole(op,id);
         }
    }

  function validateMultipleEmails(id) {
       id=id.value.replace(new RegExp(",|;" , "gi"), "\n");

       var result=new Array();
       if( id.indexOf("\n") != -1){
                result = id.split("\n");
            }
          if(result==0){
           if(!validateEmail(id)){
              alert("'"+id+ "' email id id wrong. Please provide valid Email id.");
              return false;
      }
          }

            for(var i = 0;i < result.length;i++){
          if(trimme(result[i]) != ""){
              if(!validateEmail(trimme(result[i]))){
                  alert("'"+result[i]+ "' email id id wrong. Please provide valid Email id.");
                  return false;
              }
          }
      }
       return true;
    }

   function validateEmail(field) {
        var regex=/\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b/i;
        return (regex.test(field)) ? true : false;
  }
</script>

<%
    if (role == null)
    {
%>
    <div class="section_title">
        <p>No role exists named '<%ar.writeHtml(roleName);%>', would you like to create one?</p>
    </div>
    <!-- Content Area Starts Here -->
    <div id="createRole" class="generalArea">
        <div class="generalHeading"><fmt:message key="nugen.projectsettings.heading.CreateNewRole"/></div>
        <div class="generalContent">
            <form name="createRoleForm" action="CreateRole.form" method="post">
                <table width="100%" border="0" cellpadding="0" cellspacing="0">
                    <tr>
                        <td><b><fmt:message key="nugen.projectsettings.label.RoleName"/> :</b></td>
                        <td>
                            <input type="text" name="rolename" id="rolename" size="73" value =""/>&nbsp;
                            <input type="button" class="inputBtn" value="Add Role" onclick="submitRole();">
                        </td>
                    </tr>
                    <tr><td>&nbsp;</td></tr>
                    <tr>
                        <td><b><fmt:message key="nugen.projectsettings.label.MessageCriteria"/> :</b></td>
                        <td><textarea name="description" id="description" cols="70" rows="2"></textarea></td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
<%
    }
    else
    {
%>

    <!-- Content Area Starts Here -->
    <div id="listRole" class="generalArea">
        <form name="updateRoleForm" id="updateRoleForm" method="post" action="pageRoleAction.form">
            <input type="hidden" name="go" value="<% ar.writeHtml(go); %>">
            <%List<AddressListEntry> allUsers = role.getDirectPlayers();%>

            <div class="pageHeading">Details of Role '<%ar.writeHtml(roleName);%>'</div>
            <div class="pageSubHeading">You can modify the details of a particular role</div>
            <div class="generalSettings">

                <input type="hidden" name="id" value="na">
                <input type="hidden" name="pageId" value="<%ar.writeHtml(ngp.getKey());%>">
                <input type="hidden" name="roleName" value="<%ar.writeHtml(role.getName());%>">

                <table width="720px">
                    <tr><td style="height:10px" colspan="3"></td></tr>
                    <tr>
                        <td class="gridTableColummHeader_2">Name:</td>
                        <td style="width:20px;"></td>
                        <td><%ar.writeHtml(roleName);%></td>
                    </tr>
                    <tr><td style="height:8px" colspan="3"></td></tr>
                    <tr>
                         <td class="gridTableColummHeader_2" valign="top">Description:</td>
                         <td style="width:20px;"></td>
                         <td><textarea name="desc" id="description" class="textAreaGeneral" rows="4"><%ar.writeHtml(role.getDescription());%></textarea></td>
                    </tr>
                    <tr><td style="height:8px" colspan="3"></td></tr>
                    <tr>
                         <td class="gridTableColummHeader_2" valign="top">Eligibility:</td>
                         <td style="width:20px;"></td>
                         <td><textarea name="reqs" id="description" class="textAreaGeneral" rows="4"><%ar.writeHtml(role.getRequirements());%></textarea></td>
                    </tr>
                    <tr><td style="height:8px" colspan="3"></td></tr>
                    <tr>
                         <td class="gridTableColummHeader_2"></td>
                         <td style="width:20px;"></td>
                         <td><input type="button" class="inputBtn" value="Update Details" onclick="updateRole('Update Details','na');"></td>
                    </tr>
                    <tr><td style="height:10px" colspan="3"></td></tr>
                </table>
            </div>

            <div class="generalHeadingBorderLess">Expanded List of Players of Role '<%ar.writeHtml(roleName);%>'</div>
            <div class="generalContent">
            <%
            allUsers = role.getExpandedPlayers(ngp);
            ar.write("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"gridTable\">");
            for (AddressListEntry ale : allUsers)
            {
                ar.write("<tr>");
                ar.write("<td width=\"230px\" >");
                ale.writeLink(ar);
                ar.write("</td>");
                ar.write("<td>");
                ar.writeHtml(ale.getEmail());
                ar.write("</td>");
                ar.write("</tr>");
            }

            ar.write("</table>");
            %>
            </div>
            <br><br>

        </form>
    </div>
<%
    }
%>