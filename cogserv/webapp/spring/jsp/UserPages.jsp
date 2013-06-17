<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/include.jsp"
%><%@page import="org.socialbiz.cog.AuthRequest"
%><%@page import="org.socialbiz.cog.NGBook"
%><%@page import="org.socialbiz.cog.NGPage"
%><%@page import="org.socialbiz.cog.NGPageIndex"
%><%@page import="org.socialbiz.cog.TemplateRecord"
%><%@page import="org.socialbiz.cog.UserProfile"
%><%@page import="org.socialbiz.cog.spring.ProjectHelper"
%><%@page import="java.io.File"
%><%@page import="java.util.Enumeration"
%><%@page import="java.util.Properties"
%><%@page import="java.util.Vector"
%><%@page import="org.w3c.dom.Element"
%><%
/*
Required Parameter:

    1. bookList : This parameter provide list of account to fill account dropdown from request attribute.

Optional Parameter:

    1. bookKey      : This is key of an account.
    2. projectName  : This parameter is the name of project.


*/
    List<NGBook> bookList = (List<NGBook>) request.getAttribute("bookList");

    String bookKey = ar.defParam("bookKey",null);
    String projectName = ar.defParam("projectName","");

%><%!String pageTitle="";%><%
    request.setCharacterEncoding("UTF-8");

    UserProfile  uProf = ar.getUserProfile();

    ngb = null;
    List templateList = uProf.getTemplateList();

    Vector<NGPageIndex> templates = new Vector();
    if (templateList != null)
    {
        Hashtable visitDate = new Hashtable();

        for(int i=0;i<templateList.size();i++){
            TemplateRecord tr = (TemplateRecord)templateList.get(i);
            String pageKey = tr.getPageKey();
            NGPageIndex ngpi = NGPageIndex.getContainerIndexByKey(pageKey);
            if (ngpi!=null)
            {
                templates.add(ngpi);
                visitDate.put(ngpi.containerKey, new Long(tr.getLastSeen()));
            }
        }

        NGPageIndex.sortInverseChronological(templates);
    }

    if (ar.isLoggedIn() && uProf!=null)
    {
%>
<script language="javascript">
    var flag=false;
    var projectNameRequiredAlert = '<fmt:message key="nugen.project.name.required.error.text"/>';
    var projectNameTitle = '<fmt:message key="nugen.project.projectname.textbox.text"/>';

     function isProjectExist(){
             var projectName = document.getElementById('projectname').value;
             var acct = document.getElementById('accountId');

             var accountId = acct.options[acct.selectedIndex].value;

            var transaction = YAHOO.util.Connect.asyncRequest('POST',"isProjectExist.ajax?projectname="+projectName+"&book="+accountId, projectValidationResponse);
            return false;
        }

        var projectValidationResponse ={
                success: function(o) {
                    var respText = o.responseText;
                    var json = eval('(' + respText+')');
                    if(json.msgType == "no"){
                        document.forms["projectform"].submit();
                    }
                    else{
                        showErrorMessage("Result", json.msg, json.comments);
                    }
                },
                failure: function(o) {
                    alert("projectValidationResponse Error:" +o.responseText);
                }
        }
</script>
<body class="yui-skin-sam">

<div class="generalArea">
    <div class="pageHeading">My Projects</div>
    <div class="pageSubHeading">From here you can create a new project, view list of projects, delete a project & make existing project a template for future needs.</div>

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
                    title="Collapse" alt="" /></span>
               </td>
            </tr>
       </table>
   </form>
   <script language="javascript">
      initCal();
    </script>
<%
        }
    }
    else if (ar.isLoggedIn())   //unknown user
    {
%>
        <p>unable to find user.</p>
<%
    }
    else   //unknown user
    {
%>
        <p>You must be logged in, in order to see information about users.</p>
<%
    }
%>
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
<div id="paging"></div>
    <div id="container">
    <div id="container1">
        <table id="pagelist">
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
    </div>
    <br/>
    <br/>
     <%} %>
    <div class="generalHeadingBorderLess">List of Templates</div>
    <div id="paging1"></div>
    <div id="templateDiv">
        <table id="templatelist">
                <thead>
                    <tr>
                        <th>No</th>
                        <th><fmt:message key="nugen.userhome.Name"/></th>
                    </tr>
                </thead>
                <tbody>
                <%
                    int count = 0;
                    if (templateList != null)
                    {

                        for (NGPageIndex ngpi : templates)
                        {
                            String linkAddr = ar.retPath + "t/" +ngpi.pageBookKey+"/"+ngpi.containerKey + "/projectHome.htm";
                   %>
                        <tr>
                            <td><%=++count %></td>
                            <td>
                                <a href="<%ar.writeHtml(linkAddr);%>"
                                    title="navigate to the template page">
                                    <%ar.writeHtml(ngpi.containerName); %>
                                </a>

                                <!--<%ar.writeHtml(ngpi.containerName); %>-->
                            </td>
                        </tr>
                   <%

                        }
                    }
                %>
                </tbody>
            </table>
    </div>
</div>


    <script type="text/javascript">
    function trim(s) {
        var temp = s;
        return temp.replace(/^s+/,'').replace(/s+$/,'');
    }
    var paginator;
    var d = document;
        YAHOO.util.Event.addListener(window, "load", function()
        {
            YAHOO.example.EnhanceFromMarkup = function()
            {



                var myColumnDefs = [
                    {key:"no",width:5,label:"No",formatter:YAHOO.widget.DataTable.formatNumber,sortable:true,resizeable:true},
                    {key:"pagename",label:"Project Name", sortable:true,sortOptions:{sortFunction:sortNames},resizeable:true},
                    {key:"lastmodified",label:"Last Modified", sortable:true,sortOptions:{sortFunction:sortDates},resizeable:true},
                    {key:"comments",label:"Comments",sortable:true, resizeable:true},
                    {key:"pagenameHidden",label:"Page N", sortable:true,resizeable:true,hidden:true},
                    {key:"pagekey",label:"<fmt:message key='nugen.userhome.PageKey'/>", sortable:true,resizeable:true,hidden:true},
                    {key:"istemplate",label:"istemplate",sortable:true, resizeable:true,hidden:true},
                    {key:"timePeriod",label:"timePeriod",sortable:true, resizeable:true,hidden:true}
                ];

                var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("pagelist"));
                myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
                myDataSource.responseSchema = {
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
                        containers   : 'paging'
                    })
                var oConfigs = {
                    paginator: paginator,
                    initialRequest: "results=999999"
                };



                var myDataTable = new YAHOO.widget.DataTable("container1", myColumnDefs, myDataSource, oConfigs,
                {caption:"",sortedBy:{key:"no",dir:"desc"}});

                // Enable row highlighting
                myDataTable.subscribe("rowMouseoverEvent", myDataTable.onEventHighlightRow);
                myDataTable.subscribe("rowMouseoutEvent", myDataTable.onEventUnhighlightRow);

                 var onContextMenuClick = function(p_sType, p_aArgs, p_myDataTable) {
                    var task = p_aArgs[1];

                  if(task) {

                        var elRow = this.contextEventTarget;
                        elRow = p_myDataTable.getTrEl(elRow);

                        myDataTable2=p_myDataTable;
                        elRow2=elRow;
                        if(elRow) {
                            switch(task.index) {
                            case 0:     // Delete row upon confirmation
                                    var oRecord = p_myDataTable.getRecord(elRow);

                                    if(trim(oRecord.getData("istemplate")) == "true"){
                                        markTemplate(oRecord.getData("pagekey"),'removeTemplate','<%=ar.retPath %>');
                                    }else{
                                        markTemplate(oRecord.getData("pagekey"),'MarkAsTemplate','<%=ar.retPath %>');
                                    }

                            }
                        }
                    }
                };
                var myContextMenu = new YAHOO.widget.ContextMenu("mycontextmenu",
                        {trigger:myDataTable.getTbodyEl()});

                var onBeforeMenuClick = function(){
                    var elRow = this.contextEventTarget;
                    elRow = myDataTable.getTrEl(elRow);
                    var oRecord = myDataTable.getRecord(elRow);
                    myContextMenu.clearContent();
                    if(trim(oRecord.getData("istemplate")) == "true"){
                        myContextMenu.addItem("Stop using as Template");
                    }else{
                        myContextMenu.addItem("Mark as Template");
                    }
                     myContextMenu.render("container");
                }



                // Render the ContextMenu instance to the parent container of the DataTable
                myContextMenu.render("container");
                myContextMenu.clickEvent.subscribe(onContextMenuClick, myDataTable);
                myContextMenu.beforeShowEvent.subscribe(onBeforeMenuClick, myDataTable);
                return {
                    oDS: myDataSource,
                    oDT: myDataTable
                };
            }();
        });


        YAHOO.util.Event.addListener(window, "load", function()
        {
            YAHOO.example.EnhanceFromMarkup = function()
            {
                var templateColumnDefs = [
                    {key:"no",label:"No",sortable:true,resizeable:true},
                    {key:"templatename",label:"<fmt:message key='nugen.userhome.Name'/>", sortable:true,resizeable:true}
                ];

                var templateDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("templatelist"));
                templateDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
                templateDataSource.responseSchema = {
                    fields: [{key:"no"},
                            {key:"templatename"}
                            ]
                };

                var oConfigs = {
                    paginator: new YAHOO.widget.Paginator({
                        rowsPerPage: 200,
                        containers   : 'paging1'
                    }),
                    initialRequest: "results=999999"
                };


                var templateTable = new YAHOO.widget.DataTable("templateDiv", templateColumnDefs, templateDataSource, oConfigs,
                {caption:"",sortedBy:{key:"templatename",dir:"desc"}});

                // Enable row highlighting
                templateTable.subscribe("rowMouseoverEvent", templateTable.onEventHighlightRow);
                templateTable.subscribe("rowMouseoutEvent", templateTable.onEventUnhighlightRow);

                return {
                    oDS: templateDataSource,
                    oDT: templateTable
                };
            }();
        });
        function markTemplate(pageId,action,URL){
            var transaction = YAHOO.util.Connect.asyncRequest('POST', URL+"t/markAsTemplate.ajax?pageId="+pageId+"&action="+action, result);
        }
        var result = {
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

          var projectName = '<%=projectName%>';
            if(projectName!="" && projectName!=null){
                document.getElementById("projectname").value = projectName;
                updateVal();
            }

            var sortNames = function(a, b, desc) {
                if(!YAHOO.lang.isValue(a)) {
                    return (!YAHOO.lang.isValue(b)) ? 0 : 1;
                }
                else if(!YAHOO.lang.isValue(b)) {
                    return -1;
                }
                var comp = YAHOO.util.Sort.compare;
                var compState = comp(a.getData("pagenameHidden"), b.getData("pagenameHidden"), desc);
                return compState;
            };
    </script>

</body>
<%@ include file="functions.jsp"%>
