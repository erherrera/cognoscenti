<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="administration.jsp"
%><%

    ar.assertLoggedIn("New Account page should never be accessed when not logged in");
    if (!ar.isSuperAdmin()) {
        throw new Exception("New Account page should only be accessed by Super Admin");
    }
    if (uProf==null) {
        throw new Exception("Program Logic Error: The 'uProf' object must be set up for newAccounts.jsp");
    }

%>
<div class="content tab05" style="display:block;">
    <div class="section_body">
        <div style="height:10px;"></div>
        <div id="deniedAccountPaging"></div>
        <div id="listofpagesdiv">
            <table id="pagelist">
                <thead>
                    <tr>
                        <th>Account Name</th>
                        <th>Account Description</th>
                        <th>Account Status</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                    Iterator forallRequest = deniedAccounts.listIterator();
                    while (forallRequest.hasNext()) {
                        AccountRequest accountDetails = (AccountRequest) forallRequest.next();

                       %>
                       <tr>
                        <td>
                        <%
                            writeHtml(out, accountDetails.getName());
                        %>
                        </td>
                        <td>
                        <%
                            writeHtml(out, accountDetails.getDescription());
                        %>
                        </td>
                        <td>
                        <%
                            writeHtml(out, accountDetails.getStatus());
                        %>
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
<script type="text/javascript">
    YAHOO.util.Event.addListener(window, "load", function()
    {
        YAHOO.example.EnhanceFromMarkup = function()
        {
            var deniedAccountCD = [
                {key:"accountName",label:"Account Name",sortable:true,resizeable:true},
                {key:"members",label:"Account Description",sortable:true,resizeable:true},
                {key:"desc",label:"Account Status",sortable:false,resizeable:true}];

            var deniedAccountDS = new YAHOO.util.DataSource(YAHOO.util.Dom.get("pagelist"));
            deniedAccountDS.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
            deniedAccountDS.responseSchema = {
                fields: [{key:"accountName"},
                        {key:"members"},
                        {key:"desc"}]
            };

            var oConfigs = {
                paginator: new YAHOO.widget.Paginator({
                    rowsPerPage: 200,
                    containers   : 'deniedAccountPaging'
                }),
                initialRequest: "results=999999"
            };

            var deniedAccountDT = new YAHOO.widget.DataTable("listofpagesdiv", deniedAccountCD, deniedAccountDS, oConfigs,
            {caption:"",sortedBy:{key:"bookid",dir:"desc"}});

             // Enable row highlighting
            deniedAccountDT.subscribe("rowMouseoverEvent", deniedAccountDT.onEventHighlightRow);
            deniedAccountDT.subscribe("rowMouseoutEvent", deniedAccountDT.onEventUnhighlightRow);

            return {
                oDS: deniedAccountDS,
                oDT: deniedAccountDT
            };
        }();
    });
</script>
