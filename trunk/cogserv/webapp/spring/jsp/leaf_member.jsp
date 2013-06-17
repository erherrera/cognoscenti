<%@page errorPage="/spring/jsp/error.jsp"
%><%@page import="org.socialbiz.cog.RoleRequestRecord"
%><%@page import="org.socialbiz.cog.NGRole"
%><%@ include file="leaf_ProjectHome.jsp"
%><%!
    String pageTitle="";
%>
<%
    displayCreatLeaf(ar,ngp);
%>

<div class="content tab01">
    <div class="leafLetArea">
    <%
        displayAllLeaflets(ar, ngp, SectionDef.MEMBER_ACCESS);
    %>
    </div>
    <%
         out.flush();
    %>
</div>
</div>
