<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="leaf_ProjectHome.jsp"
%><%!
    String pageTitle="";
%>
<%displayCreatLeaf(ar,ngp);%>

 <div class="content tab01">
    <%
        displayDraftNotes(ar, ngp);
    %>
</div>
    <%
        out.flush();
    %>
</div>
</div>
</div>
</div>
</div>
</div>
