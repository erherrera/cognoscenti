<%@page errorPage="/spring/jsp/error.jsp"
%><%@page import="org.socialbiz.cog.AuthRequest"
%><%@page import="org.socialbiz.cog.NGBook"
%><%@page import="org.socialbiz.cog.NGPage"
%><%@page import="org.socialbiz.cog.NGPageIndex"
%><%@page import="org.socialbiz.cog.SectionUtil"
%><%@page import="org.socialbiz.cog.RUElement"
%><%@page import="org.socialbiz.cog.SectionDef"
%><%@page import="org.socialbiz.cog.SectionFormat"
%><%@page import="org.socialbiz.cog.SectionUtil"
%><%@page import="org.socialbiz.cog.UserProfile"
%><%@page import="org.socialbiz.cog.UtilityMethods"
%><%@page import="java.io.Writer"
%><%@page import="java.net.URLEncoder"
%><%@ include file="/spring/jsp/include.jsp"
%><%@page import="org.socialbiz.cog.SearchResultRecord"
%><%
/*

Parameters:

    1. searchResultRecord : This request attribute is used to get the Array of search result (SearchResultRecord[]).

*/

    List<SearchResultRecord> searchResults  = (List<SearchResultRecord>)request.getAttribute("searchResults");
%>

<body class="yui-skin-sam">
    <div class="generalHeading"><label id="resultsLbl">Search for Content</label></div>
    <br/>
    <div id="container">
        <form id="searchForm" name="searchForm" action="<%=ar.retPath%>t/searchPublicNotes.htm">
             <table   cellpadding="0" cellspacing="0">
                 <tr>
                     <td>
                         <input type="text" style="height:18px;color:#666;font-size:12px;border:1px solid #ccc;"  id="searchText" size="32px" name="searchText" />
                     </td>
                     <td>&nbsp;
                     <input type="button" class="inputBtn" onclick="return onSearch();" value="  Search  ">
                     </td>
                 </tr>
             </table>
         </form>
    </div>

    <br/><div class="seperator">&nbsp;</div>
    <div class="generalHeading"><label id="resultsLbl">Search Result</label></div>
        <div id="container">
            <div id="searchresultdiv">
                    <table id="searchresultTable">
                        <thead>
                             <tr>
                                 <th >Project/Account Name</th>
                                 <th >Note Subject</th>
                             </tr>
                         </thead>
                         <tbody>
                        <%
                        for(SearchResultRecord srr : searchResults){
                        %>
                            <tr>
                                <td>
                                    <a href="#" onclick="return goToLink('<%=ar.baseURL%><%=srr.getPageLink()%>')" title='Access Project/Site'>
                                        <%ar.writeHtml(srr.getPageName());%>
                                    </a>
                                </td>
                                <td>
                                    <a href="#" onclick="return goToLink('<%=ar.baseURL%><%=srr.getNoteLink()%>')" title='Access Note'>
                                        <%ar.writeHtml(srr.getNoteSubject());%>
                                    </a>
                                </td>
                            </tr>


                        <%}
                        %>
                         </tbody>
                    </table>
                </div>
        </div>


<%@ include file="functions.jsp"%>
 </body>

<script type="text/javascript">
        function goToLink(link){
            document.location = link;
            return true;
        }
        YAHOO.util.Event.addListener(window, "load", function()
        {

            YAHOO.example.EnhanceFromMarkup = function()
            {
                var myColumnDefs = [
                    {key:"Project_Account_Name",label:"Project/Site Name",sortable:true,resizeable:true},
                    {key:"Note_Subject",label:"Note Subject",sortable:true,resizeable:true}
                    ];

                var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("searchresultTable"));
                myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
                myDataSource.responseSchema = {
                    fields: [
                            {key:"Project_Account_Name"},
                            {key:"Note_Subject"}
                            ]
                };

                var oConfigs = {
                    paginator: new YAHOO.widget.Paginator({
                        rowsPerPage: 200
                    }),
                    initialRequest: "results=999999"

                };


                var myDataTable = new YAHOO.widget.DataTable("searchresultdiv", myColumnDefs, myDataSource, oConfigs,
                {caption:"",sortedBy:{key:"Project_Account_Name",dir:"Project_Account_Name"}});

                return {
                    oDS: myDataSource,
                    oDT: myDataTable
                };
            }();
        });
</script>
