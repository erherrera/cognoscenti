<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/include.jsp"
%><%
    ar = AuthRequest.getOrCreate(request, response, out);
    ar.assertLoggedIn("Program Logic -- requiredName.jsp is useful only for logged in users and should only appear then.");
    UserProfile up = ar.getUserProfile();

    String fullName = up.getName();

%>
<div>
    <div class="pageHeading">Set Name</div>
    <div class="pageSubHeading">
        Please set your name in your profile
    </div>
    <br/>
    <table width="600">
    <tr>
        <td class="linkWizardHeading">You Need A Name:</td>
    </tr>
    <tr style="height:50px;padding:15px">
    <td style="padding:15px">Before going any further, you need to specify your display name.  This is the name
                             that will be displayed to others to indicate things you have done or own.
                             Please specify your full name because there may be many people using this server.</td>
    </tr>
    <form action="<%= ar.retPath %>t/requiredName.form" method="post">
    <input type="hidden" name="go" value="<%= ar.getCompleteURL() %>">

    <tr style="height:50px;padding:15px">
         <td style="padding:15px"><input type="text" name="dName" size="50"><input type="submit" value="Set Display Name"></td>
    </tr>

    <tr style="height:50px;padding:15px">
         <td style="padding:15px"></td>
    </tr>

    </form>
    </table>
</div>

