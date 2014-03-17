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

    RemoteGoal remoteGoal = uPage.findRemoteGoal(url);
    if (remoteGoal==null) {
        throw new Exception("Unable to find the remote goal record");
    }

    List<HistoryRecord> histRecs = new Vector<HistoryRecord>();
    pageTitle = uProf.getName();

    UserProfile  operatingUser =ar.getUserProfile();
    boolean viewingSelf = uProf.getKey().equals(operatingUser.getKey());

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

    String accessUrl = remoteGoal.getProjectAccessURL();
    boolean isLocal = accessUrl.startsWith(ar.baseURL);
    NGPage localProject = null;
    if (isLocal) {
        int siteBegin = accessUrl.indexOf("/", ar.baseURL.length())+1;
        int projBegin = accessUrl.indexOf("/", siteBegin)+1;
        int projEnd   = accessUrl.indexOf("/", projBegin);
        String siteKey = accessUrl.substring(siteBegin, projBegin-1);
        String projKey = accessUrl.substring(projBegin, projEnd);
        localProject = NGPageIndex.getProjectByKeyOrFail(projKey);
    }
    else {
        localProject = NGPageIndex.getProjectByUpstreamLink(accessUrl);
        if (localProject!=null && !accessUrl.equals(localProject.getUpstreamLink())) {
            throw new Exception("Strange, found the project but it has the wrong URL");
        }
    }

%>

    <script src="<%=ar.baseURL%>jscript/jquery.dd.js" type="text/javascript"></script>
    <link rel="stylesheet" type="text/css" href="<%=ar.retPath%>css/dd.css" />

    <script type="text/javascript" language = "JavaScript">
        function createProject(){
            document.forms["projectform"].submit();
        }
    </script>

<body class="yui-skin-sam">

    <!-- Content Area Starts Here -->
    <div class="generalArea">

        <div id="TabbedPanels1" class="TabbedPanels">
            <div class="TabbedPanelsContentGroup">
                <div class="TabbedPanelsContent">
                    <table width="600">
                        <!-- ========================================================================= -->
                        <tr><td height="23px"></td></tr>
                        <tr>
                            <form action="<% ar.writeHtml(remoteGoal.getUserInterfaceURL());%>" method="get">
                            <td colspan="3" class="generalHeading">Remote Goal Status
                                &nbsp; &nbsp; &nbsp; &nbsp;
                                <input type="submit" value="Visit Original Goal" class="inputBtn" /></td>
                            </form>                        </tr>
                        <tr><td height="10px"></td></tr>
                        <tr>
                            <td class="gridTableColummHeader"><fmt:message key="nugen.process.taskname.display.text"/>:</td>
                            <td style="width:20px;"></td>
                            <td class="textAreaGeneral">
                                <%ar.writeHtml(remoteGoal.getSynopsis());%>
                            </td>
                        </tr>
                        <tr><td height="10px"></td></tr>
                        <tr>
                            <td class="gridTableColummHeader">Project:</td>
                            <td style="width:20px;"></td>
                            <td class="textAreaGeneral">
                                <a href="<%ar.writeHtml(remoteGoal.getProjectAccessURL());%>">
                                <%ar.writeHtml(remoteGoal.getProjectName());%></a> of
                                <a href="<%ar.writeHtml(remoteGoal.getSiteAccessURL());%>">
                                <%ar.writeHtml(remoteGoal.getSiteName());%></a>
                            </td>
                        </tr>
                        <tr><td height="10px"></td></tr>
                        <tr>
                            <td class="gridTableColummHeader">Assigned To:</td>
                            <td style="width:20px;"></td>
                            <td class="textAreaGeneral">
                                <%ar.writeHtml("unknown");%>
                            </td>
                        </tr>
                        <tr><td height="10px"></td></tr>
                        <tr>
                            <td class="gridTableColummHeader"><fmt:message key="nugen.process.priority.text"/></td>
                            <td style="width:20px;"></td>
                            <td>
                                <table>
                                    <tr>
                                        <td style="width:150px;" class="textAreaGeneral">
                                            <%ar.writeHtml(BaseRecord.getPriorityStr(remoteGoal.getPriority()));%>
                                         </td>
                                         <td style="width:45px;"></td>
                                         <td style="color:#000000"><b><fmt:message key="nugen.process.state.text"/></b></td>
                                        <td style="width:10px;"></td>
                                        <td class="textAreaGeneral">
                                            <img src="<%=ar.retPath%>assets/images/<%=BaseRecord.stateImg(remoteGoal.getState())%>">
                                            &nbsp; &nbsp;<%ar.writeHtml(BaseRecord.stateName(remoteGoal.getState()));%>
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
                                        <td class="textAreaGeneral">
                                            <%=(remoteGoal.getDueDate()==0)?"":formatter.format(new Date(remoteGoal.getDueDate()))%>
                                        </td>
                                        <td style="width:17px;"></td>
                                        <td style="color:#000000"><b>Completed:</b></td>
                                        <td style="width:10px;"></td>
                                        <td class="textAreaGeneral">
                                            <%=remoteGoal.getPercentComplete()%>%
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr><td height="10px"></td></tr>
                        <tr>
                            <td class="gridTableColummHeader"><fmt:message key="nugen.project.startdate.text"/></td>
                            <td style="width:20px;"></td>
                            <td>
                                <table>
                                    <tr>
                                        <td class="textAreaGeneral">
                                            <%=(remoteGoal.getStartDate()==0)?"":formatter.format(new Date(remoteGoal.getStartDate()))%>
                                        </td>
                                        <td style="width:17px;"></td>
                                        <td style="color:#000000"><b>End:</b></td>
                                        <td style="width:10px;"></td>
                                        <td class="textAreaGeneral">
                                            <%=(remoteGoal.getEndDate()==0)?"&nbsp;":formatter.format(new Date(remoteGoal.getEndDate()))%>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                         <tr><td height="10px"></td></tr>
                        <tr>
                            <td class="gridTableColummHeader" valign="top"><fmt:message key="nugen.project.desc.text"/></td>
                            <td style="width:20px;"></td>
                            <td class="textAreaGeneral"><%ar.writeHtml(remoteGoal.getDescription());%></td>
                        </tr>
                        <tr><td height="25px"></td></tr>
                        <tr>
                            <td class="gridTableColummHeader" valign="top"><fmt:message key="nugen.project.status.text"/></td>
                            <td style="width:20px;"></td>
                            <td class="textAreaGeneral"><%ar.writeHtml(remoteGoal.getStatus());%></td>
                        </tr>
                        <tr><td height="30px"></td>
                        </tr>
                    </table>
                    <div class="generalArea">
                        <div class="generalHeading">Local Project</div>
                        <div class="generalContent">
                    <%
                        if(!isLocal && localProject!=null){
                    %>
                        <table WIDTH="600"><tr><td>
                            <form action="<%=ar.retPath%><%=ar.getResourceURL(localProject, "projectActiveTasks.htm")%>" method="get">
                                <input type="submit" value="Access Local Clone" class="inputBtn" />
                            </form>
                        </td>
                        <td style="width:20px;"></td>
                        <td> Use this button to access the local project (<%ar.writeHtml(localProject.getFullName());%>)
                             which is a clone of the remote project that the goal is on.
                        </td></tr></table>
                    <%
                        }
                        else if (isLocal && localProject!=null) {
                    %>
                            <div>
                            This project is on this local host: <a href="<%=ar.retPath%><%=ar.getResourceURL(localProject,"projectActiveTasks.htm")%>">
                                <%ar.writeHtml(localProject.getFullName());%></a><br/>
                                <%ar.writeHtml(remoteGoal.getAccessURL());%><br/>
                                <%ar.writeHtml(ar.baseURL);%>
                            </div>
                    <%
                        }
                        else if(!viewingSelf){
                    %>
                            <div id="loginArea">
                                <span class="black">No project available.  Can't create a project on someone else's remote task.</span>
                            </div>
                    <%
                        }
                        else
                        {
                    %>
                        <table WIDTH="600"><tr><td>
                            <form action="userCreateProject.htm" method="get">
                                <input type="hidden" name="upstream" value="<%ar.writeHtml(remoteGoal.getProjectAccessURL());%>"/>
                                <input type="hidden" name="pname" value="<%ar.writeHtml(remoteGoal.getProjectName());%> (clone)"/>
                                <input type="hidden" name="desc" value="This is a local clone of a project named '<%ar.writeHtml(remoteGoal.getProjectName());%>' on a remote site named '<%ar.writeHtml(remoteGoal.getSiteName());%>'"/>
                                <input type="submit" value="Create Local Clone" class="inputBtn" />
                            </form>
                        </td>
                        <td style="width:20px;"></td>
                        <td> Use this button to create a local clone of the remote project,
                           to download all the document for working off line, and to allow you to 
                           involve other people in working on the project.
                        </td></tr></table>

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
