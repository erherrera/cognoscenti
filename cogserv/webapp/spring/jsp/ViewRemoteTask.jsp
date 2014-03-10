<%@page errorPage="/spring/jsp/error.jsp"
%><%@page import="java.util.Date"
%><%@page import="java.text.SimpleDateFormat"
%><%@page import="org.socialbiz.cog.TemplateRecord"
%><%@ include file="/spring/jsp/include.jsp"
%><%@ include file="/spring/jsp/functions.jsp"
%><%!
    String pageTitle = "";
%><%
/*
Required parameters:

    1. userKey   : This is the id of an user
    2. url      : This parameter is url which is the unique key of the goal record

*/

    String userId = ar.reqParam("userKey");
    String url = ar.reqParam("url");

    UserProfile uProf = ar.getUserProfile();
    UserPage uPage = uProf.getUserPage();

    pageTitle = uProf.getName();

    SimpleDateFormat formatter  = new SimpleDateFormat ("MM/dd/yyyy");
    String bookKey=null;  //this is always NULL

    RemoteGoal currentTaskRecord = uPage.findRemoteGoal(url);
    if (currentTaskRecord==null) {
        throw new Exception("Unable to find the remote goal record");
    }

    List<HistoryRecord> histRecs = new Vector<HistoryRecord>();
    pageTitle = uProf.getName();





    Vector<NGBook> bookList =  new Vector<NGBook>();
    Vector<NGPageIndex> templates = new Vector<NGPageIndex>();
    for(TemplateRecord tr : uProf.getTemplateList()){
        String pageKey = tr.getPageKey();
        NGPageIndex ngpi = NGPageIndex.getContainerIndexByKey(pageKey);
        if (ngpi!=null){
            templates.add(ngpi);
            bookList.add(ngpi.getPage().getSite());
        }
    }
    NGPageIndex.sortInverseChronological(templates);


%>

    <script src="<%=ar.baseURL%>jscript/jquery.dd.js" type="text/javascript"></script>
    <link rel="stylesheet" type="text/css" href="<%=ar.retPath%>css/dd.css" />

    <script type="text/javascript" language = "JavaScript">

        var flag=false;
        var emailflag=false;
        var taskNameRequired = '<fmt:message key="nugen.process.taskname.required.error.text"/>';
        var taskName = '<fmt:message key="nugen.process.taskname.textbox.text"/>';
        var emailadd='<fmt:message key="nugen.process.emailaddress.textbox.text"/>'

         function submitUpdatedTask(){
            var taskname =  document.getElementById("taskname_update");
            if(!(!taskname.value=='' || !taskname.value==null)){
                alert(taskNameRequired);
                    return false;
            }
            document.forms["updateTaskForm"].submit();
        }

        function createProject(){
            document.forms["projectform"].submit();
        }
        function inviteUser(bookId,pageId,emailId)
        {
            var uri='<%=ar.retPath%>'+"t/"+bookId+"/"+pageId+"/inviteUser.htm?emailId="+emailId;
            window.open(uri,TARGET="_parent");
        }

        function AddNewAssigne(){
            document.forms["assignTask"].submit();
        }

        var callbackprocess = {
           success: function(o) {
               var respText = o.responseText;
               var json = eval('(' + respText+')');
               if(json.msgType != "success"){
                   showErrorMessage("Result", json.msg , json.comments );
              }
           },
           failure: function(o) {
                   alert("callbackprocess Error:" +o.responseText);
           }
        }

    function removeAssigne(assigneeId){
        document.getElementById("remove").value="true";
        document.getElementById("removeAssignee").value=assigneeId;
        document.forms["assignTask"].submit();
    }

    function updateAssigneeVal(){
        emailflag=true;
    }


    function createSubTask(){
        var taskname =  document.getElementById("taskname");
        var assignto =  document.getElementById("assignto_SubTask");

        if(taskname.value=='' || taskname.value==null){
            alert(taskNameRequired);
                return false;
        }

        if(assignto.value==emailadd){
            document.getElementById("assignto_SubTask").value="";
        }
        document.forms["createSubTaskForm"].elements["assignto"].value = assignto.value;
        document.forms["createSubTaskForm"].submit();
    }

    function updateTaskVal(){
        flagSubTask=true;
    }

    function clearField(elementName) {
        var task=document.getElementById(elementName).value;
        if(task==taskName){
            document.getElementById(elementName).value="";
            document.getElementById(elementName).style.color="black";
        }
    }

    function defaultTaskValue(elementName) {
        var task=document.getElementById(elementName).value;
        if(task==""){
            flag=false;
            document.getElementById(elementName).value=taskName;
            document.getElementById(elementName).style.color = "gray";
        }
    }

    function validatePercentage(){
        var percentage = document.getElementById("percentage").value;
        if(percentage==""){
            document.getElementById("percentage").value = 0;
        }else{
            var x = parseInt(percentage);
            var numericExpression = /^[0-9]+$/;
            if(percentage.match(numericExpression)) {
                if (isNaN(x) || x < 0 || x > 100) {
                    alert("Please enter correct percentage, a numeric value between 0 to 100");
                }
            } else {
                alert("Please enter correct percentage, a numeric value between 0 to 100");
            }
        }
    }
</script>

<body class="yui-skin-sam">

    <!-- Content Area Starts Here -->
    <div class="generalArea">
        <div class="pageHeading">
            <img src="<%=ar.retPath%>/assets/images/tb_<%=BaseRecord.stateImg(currentTaskRecord.getState())%>" />
            <span style="color:#5377ac"> <%=BaseRecord.stateName(currentTaskRecord.getState())%> Goal:</span>
            <%
                ar.writeHtml(currentTaskRecord.getSynopsis());
            %>
        </div>

        <div class="pageSubHeading">
            <table>
                <tr>
                    <td valign="top">assigned to:&nbsp;&nbsp;
                        <div id="assignDivContent" class="assignDivContent" style="display:none">

                        </div>
                         <a href=""><span style="color:red">XXXX</span></a>
                    </td>
                    <td valign="top">&nbsp;&nbsp;&nbsp;<a href="#" title="More" onclick="expandDiv('assignDivContent')"><img src="<%=ar.retPath%>/assets/iconMore.gif" border="0" alt="More" /></a></td>
                    <td valign="top">&nbsp;&nbsp;&nbsp;&nbsp;
                        due date:
                        <span  id="top_btn_dueDate" style="color: red">
                            <%
                                writeDate(ar,currentTaskRecord.getDueDate());
                            %>
                        </span>
                        start date:
                        <span id="top_btn_dueDate" style="color: red">
                            <%
                                writeDate(ar,currentTaskRecord.getStartDate());
                            %>
                        </span>

                        end date:
                        <span  id="top_btn_dueDate" style="color: red">
                            <%
                                writeDate(ar,currentTaskRecord.getEndDate());
                            %>
                        </span>
                    </td>
                </tr>
            </table>
        </div>

        <p style="padding-top:10px"><%
            ar.writeHtmlWithLines(currentTaskRecord.getDescription());
        %></p>

        <div id="TabbedPanels1" class="TabbedPanels">
            <div class="TabbedPanelsContentGroup">
                <div class="TabbedPanelsContent">
                    <table width="600">
                        <!-- ========================================================================= -->
                        <form name="updateTaskFormB" action="updateTask.form" method="post">
                            <input type="hidden" name="go" id="go" value="<%=ar.getCompleteURL()%>"/>
                            <input type="hidden" name="url" value="<%ar.writeHtml(url);%>">
                            <tr><td height="23px"></td></tr>
                            <tr>
                                <td colspan="3" class="generalHeading">Status</td>
                            </tr>
                            <tr><td height="10px"></td></tr>
                            <tr>
                                <td class="gridTableColummHeader"><fmt:message key="nugen.process.taskname.display.text"/>:</td>
                                <td style="width:20px;"></td>
                                <td><input type="text" name="taskname_update" id="taskname_update" class="inputGeneral" size="50" tabindex=1  value='<%ar.writeHtml(currentTaskRecord.getSynopsis());%>'/>
                                </td>
                            </tr>
                            <tr><td height="10px"></td></tr>
                            <tr>
                                <td class="gridTableColummHeader"><fmt:message key="nugen.process.priority.text"/></td>
                                <td style="width:20px;"></td>
                                <td>
                                    <table>
                                        <tr>
                                            <td><select name="priority" id="priority" tabindex="4">
                                                <%
                                                    int taskPrior = currentTaskRecord.getPriority();
                                                    for (int i=0; i<3; i++)
                                                    {
                                                        ar.write("\n     <option ");
                                                        if (i==taskPrior) {
                                                            out.write("selected=\"selected\" ");
                                                        }
                                                        ar.write("value=\"");
                                                        ar.write(Integer.toString(i));
                                                        ar.write("\">");
                                                        ar.writeHtml(BaseRecord.getPriorityStr(i));
                                                        ar.write("</option>");
                                                    }
                                                %>
                                                </select>
                                             </td>
                                             <td style="width:45px;"></td>
                                             <td style="color:#000000"><b><fmt:message key="nugen.process.state.text"/></b></td>
                                            <td style="width:10px;"></td>
                                            <td>
                                                <select name="state" id="state" class="specialNo" tabindex="5">
                                                <%
                                                    int taskState = currentTaskRecord.getState();
                                                    for(int i=1;i<=8;i++){
                                                        String img4=ar.retPath+"assets/images/"+BaseRecord.stateImg(i);
                                                        ar.write("\n     <option ");
                                                        if (i==taskState) {
                                                            ar.write("selected=\"selected\"");
                                                        }
                                                        ar.write(" value=\"");
                                                        ar.write(Integer.toString(i));
                                                        ar.write("\"  title=\"");
                                                        ar.writeHtml(img4);
                                                        ar.write("\" >");
                                                        ar.writeHtml(BaseRecord.stateName(i));
                                                        ar.write("</option>");
                                                    }
                                                    String img5=ar.retPath+"assets/images/"+BaseRecord.stateImg(0);
                                                    ar.write("\n     <option ");
                                                    if(taskState==0){
                                                         ar.write("selected=\"selected\"");
                                                    }
                                                    ar.write(" value=\"0\"  title=\"");
                                                    ar.writeHtml(img5);
                                                    ar.write("\" >");
                                                    ar.writeHtml(BaseRecord.stateName(0));
                                                    ar.write("</option>");
                                                %>
                                                </select>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr><td height="10px"></td></tr>
                            <tr>
                                <td class="gridTableColummHeader"><fmt:message key="nugen.project.duedate.text"/></td>
                                <td style="width:20px;"></td>
                                <td>
                                    <table>
                                        <tr>
                                            <td><input type="text" size="16" name="dueDate_update" id="dueDate_update"
                                                value='<%=(currentTaskRecord.getDueDate()==0)?"":formatter.format(new Date(currentTaskRecord.getDueDate()))%>' readonly="1"/>
                                            </td>
                                            <td style="width:17px;"></td>
                                            <td style="color:#000000"><b><fmt:message key="nugen.project.startdate.text"/></b></td>
                                            <td style="width:10px;"></td>
                                            <td><input type="text" size="16" name="startDate_update" id="startDate_update" value='<%=(currentTaskRecord.getStartDate()==0)?"":formatter.format(new Date(currentTaskRecord.getStartDate()))%>' readonly="1"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr><td height="10px"></td></tr>
                            <tr>
                                <td class="gridTableColummHeader"><fmt:message key="nugen.project.enddate.text"/></td>
                                <td style="width:20px;"></td>
                                <td><input type="text" size="16" name="endDate_update" id="endDate_update"  value='<%=(currentTaskRecord.getEndDate()==0)?"":formatter.format(new Date(currentTaskRecord.getEndDate()))%>' readonly="1"/>
                                </td>
                            </tr>
                             <tr><td height="10px"></td></tr>
                            <tr>
                                <td class="gridTableColummHeader" valign="top"><fmt:message key="nugen.project.desc.text"/></td>
                                <td style="width:20px;"></td>
                                <td><textarea name="description" id="description" class="textAreaGeneral" rows="4" tabindex=7><%
                                    ar.writeHtml(currentTaskRecord.getDescription());
                                %></textarea></td>
                            </tr>
                            <tr><td height="25px"></td></tr>
                            <tr>
                                <td class="gridTableColummHeader" valign="top"><fmt:message key="nugen.project.status.text"/></td>
                                <td style="width:20px;"></td>
                                <td><textarea id="status" name="status" class="textAreaGeneral" rows="4"><%
                                    ar.writeHtml(currentTaskRecord.getStatus());
                                %></textarea></td>
                            </tr>
                            <tr><td height="20px"></td></tr>
                            <tr>
                                <td class="gridTableColummHeader"><fmt:message key="nugen.process.state.text"/></td>
                                <td style="width:20px;"></td>
                                <td><table><tr>
                                    <td>
                                        <select name="states" id="states" class="specialx" tabindex=5 >
                                        <%
                                            for(int i=1;i<=9;i++){
                                                String selected = "";
                                                if (i==currentTaskRecord.getState()) {
                                                    selected = "selected=\"selected\"";
                                                }
                                                String img3=ar.retPath+"assets/images/"+BaseRecord.stateImg(i);
                                                out.write("     <option " + selected + " value=\"" + i + "\"  title=\""+img3+"\" >" + BaseRecord.stateName(i) + "</option>");
                                            }
                                            String errorselected="";
                                            if(currentTaskRecord.getState()==0){
                                                errorselected = "selected=\"selected\"";
                                            }
                                            String image=ar.retPath+"assets/images/"+BaseRecord.stateImg(0);
                                            out.write("     <option " + errorselected + " value=\"" + 0 + "\"  title=\""+image+"\" >" + BaseRecord.stateName(0) + "</option>");
                                        %>
                                        </select>
                                    </td>
                                    <td style="width:20px;"></td>
                                    <td style="color:#000000"><b>Completed:</b></td>
                                    <td style="width:10px;"></td>
                                    <td>
                                        <input type="text" name="percentage" id="percentage" value="<%=currentTaskRecord.getPercentComplete()%>"
                                            style="font-size:12px;color:#333333;width: 25px;" onchange="validatePercentage()"/>%
                                    </td>
                                </tr></table></td>
                            </tr>
                            <tr><td height="10px"></td></tr>
                            <tr>
                                <td class="gridTableColummHeader" valign="top"></td>
                                <td style="width:20px;"></td>
                                </form>
                                <form action="<% ar.writeHtml(currentTaskRecord.getUserInterfaceURL());%>" method="get">
                                <td><input type="submit" value="Visit Task Update Page" class="inputBtn" /></td>
                            </tr>
                        </form>


                        <tr><td height="30px"></td></tr>
                        <tr>
                            <td colspan="3" class="generalHeading">Local Project</td>
                        </tr>


                    </table>
                    <div class="generalArea">
                        <!--  Start here -->
                        <div class="generalContent">
                    <%
                        if(bookList!=null && bookList.size()<1){
                    %>
                            <div id="loginArea">
                                <span class="black">
                                    <fmt:message key="nugen.userhome.PermissionToCreateProject.text"/>
                                </span>
                            </div>
                    <%
                        }
                                    else
                                    {
                                        String actionPath=ar.retPath+"t/createProjectFromRemoteGoal.form";
                    %>
                            <form name="projectform" action='<%=actionPath%>' method="post" autocomplete="off" >
                                <table width="600">
                                    <tr><td style="height:20px"></td></tr>
                                    <tr>
                                        <td class="gridTableColummHeader">Sub Project Name:</td>
                                        <td style="width:20px;"></td>
                                        <td>
                                            <input type="text" onblur="validateProjectField()" class="inputGeneral"
                                            name="projectname" id="projectname" value="<%ar.writeHtml(currentTaskRecord.getSynopsis());%>"
                                            onKeyup="updateVal();" onblur="addvalue();" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="gridTableColummHeader"></td>
                                        <td style="width:20px;"></td>
                                        <td width="396px">
                                            <b>Note:</b> From here you can create a new subproject.The subproject will be connected to this activity, and will be completed when the subproject process is completed.
                                        </td>
                                    </tr>
                                    <tr><td style="height:10px"></td></tr>
                                    <tr>
                                        <td class="gridTableColummHeader">Select Template:</td>
                                        <td style="width:20px;"></td>
                                        <td><Select class="selectGeneral" id="templateName" name="templateName">
                                            <option value="" selected>Select</option>
                                            <%
                                                for (NGPageIndex ngpi : templates){
                                                    %><option value="<%=ngpi.containerKey%>" ><%
                                                    ar.writeHtml(ngpi.containerName);
                                                    %></option><%
                                                }
                                            %>
                                                    </Select></td>
                                      </tr>
                                      <tr><td style="height:15px"></td></tr>
                                      <tr>
                                          <td class="gridTableColummHeader"><fmt:message key="nugen.userhome.Account"/></td>
                                          <td style="width:20px;"></td>
                                          <td><select class="selectGeneral" name="accountId" id="accountId">
                                            <%
                                                for (NGBook nGBook : bookList) {
                                                    String id =nGBook.getKey();
                                                    String bookName= nGBook.getName();
                                                    %><option value="<%=id%>"><%
                                                    ar.writeHtml(bookName);
                                                    %></option><%
                                                }
                                            %>
                                          </select></td>
                                     </tr>
                                     <tr><td style="height:15px"></td></tr>
                                     <tr>
                                         <td class="gridTableColummHeader" style="vertical-align:top"><fmt:message key="nugen.project.desc.text"/></td>
                                         <td style="width:20px;"></td>
                                         <td><textarea name="description" id="description" class="textAreaGeneral" rows="4" tabindex=7></textarea></td>
                                     </tr>
                                     <tr><td style="height:10px"></td></tr>
                                     <tr>
                                         <td class="gridTableColummHeader"></td>
                                         <td style="width:20px;"></td>
                                         <td>
                                             <input type="button" value="Create Sub Project" class="inputBtn" onclick="createProject();" />
                                             <input type="hidden" name="goUrl" value="<%ar.writeHtml(ar.getCompleteURL());%>" />
                                             <input type="hidden" id="parentProcessUrl" name="parentProcessUrl"
                                                value="XXX" />
                                         </td>

                                     </tr>
                                </table>
                            </form>
                  <%
                    }
                  %>
                        </div>
                        <!-- End here -->
                      </div>
                </div>
            </div>
            <script type="text/javascript">
                var TabbedPanels1 = new Spry.Widget.TabbedPanels("TabbedPanels1");
            </script>
        </div>
    </div>
</body>

<%
    if (ar.isMember()){
        SectionTask.plugInCalenderScript(out, "dueDate_update", "btn_update");
        SectionTask.plugInCalenderScript(out, "dueDate", "btn_dueDate");
        SectionTask.plugInCalenderScript(out, "startDate_update", "top_btn_startDate");
        SectionTask.plugInCalenderScript(out, "endDate_update", "top_btn_endDate");
    }
%><%!public void writeDate(AuthRequest ar, long date) throws Exception {
        if(date!=0){
            ar.write(new SimpleDateFormat("MM/dd/yyyy").format(new Date(date)));
        }else{
            ar.write(" -- ");
        }
    }%>
