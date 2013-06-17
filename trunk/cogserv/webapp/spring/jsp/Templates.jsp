<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="UserHome.jsp"
%>
<div class="content tab04" style="display:block;">
    <div class="section_body">
        <div style="height:10px;"></div>
        <div id="pagingTemplate"></div>
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
</div>
<script type="text/javascript">
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
                    containers   : 'pagingTemplate'
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
</script>