<%@page errorPage="/spring/jsp/error.jsp"
%><%@ include file="/spring/jsp/include.jsp"
%><%
    AuthRequest ud_ar = AuthRequest.getOrCreate(request, response, out);
    ud_ar.assertLoggedIn("You need to Login to Upload a file.");
    String pageId = ud_ar.reqParam("pageId");
    NGPage ngp = (NGPage)NGPageIndex.getContainerByKeyOrFail(pageId);
    if (ngp.isFrozen()) {
        throw new Exception("Program Logic Error: addDocument.jsp should never be invoked when the project is frozen.  Please check the logic of the controller.");
    }

%>
<html>
    <head>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <title>Add Document</title>
        <script type="text/javascript" language="javascript" src="<%=ud_ar.retPath%>bewebapp/bewebapp.nocache.js"></script>
        <link href="<%=ud_ar.baseURL%>css/body.css" rel="styleSheet" type="text/css" media="screen" />
    </head>
    <body class="yui-skin-sam">
        <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
        <div>
            <div class="pageHeading">Add Document</div>
            <div class="pageSubHeading">
                Use this form to add a document attachment to the Project "<%=ngp.getFullName() %>"
            </div>
            <br/>
            <table>
            <tr>
                <td colspan="3" class="linkWizardHeading">How do you want to attach the file?:</td>
            </tr>
            <tr style="height:50px;padding:15px">
            <td style="padding:15px"><a href="uploadDocument.htm"><button type="button" class="inputBtn" height>Upload a File</button></a></td>
            <td style="padding:15px"><p>Take a file from your local disk, and using your browser upload that document to the project.</p></td>
            </tr>
            <tr style="height:50px;padding:15px">
            <td style="padding:15px"><a href="linkToRepository.htm"><button type="button" class="inputBtn" height>Attach from Repository</button></a></td>
            <td style="padding:15px"><p>Cognoscenti can access a document repository, using WebDAV or other protocols,
                   to retrieve a document directly from there.   The advantage of this is that later, if the document is
                   changed, the updated document can be synchronized with that document repository.  Either specify the
                   address of that document, or browse the repository using your pre-configured connections.</p></td>
            </tr>
            <tr style="height:50px;padding:15px">
            <td style="padding:15px"><a href="linkURLToProject.htm"><button type="button" class="inputBtn" height>Link URL</button></a></td>
            <td style="padding:15px"><p>Link a web page to the project.   This will not download the web page as a attachment,
                   but instead will provide an easy way for other users to access the web page in their browser.</p></td>
            </tr>
            </table>
        </div>
    </body>
</html>
