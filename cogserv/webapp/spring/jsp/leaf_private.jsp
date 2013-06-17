<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="leaf_ProjectHome.jsp"%>
<%!
        AuthRequest ar=null;
        String pageTitle="";
%>

<%
    ar = AuthRequest.getOrCreate(request, response, out);
    /* if the parameter is not found in the parameters list, then find it out in the attributes list */
    UserProfile uProf = ar.getUserProfile();
    ar.setPageAccessLevels(ngp);
%>
 <%displayCreatLeaf(ar,ngp);%>
 <div class="content tab01">

 <%
                    if (!ar.isLoggedIn())
                    {
                    %>
                        <div class="generalContent">
                            <fmt:message key="nugen.projecthome.private.logout"></fmt:message>
                        </div>
                    <%
                    }
                    else
                    {
                    %>
                        <div class="generalContent">
                            <fmt:message key="nugen.projecthome.privatelogin">
                                <fmt:param value='<%= ar.getBestUserId() %>'></fmt:param>
                            </fmt:message>
                            <br/>
                        <%
                        String pageKey = ngp.getKey();
                        long pageChangeTime = ngp.getLastModifyTime();
                        UserProfile up = ar.getUserProfile();
                        long subTime = 0;
                        if (uProf!=null)
                        {
                            subTime = uProf.watchTime(pageKey);
                        }
                        boolean found = subTime!=0;
                        String thisPage = ar.getResourceURL(ngp,"projectHome.htm");
                        ar.write("\n<input type=\"hidden\" id=\"pageChangeTime\" value=\"");
                        ar.write(String.valueOf(pageChangeTime));
                        ar.write("\"/>");
                        ar.write("\n<input type=\"hidden\" id=\"subTime\" value=\"");
                        ar.write(String.valueOf(subTime));
                        ar.write("\"/>");

                        %>
                            <br/><!--
                            <div id="loginArea">
                                <span class="black" id="01"  style="display: none;">
                                    <fmt:message key="nugen.projecthome.private.stopwatching.notchanged"/>
                                    <% SectionUtil.nicePrintTime(out, subTime, ar.nowTime); %>
                                    <fmt:message key="nugen.projecthome.private.stopwatching"/>
                                    <input type="hidden" name="p" value="<%ar.writeHtml(pageKey);%>">
                                    <input type="hidden" name="go" value="<%ar.writeHtml(thisPage);%>">
                                    <input type="button" name="action"  class="inputBtn"
                                        value="<fmt:message key='nugen.button.StopWatching'/>"
                                        onclick="ajaxChangeWatching('<%ar.writeHtml(pageKey);%>','Stop Watching','<%=ar.retPath%>');">
                                </span>

                                <span class="black" id="02"  style="display: none;">
                                    <fmt:message key="nugen.projecthome.private.stopwatching.changed"/>
                                    <%SectionUtil.nicePrintTime(out, subTime, ar.nowTime);%>
                                    <fmt:message key="nugen.projecthome.private.stopwatching"/>
                                    <input type="hidden" name="p" value="<%ar.writeHtml(pageKey);%>">
                                    <input type="hidden" name="go" value="<%ar.writeHtml(thisPage);%>">
                                    <input type="button" name="action"  class="inputBtn"
                                        value="<fmt:message key='nugen.button.ResetWatchingTime'/>"
                                        onclick="ajaxChangeWatching('<%ar.writeHtml(pageKey);%>','Reset Watch Time','<%=ar.retPath%>');">
                                    <input type="button" name="action"  class="inputBtn"
                                        value="<fmt:message key='nugen.button.StopWatching'/>"
                                        onclick="ajaxChangeWatching('<%ar.writeHtml(pageKey);%>','Stop Watching','<%=ar.retPath%>');">
                                </span>

                                <span class="black" id="03"  style="display: none;">
                                    <fmt:message key="nugen.projecthome.private.startwatching"></fmt:message>
                                    <input type="hidden" name="p" value="<%ar.writeHtml(pageKey);%>">
                                    <input type="hidden" name="go" value="<%ar.writeHtml(thisPage);%>">
                                    <input type="button" name="action"  class="inputBtn"
                                        value="<fmt:message key='nugen.button.StartWatching'/>"
                                        onclick="ajaxChangeWatching('<%ar.writeHtml(pageKey);%>','Start Watching','<%=ar.retPath%>');">
                                </span>
                            </div>

                            --><script type="text/javascript">
var time = null;
var pageChangeTime = null;
if(document.getElementById('subTime')!=null){
time = document.getElementById('subTime').value;
}
if(document.getElementById('pageChangeTime')!=null){
pageChangeTime = document.getElementById('pageChangeTime').value;
}
if (time>pageChangeTime){
    document.getElementById("01").style.display="";
}
else if(time>0){
    document.getElementById("02").style.display="";
}
else{
    if(document.getElementById("03")!=null)
        document.getElementById("03").style.display="";
}

</script>
                        </div>
                        <%
                        displayAllLeaflets(ar, ngp, SectionDef.PRIVATE_ACCESS);
                    }
                    out.flush();
                    %>

</div>
</div>


</div>
                  </div>
<div class="seperator">&nbsp;</div>


<div id="loginArea">
<span class="black"><%writeMembershipStatus(ngp, ar);%></span></div>
</div>
<%@ include file="logininfoblock.jsp"%>
