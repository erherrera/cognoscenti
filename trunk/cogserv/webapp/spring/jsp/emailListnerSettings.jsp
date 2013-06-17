<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="administration.jsp"
%>
<div class="content tab01" style="display:block;" >
    <div class="section_body">
        <div style="height:10px;"></div>
        <div class="generalHeading">Email Listener Settings</div> 
        <%
             File  emailPropFile = ConfigFile.getFile("EmailNotification.properties");
                 Properties emailProperties = new Properties();
                 if (!emailPropFile.exists()) {
             throw new NGException("nugen.exception.incorrect.sys.config", new Object[]{emailPropFile.getAbsolutePath()});
                 }
             
                 FileInputStream fis = new FileInputStream(emailPropFile);
                 emailProperties.load(fis);
                 if(SuperAdminLogFile.getEmailListenerPropertiesFlag()){
         %>
        <img src="<%=ar.retPath%>assets/images/greencircle.jpg" border="green" width="10px" height="10px" /> 
        &nbsp;&nbsp; Settings for Email Listener are fine. 
        <a href="javascript:showHideDiv('currentSettings');">See current Settings</a><br/><br/>
        <div id="currentSettings" style="display: none;"> 
            <table border="0px solid gray" class="gridTable" width="70%">
                <tr>
                    <td>POP3 Host</td>
                    <td><%=emailProperties.getProperty("mail.pop3.host")%></td>
                </tr>
                <tr>
                    <td>POP3 Port</td>
                    <td><%=emailProperties.getProperty("mail.pop3.port")%></td>
                </tr>
                <tr>
                    <td>User Name</td>
                    <td><%=emailProperties.getProperty("mail.pop3.user")%></td>
                </tr>
                <tr>
                    <td>Password</td>
                    <td><%=emailProperties.getProperty("mail.pop3.password")%></td>
                </tr>
            </table>
        </div>
        <%
            }else{
        %>
        <table width="100%">
            <tr>
                <td width="5%" valign="top">
                    <img id="imgId" src="<%=ar.retPath%>assets/images/redcircle.jpg" border="red" width="10px" height="10px" />
                </td>
                <td width="95%" valign="top">
                    Email Listener is not working due to any of the following reasons:<br/>
                    &nbsp;&nbsp;&nbsp;&nbsp;1.&nbsp;&nbsp;Mail Server is down.<br/>
                    &nbsp;&nbsp;&nbsp;&nbsp;2.&nbsp;&nbsp;Settings for Email Listener are either incorrect or empty.<br/>
                    <br/>
                    <a href="javascript:showHideDiv('errorDiv');" ><B>See Detail.</B></a><br/><br/>
                    <div id="errorDiv" style="display: none;width:600px;border: #ccc 1px dashed;margin:0px 0px 10px 0px;height:100px;overflow-x:auto;overflow-y:scroll" title="Reason" align="left">
                       
                       <div class="generalHeading">Current Email Listener Settings</div> <br/>
                       <table border="0px solid gray" class="gridTable" width="70%">
                           <tr>
                               <td>POP3 Host</td>
                               <td><%=emailProperties.getProperty("mail.pop3.host")%></td>
                           </tr>
                           <tr>
                               <td>POP3 Port</td>
                               <td><%=emailProperties.getProperty("mail.pop3.port")%></td>
                           </tr>
                           <tr>
                               <td>User Name</td>
                               <td><%=emailProperties.getProperty("mail.pop3.user")%></td>
                           </tr>
                           <tr>
                               <td>Password</td>
                               <td><%=emailProperties.getProperty("mail.pop3.password")%></td>
                           </tr>
                       </table>
                       <br/>
                       <div class="generalHeading">Exception</div><br/>
                       <pre>
                       <%=SuperAdminLogFile.getEmailListenerProblem()%>
                       </pre>
                    </div>
                </td>
            </tr>
            <!--<tr>
                <td>&nbsp;</td>
                <td>You can also <a href="javascript:changeListenerSettings();"><B>click here</B></a> to change email listener settings.</td>
            </tr>-->
        </table>
        <%
        }
        %>
        <table>
            <tr>
                <td>&nbsp;</td>
                <td>You can also <a href="javascript:changeListenerSettings();"><B>click here</B></a> to change email listener settings.</td>
            </tr>
        </table>
    </div>
    <div id="listenerSettingsDiv" style="display: none">
        <form id="listenerSettingsForm" action="<%=ar.baseURL%>v/<%=uProf.getKey() %>/changeListenerSettings.form" method="post">
            <table>
                <tr>
                    <td class="gridTableColummHeader" width="10%">POP3 Host:&nbsp;&nbsp;</td>
                    <td>
                        <input id="pop3Host" name="pop3Host" value="<%=emailProperties.getProperty("mail.pop3.host") %>"> 
                    </td>
                </tr>
                <tr>
                    <td class="gridTableColummHeader">POP3 Port:&nbsp;&nbsp;</td>
                    <td>
                        <input id="pop3Port" name="pop3Port" value="<%=emailProperties.getProperty("mail.pop3.port") %>"> 
                    </td>
                </tr>
                <tr>
                    <td class="gridTableColummHeader">User name:&nbsp;&nbsp;</td>
                    <td>
                        <input id="pop3User" name="pop3User" value="<%=emailProperties.getProperty("mail.pop3.user") %>"> 
                    </td>
                </tr>
                <tr>
                    <td class="gridTableColummHeader">Password:&nbsp;&nbsp;</td>
                    <td>
                        <input id="pop3Password" name="pop3Password" value="<%=emailProperties.getProperty("mail.pop3.password") %>"> 
                    </td>
                </tr>
                <tr><td style="height:15px"></td></tr>
                <tr>
                    <td colspan="2" align="center">
                        <input type="submit" class="inputBtn"
                                value="<fmt:message key="nugen.button.general.update" />">&nbsp;&nbsp;
                        <input type="button" class="inputBtn"
                                value="<fmt:message key="nugen.button.general.cancel" />"
                                onclick="return cancelPanel();">
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>
<script type="text/javascript">
    var xTimer = window.setInterval("changeImg()",500);
    var divHide = false;
    
    function changeImg(){
      document.getElementById("imgId").src = (document.getElementById("imgId").src.indexOf("redcircle.jpg") == -1)?"<%=ar.retPath%>assets/images/redcircle.jpg":""; 
    }
</script>