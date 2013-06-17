<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="leaf_ProjectHome.jsp"
%><%!
    String pageTitle="";
%>
<%displayCreatLeaf(ar,ngp);%>

    <div class="content tab01">
    <%
    displayAllLeaflets(ar, ngp, SectionDef.PUBLIC_ACCESS);
    %>
    </div>
