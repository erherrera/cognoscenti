<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="UserProfile.jsp"
%><%@page import="org.socialbiz.cog.spring.AccountRequest"
%><%@page import="org.socialbiz.cog.AccountReqFile"
%><%
/*

Required Parameters:

    1. memberOfAccounts : This parameter provide the list of those accounts which belong to user.

*/
    ar.assertLoggedIn("You must be logged in to see user account information");

    //note, this page only displays info for the current logged in user, regardless of URL
    UserProfile  userProfile =ar.getUserProfile();
    if (userProfile==null)
    {
        //this should never happen, and if it does it is not the users fault
        throw new ProgramLogicError("user profile setting is null.");
    }

    List<NGBook> memberOfAccounts = (List<NGBook>) request.getAttribute("memberOfAccounts");
    if (memberOfAccounts==null)
    {
        //this should never happen, and if it does it is not the users fault
        throw new ProgramLogicError("memberOfAccounts setting is null.");
    }



%>
<div class="content tab04" style="display:block;">
    <div class="section_body">
        <div style="height:10px;"></div>
<%
if(memberOfAccounts.size()>0) {
%>
        <div class="generalHeadingBorderLess">Choose Account for Project</div>
        <div class="generalContent">
            <div id="accountPaging"></div>
            <div id="accountsContainer">
                A project must be created inside an account.
                Choose from the list below the account you would like to create
                this new project in.
                <br/>
                <br/>
                <%
                for (NGBook account : memberOfAccounts) {
                    String accountLink =ar.baseURL+"t/"+account.getKey()+"/$/accountCreateProject.htm";
                    %>
                    <form action="<%ar.writeHtml(accountLink);%>">
                    <input type="submit" value="<%ar.writeHtml(account.getName());%>" class="inputBtn">
                    </form><br/>
                <%
                }
                %>
            </div>
        </div>
<%
}else{
%>
        <div class="generalContent">
            To create a project, you must have a space for that
            project in an account.  You have no accounts at this time.
            Each project has to belong to an account, and you can only create a
            project in an account if you have been given access to do so.
            <br/>
            Create an Account in order to create a project<br/>.
            <form action="userAccounts.htm">
            <input type="submit" value="View Your Accounts" class="inputBtn">
            </form>
        </div>
<%
}
%>
    </div>
</div>
