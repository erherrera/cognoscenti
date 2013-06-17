<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/include.jsp"
%><%@page import="org.socialbiz.cog.RssServlet"
%>
<head>
    <!-- for calender -->
    <script language="javascript">
       var userTasks=true;
    </script>
</head>
<%!String pageTitle = "";%><%
        request.setCharacterEncoding("UTF-8");
    ar.assertLoggedIn("Can't retrieve the Task list.");

    UserProfile uProf = ar.getUserProfile();

    String rssLink = "Tasks.rss?user="+ java.net.URLEncoder.encode(uProf.getUniversalId(), "UTF-8");
    String loggingUserName=uProf.getName();
%>
    <script>
        var specialSubTab = '<fmt:message key="${requestScope.subTabId}"/>';

        var tab0_userTasks = '<fmt:message key="nugen.usertasks.subtab.active.tasks"/>';
        var tab1_userTasks = '<fmt:message key="nugen.usertasks.subtab.completed.tasks"/>';
        var tab2_userTasks = '<fmt:message key="nugen.usertasks.subtab.future.tasks"/>';
        var tab3_userTasks = '<fmt:message key="nugen.usertasks.subtab.all.tasks"/>';
        var tab4_userTasks = '<fmt:message key="nugen.usertasks.subtab.status.report"/>';
        var retPath ='<%=ar.retPath%>';

    </script>
<body class="yui-skin-sam">
    <!-- for the tab view -->
    <div id="container">
        <div>
            <ul id="subTabs" class="menu">

            </ul>
        </div>
        <script>
            createSubTabs("_userTasks");
        </script>
    </div>
    
    <!-- Display the search results here -->

</body>
<%@ include file="functions.jsp"%>