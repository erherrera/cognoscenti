<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/attachment_forms_account.jsp"
%>

<%
    pageTitle = ngb.getFullName();
    String encodedLoginMsg = URLEncoder.encode("Can't open form","UTF-8");
    String encodedDeleteMsg = URLEncoder.encode("Can't delete attachment","UTF-8");

%>
<%@page import="java.util.ArrayList"%>
<script type="text/javascript" src="<%=ar.retPath%>jscript/attachment.js"></script>
<script type="text/javascript">
     var isLoggedIn = "<%=ar.isLoggedIn()%>";
function onClickAction(flag){
    if(flag == "newAttachment"){
        document.getElementById("createDocForm").action = "uploadDocument.htm";
        document.getElementById("createDocForm").submit();
    }else if(flag == "linkUrl"){
        document.getElementById("createDocForm").action = "linkURLToProject.htm";
        document.getElementById("createDocForm").submit();
    }else if(flag == "emailReminder"){
        document.getElementById("createDocForm").action = "emailReminder.htm";
        document.getElementById("createDocForm").submit();
    }

}
function test(id1,id2,id3){
        document.getElementById(id1).style.display = "none";
        document.getElementById(id2).style.display = "block";
        document.getElementById(id3).style.display = "none";
    }  
</script>
<link rel="stylesheet" type="text/css" href="<%=ar.baseURL%>yui/build/container/assets/skins/sam/container.css">
<script type="text/javascript" src="<%=ar.baseURL%>yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="<%=ar.baseURL%>yui/build/animation/animation-min.js"></script>
<script type="text/javascript" src="<%=ar.baseURL%>yui/build/dragdrop/dragdrop-min.js"></script>
<script type="text/javascript" src="<%=ar.baseURL%>yui/build/container/container-min.js"></script>
<script type="text/javascript" src="<%=ar.baseURL%>yui/build/dispatcher/dispatcher.js"></script>
<style type="text/css">
        #mycontextmenu ul li {
            list-style:none;
             height:18px;
        }

        .yuimenubaritemlabel,
        .yuimenuitemlabel {
            outline: none;

         }

    </style>
    <div class="content tab01">
        <form name="attachmentForm" id="attachmentForm" action="editDocumentForm.htm">
            <div id="listofpagesdiv1">
                <table id="pagelist">
                    <thead>
                        <tr>
                            <th >Attachment Name</th>
                            <th ><span class="iconArrowDown">Date</span></th>
                            <th >Permission</th>
                            <th style=\"display:none\">Type</th>
                            <th style=\"display:none\">isLinked</th>
                            <th >Comment</th>
                            <th >State</th>
                            <th style=\"display:none\">AID</th>
                            <th style=\"display:none\">Display Name</th>
                            <th style=\"display:none\">downloadLinkCount</th>
                            <th style=\"display:none\">Version</th>
                            <th style=\"display:none\">Encoded Access Name</th>
                            <th>Date diff</th>
                            <th>Visibility</th>
                            <th style=\"display:none\">Ftype</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                    attachmentSectionDisplay(ar, ngb, SectionDef.PUBLIC_ACCESS);
                    if (ar.isMember())
                    {
                        attachmentSectionDisplay(ar, ngb, SectionDef.MEMBER_ACCESS);
                    }
                    %>
                    </tbody>
                </table>
            </div>
            <input type="hidden" name="p" id="p" value="<% ar.writeHtml(ngb.getKey()); %>">
            <input type="hidden" name="aid" id="aid" value="">
        </form>
        <%
        out.flush();
        %>
    </div>
    </div></div></div>
</div>
  <script type="text/javascript">

        var attachmentName= "";
        var description = "";z
        var version = "";
        var aid= "";
        var go ="";

        // Custom function to sort  Column  by another Column

        YAHOO.util.Event.addListener(window, "load", function()
        {
            YAHOO.example.EnhanceFromMarkup = function()
            {
                var myColumnDefs = [
                    {key:"attachmentName",label:"<fmt:message key='nugen.attachment.pagelist.AttachmentName'/>",formatter:downloadAttachmentFormater,sortable:true,resizeable:true},
                    {key:"date",label:"<fmt:message key='nugen.attachment.Date'/>",sortable:true,sortOptions:{sortFunction:sortDates},resizeable:true},
                    {key:"permission",label:"<fmt:message key='nugen.attachment.Permission'/>",sortable:false,resizeable:true},
                    {key:"type",label:"<fmt:message key='nugen.attachment.Type'/>",sortable:false,resizeable:true,hidden:true},
                    {key:"isLinked",label:"isLinked",sortable:true,resizeable:false,hidden:true},
                    {key:"comment",label:"<fmt:message key='nugen.attachment.Comment'/>",sortable:false,resizeable:true},
                    {key:"state",label:"<fmt:message key='nugen.attachment.State'/>",sortable:false,resizeable:true,hidden:true},
                    {key:"aid",label:"AID",sortable:false,resizeable:true,hidden:true},
                    {key:"displayName",label:"Display Name",sortable:false,resizeable:false,hidden:true},
                    {key:"downloadLinkCount",label:"downloadLinkCount",sortable:false,resizeable:false,hidden:true},
                    {key:"version",label:"version",sortable:false,resizeable:false,hidden:true},
                    {key:"encodedAccessName",label:"Encoded Access Name",sortable:false,resizeable:false,hidden:true},
                    {key:"timePeriod",label:"timePeriod",sortable:true,resizeable:false,hidden:true},
                    {key:"visibility",label:"visibility",sortable:true,resizeable:false,hidden:true},
                    {key:"ftype",label:"ftype",sortable:true,resizeable:false,hidden:true}
                    ];

                var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("pagelist"));
                myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
                myDataSource.responseSchema = {
                    fields: [{key:"attachmentName", parser:YAHOO.util.DataSourceBase.parseString},
                            {key:"date"},
                            {key:"permission"},
                            {key:"type"},
                            {key:"isLinked"},
                            {key:"comment"},
                            {key:"state"},
                            {key:"aid"},
                            {key:"displayName"},
                            {key:"downloadLinkCount"},
                            {key:"version"},
                            {key:"encodedAccessName"},
                            {key:"timePeriod", parser:YAHOO.util.DataSource.parseNumber},
                            {key:"visibility"},
                            {key:"ftype"}]
                };

                var oConfigs = {
                    paginator: new YAHOO.widget.Paginator({
                        rowsPerPage: 200
                    }),
                    initialRequest: "sort=attachmentName&results=999999"

                };


                var myDataTable = new YAHOO.widget.DataTable("listofpagesdiv1", myColumnDefs, myDataSource, oConfigs,
                {caption:"",sortedBy:{key:"attachmentName",dir:"desc"}});

                var onContextMenuClick = function(p_sType, p_aArgs, p_myDataTable) {
                    var task = p_aArgs[1];
                  if(task) {
                        // Extract which TR element triggered the context menu

                        var elRow = this.contextEventTarget;
                        elRow = p_myDataTable.getTrEl(elRow);
                        myDataTable2=p_myDataTable;
                        elRow2=elRow;
                        var oRecord = p_myDataTable.getRecord(elRow);
                        attachmentName = oRecord.getData("displayName");
                        aid = oRecord.getData("aid");
                        description = oRecord.getData("comment");
                        version =  oRecord.getData("version");
                        var visibility = oRecord.getData("visibility");
                        if(elRow) {
                            if(task.groupIndex==0){
                                switch(task.index) {

                                    case 0:
                                            document.location = document.getElementById('downloadLink'+oRecord.getData("downloadLinkCount")).getAttribute('href');
                                            break;
                                     case 1:
                                            if(oRecord.getData("ftype") != 'URL'){
                                                if(isLoggedIn == "false"){
                                                    window.location  = "<%=ar.retPath%>t/EmailLoginForm.htm?&msg=<%ar.writeHtml(encodedLoginMsg);%>&go=<%ar.writeURLData(ar.getCompleteURL());%>&openform=getUploadRevisedDocForm&aid="+oRecord.getData("aid")+"";
                                                }else{
                                                    getUploadRevisedDocForm(aid,attachmentName,description, version);
                                                }
                                            }
                                            break;
                                     case 2:
                                            window.location  = "<%=ar.retPath%>t/<%ar.writeHtml(ngb.getKey());%>/$/editDocumentForm.htm?aid="+aid;
                                            break;
                                     case 3:
                                            if(oRecord.getData("ftype") != 'URL'){
                                                window.location  = "<%=ar.retPath%>t/<%ar.writeHtml(ngb.getKey());%>/$/fileVersion.htm?aid="+aid;
                                            }
                                            break;
                                            
                                } 
                            }else if(task.groupIndex==1){              
                                switch(task.index) {                
                                     case 0:
                                            getRenameForm(aid , attachmentName);
                                           break;
                                     case 1:
                                            getPermissionForm(aid , attachmentName , visibility);
                                           break;
                                     case 2:
                                            if(isLoggedIn == "false"){
                                                window.location  = "<%=ar.retPath%>t/EmailLoginForm.htm?&msg=<%ar.writeHtml(encodedDeleteMsg);%>&go=<%ar.writeURLData(ar.getCompleteURL());%>";
                                            }else{
                                                // Delete row upon confirmation
                                                if(confirm("Are you sure you want to remove '" +
                                                    oRecord.getData("displayName") +"' attachment?")) {
                                                    document.getElementById("aid").value = aid;
                                                    document.getElementById("actionType").value = "Remove";
                                                    document.getElementById("attachmentForm").submit();
                                                }
                                            }
                                            break;
                                    }
                                }
                                else if(task.groupIndex==2){
                                    switch( task.index){
                                        case 0:
                                            if(oRecord.getData("ftype") != 'URL'){
                                                window.location  = "<%=ar.retPath%>t/<%ar.writeHtml(ngb.getKey());%>/$/uploadDocument.htm";
                                                //onClickAction('newAttachment');
                                            }
                                            break;
                                        case 1:
                                            myDataTable.sortColumn(myDataTable.getColumn("attachmentName"));
                                            break;
                                        case 2:
                                            myDataTable.sortColumn(myDataTable.getColumn("date"));
                                            break;
                                    }
                                }
                            }
                        }
                    };


                var myContextMenu = new YAHOO.widget.ContextMenu("menuwithgroups",
                        {trigger:myDataTable.getTbodyEl()});

                                // Render the ContextMenu instance to the parent container of the DataTable
                myContextMenu.render("listofpagesdiv1");
                myContextMenu.clickEvent.subscribe(onContextMenuClick, myDataTable);

                myContextMenu.beforeShowEvent.subscribe(onMenuBeforeShow, myContextMenu, true);
                
                myDataTable.subscribe("cellMouseoverEvent", function(oArgs){ 
                 var oRecord = this.getRecord(oArgs.target);
                 var column = this.getColumn(oArgs.target);
                 if(column.key != "attachmentName"){
                    return false;
                 }                 
                 document.getElementById(oRecord.getData("aid")).style.display="";
                }); 
                
                myDataTable.subscribe("rowMouseoverEvent", function(oArgs){myDataTable.unselectAllCells();}); 
              
                myDataTable.subscribe("cellMouseoutEvent", function(oArgs){ 
                    var oRecord = this.getRecord(oArgs.target);
                    document.getElementById(oRecord.getData("aid")).style.display="none";
                    myDataTable.hideColumn("Actions");
                }); 
                          
                 myDataTable.subscribe("cellClickEvent", function(oArgs){ 
                 var oRecord = this.getRecord(oArgs.target);
                 var column = this.getColumn(oArgs.target);
                 if(column.key == "attachmentName"){
                  var xy = YAHOO.util.Event.getXY(oArgs.event);
                    myContextMenu.cfg.setProperty("xy", xy);
                    myContextMenu.contextEventTarget = oArgs.target;
                    myDataTable.unselectAllCells();
                    myDataTable.selectCell(oArgs.target);
                    myContextMenu.show();
                 }
                }); 

                function onMenuBeforeShow(p_sType, p_sArgs, p_oMenu) {
                    var eRow = myDataTable.getTrEl(this.contextEventTarget);
                    var oRecord = myDataTable.getRecord(eRow);
                       var ftype = oRecord.getData("ftype");
                        if(ftype == 'URL'){
                            myContextMenu.getItems()[0].cfg.setProperty("text", "Access Link URL");
                            myContextMenu.getItems()[1].cfg.setProperty("disabled", true);
                            myContextMenu.getItems()[2].cfg.setProperty("text", "Edit Attachment Details");
                            myContextMenu.getItems()[3].cfg.setProperty("disabled", true);
                            myContextMenu.getItems()[4].cfg.setProperty("text", "Rename Attachment");
                            myContextMenu.getItems()[6].cfg.setProperty("text", "Delete Attachment");
                            myContextMenu.getItems()[7].cfg.setProperty("disabled", true);
                            myContextMenu.getItems()[8].cfg.setProperty("disabled", true);
                        }else{
                            myContextMenu.getItems()[0].cfg.setProperty("text", "Download Document");
                            myContextMenu.getItems()[1].cfg.setProperty("disabled", false);
                            myContextMenu.getItems()[2].cfg.setProperty("text", "Edit Document Details");
                            myContextMenu.getItems()[3].cfg.setProperty("disabled", false);
                            myContextMenu.getItems()[4].cfg.setProperty("text", "Rename Document");
                            myContextMenu.getItems()[6].cfg.setProperty("text", "Delete Document");
                            myContextMenu.getItems()[7].cfg.setProperty("disabled", false);
                            myContextMenu.getItems()[8].cfg.setProperty("disabled", false);
                        }
                  }
                


                return {
                    oDS: myDataSource,
                    oDT: myDataTable
                };
            }();
        });
        var downloadAttachmentFormater = function(elCell, oRecord, oColumn, sData)
        {
             /*elCell.innerHTML = '<a id="downloadLink'+oRecord.getData("downloadLinkCount")
                        + '" href="docinfo'+oRecord.getData("aid") +'.htm?version='
                        + oRecord.getData("version")
                        + '"  title=\"Access the content of this document\" onclick=\"return false\">'
                        + oRecord.getData("attachmentName") + '</a>';
                        */
                        
                        var href = '';
            var onclick = '';
            if(oRecord.getData("ftype")=='URL'){
                href = '#';
                onclick = 'return handleURIClick(\''+oRecord.getData("encodedAccessName") +'\');';
            }else{
                href = '<%=ar.baseURL%>t/<%=ngb.getKey()%>/$/a/'+oRecord.getData("encodedAccessName") +'?version='
                        + oRecord.getData("version");
                onclick = 'return handleClick()';
            }
            elCell.innerHTML = '<a id="downloadLink'+oRecord.getData("downloadLinkCount")
                 + '" href="'+href+'"  title=\"Access the content of this document\" onclick=\"'+onclick+'\">'
                 +'<img src="<%=ar.baseURL%>assets/iconDownload.png"/>'         
                 + oRecord.getData("attachmentName") + '</a>';
                        
        };

    </script>
<div id="menuwithgroups" class="yuimenu">
    <div class="bd">
        <ul class="first-of-type">
            <li class="yuimenuitem"><a class="yuimenuitemlabel">Download Document</a></li>
            <li class="yuimenuitem"><a class="yuimenuitemlabel">Upload Revised Document</a></li>
            <li class="yuimenuitem"><a class="yuimenuitemlabel">Edit Document Details</a></li>
            <li class="yuimenuitem"><a class="yuimenuitemlabel">List Versions</a></li>
        </ul>
        <ul>
            <li class="yuimenuitem"><a class="yuimenuitemlabel">Rename Document</a></li>
            <li class="yuimenuitem"><a class="yuimenuitemlabel">Make Public / Make Member Only</a></li>
            <li class="yuimenuitem"><a class="yuimenuitemlabel">Delete Document</a></li>
            <li class="yuimenuitem"><a class="yuimenuitemlabel">Send Document By Email</a></li>
        </ul>
        <ul>
            <li class="yuimenuitem"><a class="yuimenuitemlabel">Upload New Document</a></li>
            <li class="yuimenuitem"><a class="yuimenuitemlabel">Sort by Name</a></li>
            <li class="yuimenuitem"><a class="yuimenuitemlabel">Sort by Date</a></li>
        </ul>
    </div>
</div>
