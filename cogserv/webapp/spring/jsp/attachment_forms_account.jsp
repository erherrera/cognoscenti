<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/include.jsp" 
%><%@ include file="/spring/jsp/functions.jsp"
%><%
/*
Required parameter:
    
    1. accountId : This is the id of an account and here it is used to retrieve NGBook.  

*/
    
    String bookId = ar.reqParam("accountId");

%><%!
    String pageTitle="";
%><%

    NGBook ngb = (NGBook)NGPageIndex.getContainerByKeyOrFail(bookId);
    ar.setPageAccessLevels(ngb);

    int documentCounts = NGWebUtils.getDocumentCount(ngb, SectionDef.PUBLIC_ACCESS);
    if(ar.isLoggedIn()){
        documentCounts += NGWebUtils.getDocumentCount(ngb, SectionDef.MEMBER_ACCESS);;
    }
    ReminderMgr reminderMgr = ngb.getReminderMgr();
    Vector openReminders = reminderMgr.getOpenReminders();
    int reminderCounts = 0;
    if(openReminders != null){
        reminderCounts = openReminders.size();
    }
    UserProfile uProf = ar.getUserProfile();
%>
<script>
var retPath ='<%=ar.retPath%>';
var accountId='<%=bookId%>';

function trim(s) {
    var temp = s;
    return temp.replace(/^s+/,'').replace(/s+$/,'');
}

function check(id, label){
    var val = "";
    if(document.getElementById(id) != null){
        val = trim(document.getElementById(id).value);
        if(val == ""){
            alert(label+" field is empty.");
            return false;
        }else{
            return true;
        }
    }
    return false;
}
function writeAccessName(filepath){
    if(filepath != ""){
        var accessName = filepath.match(/[^\/\\]+$/);
        document.getElementById("name").value = accessName;
    }
}
function cancel(){
    location.href = "account_attachment.htm";
}
var specialSubTab = '<fmt:message key="${requestScope.subTabId}"/>';
var tab0_upload_attachments = '<fmt:message key="nugen.projecthome.subtab.upload.document"/>';
var tab1_upload_attachments = '<fmt:message key="nugen.projecthome.subtab.link.url.to.project"/>';
var tab2_upload_attachments = '<fmt:message key="nugen.projecthome.subtab.emailreminder"/>';
var tab3_upload_attachments = '<fmt:message key="nugen.projecthome.subtab.documents"/> (<%=documentCounts%>)';
var tab4_upload_attachments = '<fmt:message key="nugen.projecthome.subtab.reminders"/> (<%=reminderCounts%>)';


</script>
<body class="yui-skin-sam">
    <div id="mainContent">
        <div class="generalArea">
            <div class="generalContent">
                <div id="container">
                    <ul id="subTabs" class="menu">
                    </ul>
                    <script>
                        createAccountSubTabs("_document");
                    </script>
</body>