<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="UserProfile.jsp"
%><%@page import="org.socialbiz.cog.spring.AccountRequest"
%><%@page import="org.socialbiz.cog.AccountReqFile"
%><%
/*

Required Parameters:

    1. memberOfAccounts : This parameter provide the list of those accounts which belong to user.

*/

    List memberOfAccounts = (List) request.getAttribute("memberOfAccounts");
    if (memberOfAccounts==null)
    {
        //this should never happen, and if it does it is not the users fault
        throw new ProgramLogicError("memberOfAccounts setting is null.");
    }

    ar.assertLoggedIn("You must be logged in to see user account information");

    //note, this page only displays info for the current logged in user, regardless of URL
    UserProfile  userProfile =ar.getUserProfile();
    if (userProfile==null)
    {
        //this should never happen, and if it does it is not the users fault
        throw new ProgramLogicError("user profile setting is null.");
    }

    List<AccountRequest> allaccount = AccountReqFile.getAccountsStatus();
    boolean isSuper = ar.isSuperAdmin();
    List<AccountRequest> superRequests = new ArrayList<AccountRequest>();
    List<AccountRequest> myAccountRequests = new ArrayList<AccountRequest>();
    for (AccountRequest accountDetails: allaccount)
    {
        if(userProfile.hasAnyId(accountDetails.getUniversalId()))
        {
            myAccountRequests.add(accountDetails);
        }
        if (isSuper && accountDetails.getStatus().equalsIgnoreCase("requested"))
        {
            superRequests.add(accountDetails);
        }
    }


    List accReqs = userProfile.getAllAccountRequests();
    if (accReqs==null)
    {
        //this should never happen, and if it does it is not the users fault
        throw new ProgramLogicError("user profile returned a null for account requests.");
    }
    ngb = null;



%>
<div class="content tab04" style="display:block;">
    <div class="section_body">
        <div style="height:10px;"></div>
        <%
        if(memberOfAccounts.size()>0) {
        %>
        <div class="generalHeadingBorderLess">List of Accounts</div>
        <div class="generalContent">
            <div id="accountPaging"></div>
            <div id="accountsContainer">
                <table id="accountList">
                    <thead>
                        <tr>
                            <th>No</th>
                            <th>Account Name</th>
                            <th>Account Description</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                    int i=0;
                    Iterator it = memberOfAccounts.iterator();
                    while (it.hasNext()) {
                        i++;
                        String rowStyleClass="tableBodyRow even";
                        if(i%2 == 0){
                            rowStyleClass = "tableBodyRow odd";
                        }

                        NGBook account  = (NGBook) it.next();
                        String accountLink =ar.baseURL+"t/"+account.getKey()+"/$/public.htm";

                    %>
                        <tr>
                           <td>
                                <%=(i)%>
                            </td>
                            <td>
                                <a href="<%ar.writeHtml(accountLink); %>" title="navigate to the account"><%writeHtml(out, account.getName());%></a>
                            </td>
                            <td>
                                <%writeHtml(out, account.getDescription());%>
                            </td>
                        </tr>
                       <%
                       }
                    %>
                    </tbody>
                </table>
            </div>
            <br/>
            <br/>
            <div>
                <form name="createAccountForm" method="GET" action="requestAccount.htm">
                    <input type="submit" class="inputBtn"  Value="Request New Account">
                </form>
            </div>
        </div>
        <%
        }else{
        %>
        <div class="guideVocal">
            <%
            if(accReqs.size()>0) {
            %>
            <fmt:message key="requestedaccount.message.0"/> <%=accReqs.size()%>&nbsp;
            <fmt:message key="accounts.title"/></p>
            <%
            }else{
            %>
            <fmt:message key="noaccount.message.0"/>
            <%
            }
            %>
            <fmt:message key="noaccount.message.1"/>
            <fmt:message key="noaccount.message.2"/>
            <form name="createAccountForm" method="GET" action="requestAccount.htm">
                <input type="submit" class="inputBtn"  Value="Request New Account">
            </form>
            <fmt:message key="noaccount.message.3"/>
            <fmt:message key="noaccount.message.4"/>
        </div>
        <%
        }
        //only produce this section if you have some outstanding requests
        if (myAccountRequests.size()>0) {
        %>
        <div class="generalHeadingBorderLess"><br/>Status of Your Account Requests</div>
        <div class="generalContent">
            <div id="accountRequestPaging"></div>
            <div id="accountRequestDiv">
                <table id="pagelistrequest">
                    <thead>
                        <tr>
                            <th>Proposed Name</th>
                            <th>Description</th>
                            <th>Current Status</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%

                    for (AccountRequest accountDetails : myAccountRequests)
                    {
                        String accountLink =ar.baseURL+"t/"+accountDetails.getAccountId()+"/$/public.htm";
                        %><tr><td><%
                        if(accountDetails.getStatus().equalsIgnoreCase("Granted")){
                            %><a href="<%ar.writeHtml(accountLink); %>"><%
                            ar.writeHtml(accountDetails.getName());
                            %></a><%
                        }else{
                            ar.writeHtml(accountDetails.getName());
                        }
                        %></td>
                        <td><%
                        ar.writeHtml(accountDetails.getDescription());
                        %></td>
                        <td><%
                        ar.writeHtml(accountDetails.getStatus());
                        %></td></tr><%
                     }
                     %>
                    </tbody>
                </table>
            </div>
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
                var accountColumnDefs = [
                    {key:"no",label:"No",formatter:YAHOO.widget.DataTable.formatNumber,sortable:true,resizeable:true},
                    {key:"accountname",label:"Account Name",sortable:true,resizeable:true},
                    {key:"description",label:"Account Description",sortable:true,resizeable:true}];

                var accountDS = new YAHOO.util.DataSource(YAHOO.util.Dom.get("accountList"));
                accountDS.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
                accountDS.responseSchema = {
                    fields: [{key:"no", parser:"number"},
                            {key:"accountname"},
                            {key:"description"}]
                };

                var accountConfigs = {
                    paginator: new YAHOO.widget.Paginator({
                        rowsPerPage: 200,
                        containers:'accountPaging'
                    }),
                    initialRequest: "results=999999"
                };
                var accountDT = new YAHOO.widget.DataTable("accountsContainer", accountColumnDefs, accountDS, accountConfigs,
                {caption:"",sortedBy:{key:"no",dir:"desc"}});
                 // Enable row highlighting
                accountDT.subscribe("rowMouseoverEvent", accountDT.onEventHighlightRow);
                accountDT.subscribe("rowMouseoutEvent", accountDT.onEventUnhighlightRow);

                return {
                    oDS: accountDS,
                    oDT: accountDT
                };
            }();
        });

        YAHOO.util.Event.addListener(window, "load", function()
        {
            YAHOO.example.EnhanceFromMarkup = function()
            {
                var acountRequestCD = [
                    {key:"accountName",label:"Proposed Name",sortable:true,resizeable:true},
                    {key:"members",label:"Description",sortable:true,resizeable:true},
                    {key:"desc",label:"Current Status",sortable:false,resizeable:true}];

                var accountRequestDS = new YAHOO.util.DataSource(YAHOO.util.Dom.get("pagelistrequest"));
                accountRequestDS.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
                accountRequestDS.responseSchema = {
                    fields: [{key:"accountName"},
                            {key:"members"},
                            {key:"desc"}]
                };

                var accountRequestConfigs = {
                    paginator: new YAHOO.widget.Paginator({
                        rowsPerPage: 200,
                        containers:'accountRequestPaging'
                    }),
                    initialRequest: "results=999999"
                };


                var accountRequestDT = new YAHOO.widget.DataTable("accountRequestDiv", acountRequestCD, accountRequestDS, accountRequestConfigs,
                {caption:"",sortedBy:{key:"bookid",dir:"desc"}});

                 // Enable row highlighting
                accountRequestDT.subscribe("rowMouseoverEvent", accountRequestDT.onEventHighlightRow);
                accountRequestDT.subscribe("rowMouseoutEvent", accountRequestDT.onEventUnhighlightRow);

                return {
                    oDS: accountRequestDS,
                    oDT: accountRequestDT
                };
            }();
        });
</script>
