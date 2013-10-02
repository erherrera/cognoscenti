<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="UserHome.jsp"
%><%

    List<NGBook> bookList = (List<NGBook>) request.getAttribute("bookList");

    String bookKey = ar.defParam("bookKey",null);
    String projectName = ar.defParam("projectName","");
%>
<div class="content tab03" style="display:block;">
    <div class="section_body">
    <%
    if(bookList!=null && bookList.size()<1){
    %>
       <div class="guideVocal">
           You have not created any projects.  When you create projects, they will be listed here.<br/>
           <br/>
           In order to create a project, you need to be an "Owner" or an "Executive" of an "Account".<br/>
           <br/>
           Use <button class="inputBtn" onClick="location.href='userAccounts.htm'">Settings &gt; Accounts</button>
           to view your accounts, or request a new account from the system administrator.
           If approved you will be the owner of that new account,
           and can create new projects within it.

       </div>
    <%
    }
    else
    {
    %>
        <form name="projectform" action="createProject.form" method="post" autocomplete="off">
            <table class="popups">
                   <tr><td style="height:30px"></td></tr>
                   <tr>
                    <td class="gridTableColummHeader_2 bigHeading">Create New Project:</td>
                    <td style="width:20px;"></td>
                    <td>
                        <table cellpadding="0" cellspacing="0">
                           <tr>
                               <td class="createInput" style="padding:0px;">
                                   <input type="text" class="inputCreateButton" name="projectname" id="projectname"
                                   value='<fmt:message key="nugen.project.projectname.textbox.text"/>'
                                   onKeyup="updateVal();" onfocus="ClearForm();" onblur="addvalue();"
                                   onclick="expandDiv('assignTask')" />
                               </td>
                               <td class="createButton" onclick="submitForm();">&nbsp;</td>
                           </tr>
                       </table>
                   </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <table id="assignTask" style="display:none">
                            <tr><td width="148" class="gridTableColummHeader_2" style="height:20px"></td></tr>
                            <tr>
                                <td width="148" class="gridTableColummHeader_2">Select Template:</td>
                                <td width="39" style="width:20px;"></td>
                                <td><Select class="selectGeneral" id="templateName" name="templateName">
                                        <option value="" selected>Select</option>
                                        <%
                                        for (NGPageIndex ngpi : templates)
                                        {
                                            %>
                                            <option value="<%ar.writeHtml(ngpi.containerKey);%>" ><%ar.writeHtml(ngpi.containerName);%></option>
                                            <%
                                        }
                                        %>
                                    </Select>
                                </td>
                            </tr>
                            <tr><td style="height:15px"></td></tr>
                            <tr>
                                <td width="148" class="gridTableColummHeader_2"><fmt:message key="nugen.userhome.Account"/></td>
                                <td width="39" style="width:20px;"></td>
                                <td><select class="selectGeneral" name="accountId" id="accountId">
                                    <%
                                    for (NGBook ngb : bookList)
                                    {
                                        String id =ngb.getKey();
                                        String bookName= ngb.getName();
                                        if(bookKey !=null && id.equalsIgnoreCase(bookKey))
                                        {
                                            %><option value="<%ar.writeHtml(id);%>" selected><%
                                        }
                                        else
                                        {
                                            %><option value="<%ar.writeHtml(id);%>"><%
                                        }
                                        ar.writeHtml(bookName);
                                        %></option><%
                                    }
                                    %>
                                   </select>
                                </td>
                            </tr>
                            <tr><td style="height:10px"></td></tr>
                            <tr>
                                <td width="148" class="gridTableColummHeader_2"><fmt:message key="nugen.project.duedate.text"/></td>
                                <td width="39" style="width:20px;"></td>
                                <td><input type="text" class="inputGeneral" style="width:368px" size="50" name="dueDate" id="dueDate"  value="" readonly="1"/>
                                    <img src="<%=ar.retPath %>/jscalendar/img.gif" id="btn_dueDate" style="cursor: pointer;" title="Date selector"/>
                                </td>
                            </tr>
                            <tr><td style="height:15px"></td></tr>
                            <tr>
                                <td class="gridTableColummHeader_2" style="vertical-align:top"><fmt:message key="nugen.project.desc.text"/></td>
                                <td style="width:20px;"></td>
                                <td><textarea name="description" id="description" class="textAreaGeneral" rows="4" tabindex=7></textarea></td>
                            </tr>
                            <tr><td style="height:20px"></td></tr>
                            <tr>
                                <td width="148" class="gridTableColummHeader_2">Upstream Link:</td>
                                <td style="width:20px;"></td>
                                <td><input type="text" class="inputGeneral" style="width:368px" size="50" name="upstream" value=""/>
                                </td>
                            </tr>
                            <tr><td style="height:20px"></td></tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td width="148" class="gridTableColummHeader_2"></td>
                    <td width="39" style="width:20px;"></td>
                    <td style="cursor:pointer">
                        <span id="showDiv" style="display:inline" onclick="setVisibility('assignTask')">
                            <img src="<%=ar.retPath %>/assets/createSeperatorDown.gif" width="398" height="13"
                                title="Expand" alt="" /></span>
                        <span id="hideDiv" style="display:none" onclick="setVisibility('assignTask')">
                            <img src="<%=ar.retPath %>/assets/createSeperatorUp.gif" width="398" height="13"
                                title="Collapse" alt="" />
                        </span>
                       </td>
                </tr>
            </table>
        </form>
        <script language="javascript">
            initCal();
        </script>
    <%
    SectionTask.plugInCalenderScript(out, "dueDate", "btn_dueDate");
    SectionTask.plugInDurationCalcScript(out);
    %>
    <%
    }
    %>
    <br>
        <div class="generalHeadingBorderLess">List of Projects</div>
        <%
        Vector v = NGPageIndex.getAllPagesForAdmin(uProf);
        if(v.size()==0){
        %>
        <p>You do not have any projects currently.</p>
        <%
        }
        else{
        %>
        <div id="pagingProjects"></div>
        <div id="containerProjects">
            <table id="projectslist">
                <thead>
                    <tr>
                        <th>No</th>
                        <th>Project Name</th>
                        <th>Last Modified</th>
                        <th>Comment</th>
                        <th style="display:none">Page N</th>
                        <th style="display:none"><fmt:message key="nugen.userhome.PageKey"/></th>
                        <th style="display:none">found</th>
                        <th style="display:none">timediff</th>
                    </tr>
                </thead>
                <tbody>
        <%
            int size = v.size();
            Enumeration en1 = v.elements();
            for (int i=0; en1.hasMoreElements(); i++)
            {
                NGPageIndex ngpi = (NGPageIndex)en1.nextElement();
                String linkAddr = ar.retPath + "t/" +ngpi.pageBookKey+"/"+ngpi.containerKey + "/history.htm";
                String rowStyleClass="";
                if(i%2 == 0){
                 rowStyleClass = "tableBodyRow odd";
                }else{
                 rowStyleClass = "tableBodyRow even";
                }
        %>
                    <tr>
                        <td>
                            <%=(i+1)%>
                        </td>
                        <td>
                            <a href="<%writeHtml(out, linkAddr);%>" title="navigate to the page"><%writeHtml(out, ngpi.containerName);%></a>
                        </td>
                        <td>
                            <%SectionUtil.nicePrintTime(out, ngpi.lastChange, ar.nowTime);%>
                        </td>
                        <td>
            <%
                if (ngpi.isOrphan())
                {
                    out.write("Orphaned");
                }
                else if (ngpi.requestWaiting)
                {
                    out.write("Pending Requests");
                }
            %>

                        </td>
                        <td style='display:none'><%ar.writeHtml(ngpi.getPage().getFullName()); %></td>
                        <td style='display:none'><%ar.writeHtml(ngpi.getPage().getKey()); %></td>
                        <td style='display:none'><%= uProf.findTemplate(ngpi.getPage().getKey())%></td>
                        <td style='display:none'><%= (ar.nowTime - ngpi.lastChange)/1000%></td>
                    </tr>
            <%
            }
            %>
                </tbody>
            </table>
        </div>
        <%
        }
        %>
    </div>
</div>
<script type="text/javascript">
    YAHOO.util.Event.addListener(window, "load", function()
    {
        YAHOO.example.EnhanceFromMarkup = function()
        {
            var projectColumnDefs = [
                {key:"no",width:5,label:"No",formatter:YAHOO.widget.DataTable.formatNumber,sortable:true,resizeable:true},
                {key:"pagename",label:"Project Name", sortable:true,sortOptions:{sortFunction:sortNames},resizeable:true},
                {key:"lastmodified",label:"Last Modified", sortable:true,sortOptions:{sortFunction:sortDates},resizeable:true},
                {key:"comments",label:"Comments",sortable:true, resizeable:true},
                {key:"pagenameHidden",label:"Page N", sortable:true,resizeable:true,hidden:true},
                {key:"pagekey",label:"<fmt:message key='nugen.userhome.PageKey'/>", sortable:true,resizeable:true,hidden:true},
                {key:"istemplate",label:"istemplate",sortable:true, resizeable:true,hidden:true},
                {key:"timePeriod",label:"timePeriod",sortable:true, resizeable:true,hidden:true}
            ];

            var projectsDS = new YAHOO.util.DataSource(YAHOO.util.Dom.get("projectslist"));
            projectsDS.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
            projectsDS.responseSchema = {
                fields: [{key:"no", parser:"number"},
                        {key:"pagename"},
                        {key:"lastmodified"},
                        {key:"comments"},
                        {key:"pagenameHidden"},
                        {key:"pagekey"},
                        {key:"istemplate"},
                        {key:"timePeriod" , parser:YAHOO.util.DataSource.parseNumber}]
            };
            paginator =  new YAHOO.widget.Paginator({
                    rowsPerPage: 200,
                    containers   : 'pagingProjects'
                })
            var projectsConfigs = {
                paginator: paginator,
                initialRequest: "results=999999"
            };



            var projectsDT = new YAHOO.widget.DataTable("containerProjects", projectColumnDefs, projectsDS, projectsConfigs,
            {caption:"",sortedBy:{key:"no",dir:"desc"}});

            // Enable row highlighting
            projectsDT.subscribe("rowMouseoverEvent", projectsDT.onEventHighlightRow);
            projectsDT.subscribe("rowMouseoutEvent", projectsDT.onEventUnhighlightRow);

             var onContextMenuClick = function(p_sType, p_aArgs, p_projectsDT) {
                var task = p_aArgs[1];

                if(task) {
                    var elRow = this.contextEventTarget;
                    elRow = p_projectsDT.getTrEl(elRow);

                    projectsDT2=p_projectsDT;
                    elRow2=elRow;
                    if(elRow) {
                        switch(task.index) {
                        case 0:     // Delete row upon confirmation
                            var oRecord = p_projectsDT.getRecord(elRow);

                            if(trim(oRecord.getData("istemplate")) == "true"){
                                markTemplate(oRecord.getData("pagekey"),'removeTemplate','<%=ar.retPath %>');
                            }else{
                                markTemplate(oRecord.getData("pagekey"),'MarkAsTemplate','<%=ar.retPath %>');
                            }
                        }
                    }
                }
            };
            var projectContextMenu = new YAHOO.widget.ContextMenu("projectContextMenu",
                    {trigger:projectsDT.getTbodyEl()});

            var onBeforeMenuClick = function(){
                var elRow = this.contextEventTarget;
                elRow = projectsDT.getTrEl(elRow);
                var oRecord = projectsDT.getRecord(elRow);
                projectContextMenu.clearContent();
                if(trim(oRecord.getData("istemplate")) == "true"){
                    projectContextMenu.addItem("Stop using as Template");
                }else{
                    projectContextMenu.addItem("Mark as Template");
                }
                 projectContextMenu.render("containerProjects");
            }



            // Render the ContextMenu instance to the parent container of the DataTable
            projectContextMenu.render("containerProjects");
            projectContextMenu.clickEvent.subscribe(onContextMenuClick, projectsDT);
            projectContextMenu.beforeShowEvent.subscribe(onBeforeMenuClick, projectsDT);
            return {
                oDS: projectsDS,
                oDT: projectsDT
            };
        }();
    });

    var projectName = '<%=projectName%>';
    if(projectName!="" && projectName!=null){
        document.getElementById("projectname").value = projectName;
        updateVal();
    }

    function isProjectExist(){
        var projectName = document.getElementById('projectname').value;
        var acct = document.getElementById('accountId');
        var accountId = acct.options[acct.selectedIndex].value;
        var transaction = YAHOO.util.Connect.asyncRequest('POST',"isProjectExist.ajax?projectname="+projectName+"&accountId="+accountId, projectValidationResponse);
        return false;
    }

    function markTemplate(pageId,action,URL){
        var transaction = YAHOO.util.Connect.asyncRequest('POST', URL+"t/markAsTemplate.ajax?pageId="+pageId+"&action="+action, resultMarkTemplate);
    }
    var resultMarkTemplate = {
        success: function(o) {
                var respText = o.responseText;
                var json = eval('(' + respText+')');
                if(json.msgType == "success"){
                    window.location.reload();
                }
                else{
                    showErrorMessage("Result", json.msg , json.comments );
                }
            },
        failure: function(o) {
                alert("markAsTemplate.ajax Error:" +o.responseText);
        }
    }

</script>
