<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/include.jsp"
%><%@page import="org.socialbiz.cog.TemplateRecord"
%><%@page import="org.socialbiz.cog.AccountReqFile"
%><%@page import="org.socialbiz.cog.spring.AccountRequest"
%><%
/*
Required parameter:

    1. userKey : This is the key of user .

*/

    String userKey = ar.reqParam("userKey");

%><%!String pageTitle="";%><%
    request.setCharacterEncoding("UTF-8");
    UserProfile  uProf = UserManager.getUserProfileByKey(userKey);
%>
<div class="pageHeading">Request for a New Account Space</div>
<div class="pageSubHeading">From here you can request to create a new account from where you can create & handle multiple projects.</div>
<div class="generalSettings">
    <div id="requestAccount">
        <form name="requestNewAccount" action="<%=ar.retPath%>v/<%ar.writeHtml(uProf.getKey());%>/accountRequests.form" method="post">
            <input type="hidden" name="action" id="action" value="">
            <table width="100%" border="0" cellpadding="0" cellspacing="0">
                <tr><td style="height:10px"></td></tr>
                <tr>
                    <td class="gridTableColummHeader_2"><b>Account Name:<span style="color:red">*</span></b></td>
                    <td style="width:20px;"></td>
                    <td><input type="text" name="accountName" id="accountName" class="inputGeneral" /></td>
                </tr>
                <tr><td style="height:10px"></td></tr>
                <tr>
                    <td class="gridTableColummHeader_2" valign="top"><b>Account Description:<span style="color:red">*</span></b></td>
                    <td style="width:20px;"></td>
                    <td><textarea name="accountDesc" id="accountDesc" class="textAreaGeneral" rows="4"></textarea></td>
                </tr>
                <tr><td style="height:10px"></td></tr>
                <tr>
                    <td class="gridTableColummHeader_2"></td>
                    <td style="width:20px;"></td>
                    <td><input type="submit" value="<fmt:message key='nugen.button.general.submit'/>" class="inputBtn"  onclick="javascript:requestAccount('Submit')"/>
                    &nbsp;<input type="submit" value="<fmt:message key='nugen.button.general.cancel'/>" class="inputBtn"  onclick="javascript:requestAccount('Cancel')"/>
                    </td>
                </tr>
            </table>
        </form>
    </div>
    <script>
        function requestAccount(action){
            document.getElementById("action").value=action;
        }
    </script>
<%@ include file="functions.jsp"%>