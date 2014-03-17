<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/include.jsp"
%><%@ include file="functions.jsp"
%><%@page import="org.socialbiz.cog.AgentRule"
%><%

    String go = ar.getCompleteURL();
    ar.assertLoggedIn("Must be logged in to see anything about a user");

    UserProfile uProf = (UserProfile)request.getAttribute("userProfile");
    if (uProf == null) {
        throw new NGException("nugen.exception.cant.find.user",null);
    }

    UserProfile  operatingUser =ar.getUserProfile();
    if (operatingUser==null) {
        //this should never happen, and if it does it is not the users fault
        throw new ProgramLogicError("user profile setting is null.  No one appears to be logged in.");
    }

    boolean viewingSelf = uProf.getKey().equals(operatingUser.getKey());

    UserPage uPage = uProf.getUserPage();
    String id = ar.reqParam("id");
    AgentRule theAgent = uPage.findAgentRule(id);
    if (theAgent==null) {
        throw new Exception("Unagle to find an agent with id="+id);
    }
    Vector<NGPageIndex> templates = uProf.getValidTemplates();
    List<NGBook> memberOfSites = uProf.findAllMemberSites();

%>
<body class="yui-skin-sam">

<script type="text/javascript">
    function openModalDialogue(popupId,headerContent,panelWidth){
        var   header = headerContent;
        var bodyText= document.getElementById(popupId).innerHTML;
        createPanel(header, bodyText, panelWidth);
        myPanel.beforeHideEvent.subscribe(function() {
            if(!isConfirmPopup){
                window.location = "<%=ar.getCompleteURL()%>";
            }
        });
    }
</script>

<div class="content tab03" style="display:block;">
    <div class="section_body">
        <div class="generalHeading">Edit Agent</div>
        <div style="height:10px;"></div>
        <div id="NewAgent">
            <div class="generalSettings">
                <form name="newProfile" id="newProfile" action="AgentAction.form" method="post">
                    <input type="hidden" name="go" id="updateGo" value="Agents.htm">
                    <input type="hidden" name="id" id="updateGo" value="<%ar.writeHtml(theAgent.getId());%>">
                    <table>
                        <tr id="trspath">
                            <td class="gridTableColummHeader">Name:</td>
                            <td style="width:20px;"></td>
                            <td colspan="2"><input type="text" name="name" class="inputGeneral" size="69"
                                value="<%ar.writeHtml(theAgent.getTitle());%>"/></td>
                        </tr>
                        <tr><td style="height:10px"></td></tr>
                        <tr id="trspath">
                            <td class="gridTableColummHeader">Subj Contains:</td>
                            <td style="width:20px;"></td>
                            <td colspan="2"><input type="text" name="subjexpr" class="inputGeneral" size="69"
                                value="<%ar.writeHtml(theAgent.getSubjExpr());%>"/></td>
                        </tr>
                        <tr id="trspath">
                            <td class="gridTableColummHeader">Desc Contains:</td>
                            <td style="width:20px;"></td>
                            <td colspan="2"><input type="text" name="descexpr" class="inputGeneral" size="69"
                                value="<%ar.writeHtml(theAgent.getDescExpr());%>"/></td>
                        </tr>
                        <tr><td style="height:10px"></td></tr>
                        <tr id="trspath">
                            <td class="gridTableColummHeader">Option:</td>
                            <td style="width:20px;"></td>
                            <td colspan="2"><input type="checkbox" name="accept"/> Auto-Accept
                                <input type="checkbox" name="transform"/> Schema Transform
                                <input type="checkbox" name="normalize"/> Normalize</td>
                        </tr>
                        <tr><td style="height:10px"></td></tr>
                        <tr id="trspath">
                            <td class="gridTableColummHeader">Template:</td>
                            <td style="width:20px;"></td>
                            <td colspan="2">
                                <select name="template" style="width:320px;"/>
                                <option value="">- Select One -</option>
                                <%
                                for (NGPageIndex ngpi : templates) {
                                      String key = ngpi.containerKey;

                                      %><option value="<%
                                      ar.writeHtml(key);
                                      if (key.equals(theAgent.getTemplate())) {
                                          %>" selected="selected<%
                                      }
                                      %>"><%
                                      ar.writeHtml(ngpi.containerName);
                                      %></option><%
                                }
                                %>
                                </select>
                            </td>
                        </tr>
                        <tr><td style="height:10px"></td></tr>
                        <tr id="trspath">
                            <td class="gridTableColummHeader">Site:</td>
                            <td style="width:20px;"></td>
                            <td colspan="2">
                                <select name="site" style="width:320px;"/>
                                <option value="">- Select One -</option>
                                <%
                                for (NGBook site : memberOfSites) {
                                      String key = site.getKey();

                                      %><option value="<%
                                      ar.writeHtml(key);
                                      if (key.equals(theAgent.getSiteKey())) {
                                          %>" selected="selected<%
                                      }
                                      %>"><%
                                      ar.writeHtml(site.getName());
                                      %></option><%
                                }
                                %>
                                </select>
                            </td>
                        </tr>
                        <tr><td style="height:30px"></td></tr>
                        <tr>
                            <td class="gridTableColummHeader"></td>
                            <td style="width:20px;"></td>
                            <td colspan="2">
                                <input type="submit" class="inputBtn" name="act" value="Update">
                                <input type="submit" class="inputBtn" name="act" value="Cancel">
                            </td>
                        </tr>
                    </table>
                </form>
            </div>
        </div>


    </div>
</div>
<script type="text/javascript">

    function confirmDeletion(id, name){
        form = document.getElementById(id);
        if(confirm("Do you want to delete the agent with following expression? \n-- '"+name+"'")) {
            form.submit();
            return true;
        }
        else {
            return false;
        }
    }

</script>

