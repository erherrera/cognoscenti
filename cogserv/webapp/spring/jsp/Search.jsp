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
%><%
/*
Required parameter:

    1. b : This parameter is used to retrieve specific NGBook.

Optional Parameter:

    1. qs   : This parameter is query string, on the basis of it search is performed.
    2. pf   : This optional parameter is used to specify who is performing search like member, admin or all(public).
*/
    ar.assertLoggedIn("Can't perform search.");
    String b          = ar.reqParam("b");
    String qs         = ar.defParam("qs", "");
    String pf         = ar.defParam("pf", "all");

%><%!String pageTitle = "";%><%

    boolean isGlobalScope = false;

    if(b.equals("All Books"))
    {
        isGlobalScope = true;
        ngb = NGPageIndex.getAccountByKeyOrFail("mainbook");
    }
    else
    {
        //throws an exception if the book can not be found
        ngb = NGPageIndex.getAccountByKeyOrFail(b);
    }

    String servletURL = ar.baseURL + "servlet/DataFeedServlet?op=SEARCH&qs="
                       + URLEncoder.encode(qs, "UTF-8") + "&b="
                       + URLEncoder.encode(b,"UTF-8")  + "&pf="
                       + URLEncoder.encode(pf,"UTF-8") + "&u=new" ;

%>

<br/>
<div class="generalArea">
<form action="Search.htm" method="get" name="searchForm">
  <div class="generalHeading"><fmt:message key="nugen.serach.Headings"/> &nbsp;</div>
  <div class="generalContent">
    <div style="background-color:#f5f5f5; padding:10px">
      <table>
        <tr height="30px">
          <td width="120px"><b><fmt:message key="nugen.serach.filter.Headings"/></b></td>
          <td></td>
          <td>
            <select style="width:200px" name="pf">
              <option value="all" selected><fmt:message key="nugen.serach.filter.allProjects"/></option>
              <option value="member"><fmt:message key="nugen.serach.filter.userAsMemberProjects"/></option>
              <option value="admin"><fmt:message key="nugen.serach.filter.userAsOwnerProjects"/></option>
            </select> &nbsp;
            <select style="width:200px" name="b">
                <option value="All Books" selected="selected"><fmt:message key="nugen.serach.filter.AllAccounts"/></option>
                <%
                for(NGBook account : NGBook.getAllAccounts())
                {
                    String aname = account.getName();
                    String akey = account.getKey();
                    %>
                        <option value="<%ar.writeHtml(akey);%>"><%ar.writeHtml(aname);%></option>
                    <%
                }
                %>

            </select>
          </td>
        </tr>
        <tr>
          <td width="120px"><b><fmt:message key="nugen.serach.textbox.label"/></b></td>
          <td></td>
          <td style="padding-bottom:6px"> <input type="text"  name="qs" size="46" value="<%ar.writeHtml(qs);%>"/>
        </tr>
        <tr>
          <td width="120px"></td>
          <td></td>
          <td><button type="submit" name="action" value="Search"><fmt:message key="nugen.serach.button.label"/></button></td>
        </tr>
      </table>




      <input type="hidden" name="encodingGuard" value="%E6%9D%B1%E4%BA%AC"/>
      <input type="hidden" name="b"          value="<%ar.writeHtml(b);%>">
    </div>
  </div>
</form>
</div>

<script>

    function resetControls() {
        document.searchForm.qs.value="";
        document.searchForm.qs.focus();
    }

    function trim(sString)
    {
        if (sString.length==0)
            return sString;

        while (sString.substring(0,1) == ' ') {
            sString = sString.substring(1, sString.length);
        }
        while (sString.substring(sString.length-1, sString.length) == ' '){
            sString = sString.substring(0,sString.length-1);
        }
        return sString;
    }
</script>

<!-- Display the search results here -->
<br/>

<div class="yui-skin-sam">


<div class="generalHeading"><label id="resultsLbl">&nbsp;</label></div>
<div id="container">
  <div id="searchresultdiv"></div>
</div>
<div class="generalContent"><a href="<%ar.writeHtml(servletURL);%>"><fmt:message key="nugen.serach.result.xmllink.label"/></a></div>
</div>
</div>

<!-- Content Area Ends Here -->


<script type="text/javascript">

    function performSearchAndDisplayResults()
    {

        // just return if the search string is empty.
        if(trim(document.searchForm.qs.value) == "")
        {
            return;
        }

        // for the loading Panel
        YAHOO.namespace("example.container");
        if (!YAHOO.example.container.wait)
        {
            // Initialize the temporary Panel to display while waiting for external content to load
            YAHOO.example.container.wait =
                    new YAHOO.widget.Panel("wait",
                                            { width: "240px",
                                              fixedcenter: true,
                                              close: false,
                                              draggable: false,
                                              zindex:4,
                                              modal: true,
                                              visible: false
                                            }
                                        );

            YAHOO.example.container.wait.setHeader("Loading, please wait...");
            YAHOO.example.container.wait.setBody("<img src=\"<%=ar.retPath%>loading.gif\"/>");
            YAHOO.example.container.wait.render(document.body);
        }
        // Show the loading Panel
        YAHOO.example.container.wait.show();

        // for data table.
        YAHOO.example.Local_XML = function()
        {
            var myDataSource, myDataTable, oConfigs;

            var connectionCallback = {
                success: function(o) {

                    // hide the loading panel.
                    YAHOO.example.container.wait.hide();
                    var xmlDoc = o.responseXML;

                    var formatUrl = function(elCell, oRecord, oColumn, sData)
                    {
                        elCell.innerHTML = "<a href='<%=ar.retPath%>" + oRecord.getData("PageLink") + "' target='_blank'>" + oRecord.getData("NoteSubj") + "</a>";
                    };
                    var formatUpdater = function(elCell, oRecord, oColumn, sData)
                    {
                        elCell.innerHTML = "<a href='<%=ar.retPath%>v/" + oRecord.getData("LastModifiedBy") + "/userSettings.htm' target='_blank'>" + oRecord.getData("LastModifiedName") + "</a>";
                    };

                    var formatNameUrl = function(elCell, oRecord, oColumn, sData)
                    {
                        var uLink = oRecord.getData("UserLink");
                        if(uLink == "unknown")
                        {
                            elCell.innerHTML = sData;
                        }
                        else
                        {
                            elCell.innerHTML = "<a href='<%=ar.retPath%>" + uLink + "' target='_blank'>" + sData + "</a>";
                        }
                    };
                    var myColumnDefs = [
                        {key:"No",label:"<fmt:message key="nugen.serach.result.table.col.Number"/>",
                                  formatter:YAHOO.widget.DataTable.formatNumber,sortable:true,resizeable:true},
                        {key:"NoteSubj",label:"Note", formatter:formatUrl, sortable:true,resizeable:true},
                        {key:"PageName",label:"Project", sortable:true,resizeable:true},
                        {key:"LastModifiedBy",label:"<fmt:message key="nugen.serach.result.table.col.LastUpdated"/>",
                                  formatter:formatUpdater, sortable:true, resizeable:true},
                        {key:"LastModifiedTime",label:"<fmt:message key="nugen.serach.result.table.col.lastUpdatedTime"/>",
                                  sortable:true,sortOptions:{sortFunction:sortDates},resizeable:true},
                        {key:"timePeriod",label:"timePeriod",sortable:true, resizeable:true,hidden:true}
                    ];

                    myDataSource = new YAHOO.util.DataSource(xmlDoc);
                    myDataSource.responseType = YAHOO.util.DataSource.TYPE_XML;
                    myDataSource.responseSchema = {
                        resultNode: "Result",

                        fields: [{key:"No", parser:"number"},
                          {key:"PageKey"},
                          {key:"PageName"},
                          {key:"PageLink"},
                          {key:"BookName"},
                          {key:"NoteSubj"},
                          {key:"LastModifiedBy"},
                          {key:"LastModifiedName"},
                          {key:"LastModifiedTime"},
                          {key:"UserLink"},
                          {key:"timePeriod" , parser:YAHOO.util.DataSource.parseNumber}]
                    };


                oConfigs = { paginator: new YAHOO.widget.Paginator({rowsPerPage:160}), initialRequest:"results=99999999"};

                myDataTable = new YAHOO.widget.DataTable(
                                  "searchresultdiv",
                                  myColumnDefs,
                                  myDataSource,
                                  oConfigs,
                                  {caption:"",sortedBy:{key:"No",dir:"desc"}}
                    );


                },
                failure: function(o)
                {
                    // hide the loading panel.
                    YAHOO.example.container.wait.hide();
                }
            };

            var servletURL = "<%ar.write(servletURL);%>";

            var getXML = YAHOO.util.Connect.asyncRequest("GET",servletURL, connectionCallback);

            return {
                oDS: myDataSource,
                oDT: myDataTable
            };
        }();

        document.getElementById("resultsLbl").firstChild.nodeValue = "<fmt:message key="nugen.serach.result.heading"/> " + document.searchForm.qs.value;
    }

    var actBtn1 = new YAHOO.widget.Button("actBtn1");
    var actBtn2 = new YAHOO.widget.Button("actBtn2");

    actBtn1.on('click', performSearchAndDisplayResults);
    actBtn2.on('click', resetControls);

</script>

<%
    if (qs != null) {
%>
    <script>
        performSearchAndDisplayResults();
    </script>
<%
    }
%>

</div>

<%@ include file="functions.jsp"%>

