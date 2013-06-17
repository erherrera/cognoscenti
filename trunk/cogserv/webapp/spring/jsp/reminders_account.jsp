<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/attachment_forms_account.jsp"
%>
<script type="text/javascript">
     var isLoggedIn = "<%=ar.isLoggedIn()%>";
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
    <%if (!ar.isLoggedIn())
    {
    %>
    <div class="generalArea">
        <div class="generalContent">
            In order to see this section, you need to be logged in.
        </div>

    </div>
    <%
    }else if (ar.isMember())
    {
    %>

    <div class="generalHeading">
        <fmt:message key="nugen.attachment.AttachmentReminder"/>
    </div>
    <div class="generalContent">
        <div id="container">
            <div id="listofpagesdiv">
                <table id="pagelist2">
                    <thead>
                        <tr>
                            <th><fmt:message key="nugen.attachment.pagelist.AttachmentName"/></th>
                            <th><fmt:message key="nugen.attachment.pagelist.ResendEmail"/></th>
                            <th><fmt:message key="nugen.attachment.pagelist.Recipient"/></th>
                        </tr>
                    </thead>
                    <tbody>
        <%
        ReminderMgr rMgr = ngb.getReminderMgr();
        Vector rVec = rMgr.getOpenReminders();
        Enumeration e2 = rVec.elements();
        int reminderCount = 0;
        while(e2.hasMoreElements())
        {
            ReminderRecord rRec = (ReminderRecord)e2.nextElement();
            reminderCount++;
            String link = ar.retPath + "RemindAttachment.jsp?p="+URLEncoder.encode(ngb.getKey(), "UTF-8")
                    +"&rid="+URLEncoder.encode(rRec.getId(), "UTF-8");
            String update = ar.retPath + "ReminderEdit.jsp?p="+URLEncoder.encode(ngb.getKey(), "UTF-8")
                    +"&rid="+URLEncoder.encode(rRec.getId(), "UTF-8");
            String email = "sendemailReminder.htm?rid="+URLEncoder.encode(rRec.getId(), "UTF-8");
            String dName = rRec.getSubject();
            if (dName==null || dName.length()==0)
            {
                dName = "Reminder"+rRec.getId();
            }
            %>
                        <tr>
                            <td><%writeHtml(out, rRec.getFileDesc());%></td>
                            <td><img src="<%=ar.retPath%>emailIcon.gif" title="<fmt:message key='nugen.attachment.ResendReminder'/>"></td>
                            <td>
                                To: <%writeHtml(out, rRec.getAssignee());%><br/>
                                <%SectionUtil.nicePrintTime(out, rRec.getModifiedDate(), ar.nowTime);%>
                            </td>
                        </tr>
        <%
        }
        %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <%
    }
    %>
</div>
    <script type="text/javascript">

            YAHOO.util.Event.addListener(window, "load", function()
        {

            YAHOO.example.EnhanceFromMarkup = function()
            {
                var myColumnDefs = [
                    {key:"attachmentName",label:"<fmt:message key='nugen.attachment.pagelist.AttachmentName'/>",sortable:true,resizeable:true},
                    {key:"resendEmail",label:"<fmt:message key='nugen.attachment.pagelist.ResendEmail'/>",sortable:false,resizeable:true},
                    {key:"to",label:"<fmt:message key='nugen.attachment.pagelist.Recipient'/>",sortable:true,resizeable:true}];

                var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("pagelist2"));
                myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
                myDataSource.responseSchema = {
                    fields: [{key:"attachmentName"},
                            {key:"resendEmail"},
                            {key:"to"}]
                };

                 var oConfigs = {
                    paginator: new YAHOO.widget.Paginator({
                        rowsPerPage: 200
                    }),
                    initialRequest: "results=999999"

                };


                var myDataTable = new YAHOO.widget.DataTable("listofpagesdiv", myColumnDefs, myDataSource, oConfigs,
                {caption:"",sortedBy:{key:"attachmentName",dir:"to"}});

                return {
                    oDS: myDataSource,
                    oDT: myDataTable
                };
            }();
        });
    </script>