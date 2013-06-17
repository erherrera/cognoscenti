<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/include.jsp"
%><%@ include file="/spring/jsp/functions.jsp"
%><%

/*

Required Parameters:

    1. searchByDate : This parameter is used to search log file, here its passed as hidden attribute in controller.
    2. goURL        : This is the url of current page which is used when form is successfully processed bt controller.

    3. searchResult : This request attribute provide the search result from the log file of specified date if any.
*/


    HashMap searchResult =(HashMap)request.getAttribute("searchResult");
    String searchByDate=ar.reqParam("searchByDate");
    String goURL=ar.reqParam("goURL");

%>
<script type="text/javascript">
<!--
Submitting the user comments
//-->

function postMyComment(){
document.forms["logUserComents"].submit();
}
</script>
<!-- Begin mainContent (Body area) -->
<div id="mainContent">
    <!-- Content Area Starts Here -->
 <div class="generalArea">
        <div class="pageSubHeading">
            <table width="100%">
                <tr>
                    <td rowspan="2" width="42px"><img src="<%=ar.retPath %>assets/iconError_BIG.png" width="32" height="32" /></td>
                    <td>
                        <div class="pageHeading">
                            Details of Error: <%ar.writeHtml(searchResult.get("errorNo").toString()); %>
                        </div>
                    </td>
                </tr>
              </table>
        </div>
        <div class="generalSettings">
             <form name="logUserComents" action="logUserComents.form" method="post">

              <input type="hidden" name="errorNo" id="errorNo" value="<%ar.writeHtml(searchResult.get("errorNo").toString()); %>"/>
              <input type="hidden" name="dateTime" id="dateTime" value="<%ar.writeHtml(searchResult.get("Date&Time").toString()); %>"/>
              <input type="hidden" name="searchByDate" id="searchByDate" value="<%ar.writeHtml(searchByDate); %>"/>
              <input type="hidden" name="goURL" id="goURL" value="<%ar.writeHtml(goURL); %>"/>

                <table width="100%" border="0px solid red">
                    <tr>
                      <td style="text-align:left">
                        <b>Error Message:</b>  <%ar.writeHtmlWithLines(searchResult.get("ErrorMessage").toString()); %>
                        <br /><br />
                        <b>Page:</b> <a href="<%ar.writeHtml(searchResult.get("URL").toString()); %>"><%ar.writeHtml(searchResult.get("URL").toString()); %></a>
                        <br /><br />
                        <b>Date & Time:</b> <%ar.writeHtml(searchResult.get("Date&Time").toString()); %>
                        <br /><br />
                        <b>User Detail: </b> <%ar.writeHtml(searchResult.get("userDetails").toString()); %>
                        <br /><br />
                        <b>Comments: </b>
                        <br />
                        <textarea rows="4" name="comments" id="comments" class="textAreaGeneral"><%ar.writeHtml(searchResult.get("userComments").toString()); %></textarea>
                        <br /><br />
                        <input type="submit" class="inputBtn" value="<fmt:message key="nugen.button.comments.update" />"
                                                                onclick="postMyComment()">
                    </td>
                  </tr>
                    <tr><td style="height:20px"></td></tr>
                     <tr>
                         <td class="errorDetailArea">
                            <span id="showDiv" style="display:inline" onclick="setVisibility('errorDetails')">
                                Show Error Details &nbsp;&nbsp;
                                 <img src="<%=ar.retPath %>assets/expandBlackIcon.gif" title="Expand" alt="" />
                             </span>
                            <span id="hideDiv" style="display:none" onclick="setVisibility('errorDetails')">
                                Hide Error Details &nbsp;&nbsp;
                                <img src="<%=ar.retPath %>assets/collapseBlackIcon.gif" title="Collapse" alt="" />
                             </span>
                         </td>
                     </tr>

                      <tr><td style="height:20px"></td></tr>
                      <tr>
                          <td style="text-align:left">
                            <div id="errorDetails" class="errorStyle" style="display:none;">
                            <pre style="overflow:auto;width:900px;"><%ar.writeHtml(searchResult.get("ErrorDescription").toString()); %></pre>
                            </div>
                          </td>
                      </tr>
                </table>
             </form>

        </div>
 </div>
 <!-- Content Area Ends Here -->
