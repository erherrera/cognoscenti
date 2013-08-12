package org.socialbiz.cog.spring;

import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.socialbiz.cog.AccessControl;
import org.socialbiz.cog.AddressListEntry;
import org.socialbiz.cog.AttachmentRecord;
import org.socialbiz.cog.AuthDummy;
import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.DOMFace;
import org.socialbiz.cog.DataFeedServlet;
import org.socialbiz.cog.EmailSender;
import org.socialbiz.cog.HistoryRecord;
import org.socialbiz.cog.HtmlToWikiConverter;
import org.socialbiz.cog.LeafletResponseRecord;
import org.socialbiz.cog.MicroProfileMgr;
import org.socialbiz.cog.NGBook;
import org.socialbiz.cog.NGContainer;
import org.socialbiz.cog.NGPage;
import org.socialbiz.cog.NGPageIndex;
import org.socialbiz.cog.NGRole;
import org.socialbiz.cog.NoteRecord;
import org.socialbiz.cog.OptOutAddr;
import org.socialbiz.cog.OptOutDirectAddress;
import org.socialbiz.cog.SearchResultRecord;
import org.socialbiz.cog.SectionAttachments;
import org.socialbiz.cog.SectionDef;
import org.socialbiz.cog.SectionUtil;
import org.socialbiz.cog.UserManager;
import org.socialbiz.cog.UserProfile;
import org.socialbiz.cog.UtilityMethods;
import org.socialbiz.cog.WikiConverter;
import org.socialbiz.cog.WikiToPDF;
import org.socialbiz.cog.dms.FolderAccessHelper;
import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.exception.ProgramLogicError;
import org.socialbiz.cog.util.PDFUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class MainTabsViewControler extends BaseController {

    private ApplicationContext context;
    @Autowired
    public void setContext(ApplicationContext context) {
        this.context = context;
    }


    private ModelAndView needAccessView(HttpServletRequest request, String why) {
        request.setAttribute("property_msg_key", why);
        return new ModelAndView("Warning");
    }


    @RequestMapping(value = "/{accountId}/{pageId}/projectHome.htm", method = RequestMethod.GET)
    public ModelAndView showProjectHomeTab(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        AuthRequest ar = AuthRequest.getOrCreate(request, response);
        return redirectBrowser(ar, "public.htm");
    }

    @RequestMapping(value = "/{accountId}/{pageId}/public.htm", method = RequestMethod.GET)
    public ModelAndView showPublicTab(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ModelAndView modelAndView = null;

        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.setPageAccessLevels(nGPage);

            modelAndView=new ModelAndView("public");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Notes");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.public");
            request.setAttribute("visibility_value", "1");
            request.setAttribute("title", nGPage.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.public.page", new Object[]{pageId,accountId} , ex);
        }
        return modelAndView;

    }

    @RequestMapping(value = "/{accountId}/{pageId}/member.htm", method = RequestMethod.GET)
    public ModelAndView showMemberTab(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return needAccessView(request, "nugen.project.member.login.msg");
            }
            NGPageIndex.assertBook(accountId);
            modelAndView=new ModelAndView("member");
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);

            ar.setPageAccessLevels(nGPage);
            if(!ar.isMember()){
                return needAccessView(request, "nugen.member.section.memberlogin");
            }

            modelAndView=new ModelAndView("member");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.member");
            request.setAttribute("visibility_value", "2");

            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Notes");
            request.setAttribute("title", nGPage.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.member.page", new Object[]{pageId,accountId} , ex);
        }
        return modelAndView;

    }

    @RequestMapping(value = "/{accountId}/{pageId}/private.htm", method = RequestMethod.GET)
    public ModelAndView showPrivateTab(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;

        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            modelAndView=new ModelAndView("private");

            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Notes");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.private");
            request.setAttribute("visibility_value", "4");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.private.page", new Object[]{pageId,accountId} , ex);
        }
        return modelAndView;

    }

    @RequestMapping(value = "/{accountId}/{pageId}/deletedNotes.htm", method = RequestMethod.GET)
    public ModelAndView showDeletedTab(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;

        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Notes");

            NGPageIndex.assertBook(accountId);
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.setPageAccessLevels(nGPage);

            if(!ar.isLoggedIn()){
                return needAccessView(request, "nugen.project.deleted.notes.login.msg");
            }
            if(!ar.isMember()){
                return needAccessView(request, "nugen.projecthome.deletednotes.memberlogin");
            }

            modelAndView=new ModelAndView("leaf_deleted");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.deletedNotes");
            request.setAttribute("visibility_value", "4");
            request.setAttribute("title", nGPage.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.delete.notes.page", new Object[]{pageId,accountId} , ex);
        }
        return modelAndView;

    }

    @RequestMapping(value = "/{accountId}/{pageId}/admin.htm", method = RequestMethod.GET)
    public ModelAndView showAdminTab(@PathVariable String accountId,
            @PathVariable String pageId, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Settings");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.Admin");

            NGPageIndex.assertBook(accountId);
            NGBook nGBook = null;
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.setPageAccessLevels(nGPage);

            if(!ar.isLoggedIn()){
                return needAccessView(request, "nugen.project.login.msg");
            }

            modelAndView = new ModelAndView("leaf_admin");
            modelAndView.addObject("page", nGPage);
            modelAndView.addObject("tanent", nGBook);

            request.setAttribute("visibility_value", "3");
            request.setAttribute("title", nGPage.getFullName());
        }catch (Exception ex) {
            throw new NGException("nugen.operation.fail.project.admin.page", new Object[]{pageId,accountId} , ex);
        }

        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/{pageId}/projectSettings.htm", method = RequestMethod.GET)
    public ModelAndView showProjectSettingsTab(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        AuthRequest ar = AuthRequest.getOrCreate(request, response);
        return redirectBrowser(ar, "personal.htm");
    }

    @RequestMapping(value = "/{accountId}/{pageId}/attachment.htm", method = RequestMethod.GET)
    public ModelAndView showAttachmentTab(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            request.setAttribute("subTabId", "nugen.projecthome.subtab.documents");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Documents");

            NGPageIndex.assertBook(accountId);
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.setPageAccessLevels(nGPage);

            modelAndView=new ModelAndView("attachment");
            request.setAttribute("title", nGPage.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.attachment.page", new Object[]{pageId,accountId} , ex);
        }

        return modelAndView;

    }

    @RequestMapping(value = "/{accountId}/{pageId}/deletedAttachments.htm", method = RequestMethod.GET)
    public ModelAndView showDeletedAttachments(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Documents");

            NGPageIndex.assertBook(accountId);
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.setPageAccessLevels(nGPage);

            if(!ar.isLoggedIn()){
                return needAccessView(request, "nugen.project.deleted.doc.login.msg");
            }
            if(!ar.isMember()){
                return needAccessView(request, "nugen.attachment.deletedattachment.memberlogin");
            }

            modelAndView=new ModelAndView("leaf_deleted_attach");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.deleted");
            request.setAttribute("title", nGPage.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.delete.attachment.page", new Object[]{pageId,accountId} , ex);
        }

        return modelAndView;

    }
    @RequestMapping(value = "/{accountId}/{pageId}/process.htm", method = RequestMethod.GET)
    public ModelAndView showProcessTab(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        AuthRequest ar = AuthRequest.getOrCreate(request, response);
        return redirectBrowser(ar, "projectActiveTasks.htm");
    }



    @RequestMapping(value = "/{accountId}/{pageId}/ganttchart.htm", method = RequestMethod.GET)
    public ModelAndView showGanttChart(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Tasks");

            NGPageIndex.assertBook(accountId);
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.setPageAccessLevels(nGPage);

            if(!ar.isLoggedIn()){
                return needAccessView(request, "nugen.project.gantt.chart.login.msg");
            }
            if(!ar.isMember()){
                return needAccessView(request, "nugen.projecthome.task.memberlogin");
            }

            modelAndView=new ModelAndView("ganttchart");
            String active = ar.defParam("active", "1");
            request.setAttribute("title",  nGPage.getFullName());
            request.setAttribute("active", active);
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.process.page", new Object[]{pageId,accountId} , ex);
        }
        return modelAndView;
    }


    @RequestMapping(value = "/{accountId}/{pageId}/permission.htm", method = RequestMethod.GET)
    public ModelAndView showPermissionTab(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Settings");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.Permissions");

            NGPageIndex.assertBook(accountId);
            NGBook nGBook = null;

            // Set the Index page name
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            List<NGRole> roles = nGPage.getAllRoles();
            nGBook = nGPage.getAccount();
            ar.setPageAccessLevels(nGPage);

            if(!ar.isLoggedIn()){
                return needAccessView(request, "nugen.project.permission.login.msg");
            }
            if(!ar.isMember()){
                return needAccessView(request, "nugen.projecthome.permission.memberlogin");
            }

            modelAndView=new ModelAndView("permission");

            //TODO: eliminate these unnecessary parameters
            request.setAttribute("roles", roles);
            modelAndView.addObject("page", nGPage);
            modelAndView.addObject("tanent", nGBook);

            request.setAttribute("title", nGPage.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.permission.page", new Object[]{pageId,accountId} , ex);
        }

        return modelAndView;

    }

    @RequestMapping(value = "/{accountId}/{pageId}/history.htm", method = RequestMethod.GET)
    public ModelAndView showHistoryTab(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Stream");

            NGPageIndex.assertBook(accountId);
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.setPageAccessLevels(nGPage);
            request.setAttribute("title", nGPage.getFullName());

            if(!ar.isLoggedIn()){
                return needAccessView(request, "nugen.project.history.login.msg");
            }
            if(!ar.isMember()){
                return needAccessView(request, "nugen.projecthome.projectbulletin.memberlogin");
            }

            modelAndView=new ModelAndView("history");
            request.setAttribute("messages", context);
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.history.page", new Object[]{pageId,accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value="/{accountId}/{pageId}/a/{docId}.{ext}", method = RequestMethod.GET)
     public void loadDocument(
           @PathVariable String accountId,
           @PathVariable String pageId,
           @PathVariable String docId,
           @PathVariable String ext,
           HttpServletRequest request,
           HttpServletResponse response) throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPage ngp = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.setPageAccessLevels(ngp);

            String attachmentName = docId+"."+ext;
            AttachmentRecord att = ngp.findAttachmentByNameOrFail(attachmentName);

            boolean canAccessDoc = AccessControl.canAccessDoc(ar, ngp, att);

            if(!canAccessDoc){
                String msgKey = "message.loginalert.access.attachment";
                if(att.getVisibility() != SectionDef.PUBLIC_ACCESS){
                    msgKey = "message.loginalert.access.non.public.attachment";
                }
                sendRedirectToLogin(ar, msgKey,null);
                return;
            }
            NGPageIndex.assertBook(accountId);

            String version = ar.defParam("version", null);
            if(version != null && !"".equals(version)){
               SectionAttachments.serveUpFileNewUI(ar, ngp, attachmentName,Integer.parseInt(version));
            }else{
               SectionAttachments.serveUpFile(ar, ngp, attachmentName);
            }
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.download.document", new Object[]{pageId,accountId} , ex);
        }
    }

    @RequestMapping(value="/{accountId}/{pageId}/f/{docId}.{ext}", method = RequestMethod.GET)
    public void loadRemoteDocument(
            @PathVariable String accountId,
            @PathVariable String pageId,
            @PathVariable String docId,
            @PathVariable String ext,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        try{
            AuthRequest ar  = AuthRequest.getOrCreate(request, response);

            NGPageIndex.assertBook(accountId);
            NGPageIndex.getProjectByKeyOrFail(pageId);

            String symbol = ar.reqParam("fid");

            FolderAccessHelper fah = new FolderAccessHelper(ar);
            fah.serveUpRemoteFile(symbol);
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.download.document", new Object[]{pageId,accountId} , ex);
        }
    }

    /**
    * note that the docid in the path is not needed, but it will be different for
    * every file for convenience of auto-generating a file name to save to.
    *
    * following the name is a bunch of query paramters listing the notes to include in the output.
    */
    @RequestMapping(value="/{accountId}/{pageId}/pdf/{docId}.pdf", method = RequestMethod.GET)
    public void generatePDFDocument(
            @PathVariable String accountId,
            @PathVariable String pageId,
            @PathVariable String docId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            NGPage ngp = NGPageIndex.getProjectByKeyOrFail(pageId);

            //this constructs and outputs the PDF file to the output stream
            WikiToPDF.handlePDFRequest(ar, ngp);

        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.download.document", new Object[]{pageId,accountId} , ex);
        }
    }

    @RequestMapping(value="/{accountId}/{pageId}/pdf1/{docId}.{ext}", method = RequestMethod.POST)
    public void generatePDFDocument(
            @PathVariable String accountId,
            @PathVariable String pageId,
            @PathVariable String docId,
            @PathVariable String ext,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            NGPageIndex.getProjectByKeyOrFail(pageId);

            PDFUtil pdfUtil = new PDFUtil();
            pdfUtil.serveUpFile(ar, pageId);
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.download.document", new Object[]{pageId,accountId} , ex);
        }
    }


     @RequestMapping(value = "/index.htm", method = RequestMethod.GET)
     public ModelAndView showLandingPage(HttpServletRequest request, HttpServletResponse response)
                throws Exception {
         ModelAndView modelAndView = null;
         try{
             AuthRequest ar = AuthRequest.getOrCreate(request, response);
             NGPageIndex.assertInitialized();
             //if the user is logged in, redirect to their own home page instead
             if (ar.isLoggedIn())
             {
                 response.sendRedirect(ar.retPath+"v/"+ar.getUserProfile().getKey()+"/watchedProjects.htm");
                 return null;
             }

             modelAndView=new ModelAndView("landingPage");
             request.setAttribute("realRequestURL", ar.getRequestURL());
             List<NGBook> list=new ArrayList<NGBook>();
             for (NGBook ngb : NGBook.getAllAccounts()) {
                 list.add(ngb);
             }

             request.setAttribute("headerType", "index");
             modelAndView.addObject("bookList",list);
         }catch(Exception ex){
             throw new NGException("nugen.operation.fail.project.welcome.page", null , ex);
         }
         return modelAndView;
     }


     @RequestMapping(value = "/texteditor.htm", method = RequestMethod.GET)
     public ModelAndView openTextEditor(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

         ModelAndView modelAndView = null;

         try{
             modelAndView = new ModelAndView("texteditor");
             AuthRequest ar = AuthRequest.getOrCreate(request, response);
             if(!ar.isLoggedIn()){
                 return redirectToLoginView(ar, "message.loginalert.create.note",null);
             }

             String p = ar.reqParam("pid");
             NGContainer ngp = NGPageIndex.getContainerByKeyOrFail(p);
             ar.setPageAccessLevels(ngp);
             ar.assertMember("Need Member Access to Create a Note.");
             modelAndView.addObject("pageTitle",ngp.getFullName());
             request.setAttribute("title",ngp.getFullName());
         }catch(Exception ex){
             throw new NGException("nugen.operation.fail.project.create.note.page", null , ex);
         }
         return modelAndView;
     }



     @RequestMapping(value = "/sendNoteByEmail.htm", method = RequestMethod.GET)
     public ModelAndView sendNoteByEmail(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

         ModelAndView modelAndView = null;
         try{
             AuthRequest ar = AuthRequest.getOrCreate(request, response);
             if(!ar.isLoggedIn()){
                 return redirectToLoginView(ar, "message.loginalert.send.note.by.email",null);
             }

             modelAndView=new ModelAndView("SendNoteByEmail");
             request.setAttribute(ar.defParam("selectedAttachemnt",""), "true");
             ar.preserveRealRequestURL();
         }catch(Exception ex){
             throw new NGException("nugen.operation.fail.project.sent.note.by.email.page", null , ex);
         }
         return modelAndView;
     }

    //allow a user to change their email subscriptions, including opt out
    //even when not logged in.
    @RequestMapping(value = "/EmailAdjustment.htm", method = RequestMethod.GET)
    public ModelAndView emailAdjustment(HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            ar.preserveRealRequestURL();
            return new ModelAndView("EmailAdjustment");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.sent.note.by.email.page", null , ex);
        }
    }

    //allow a user to change their email subscriptions, including opt out
    //even when not logged in.
    @RequestMapping(value = "/EmailAdjustmentAction.form", method = RequestMethod.POST)
    public void emailAdjustmentActionForm(HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            ar.preserveRealRequestURL();

            String p = ar.reqParam("p");
            String email = ar.reqParam("email");
            String mn = ar.reqParam("mn");
            String go = ar.defParam("go", ar.baseURL);

            NGContainer ngp = NGPageIndex.getContainerByKeyOrFail(p);
            String expectedMn = ngp.emailDependentMagicNumber(email);
            if (!expectedMn.equals(mn)) {
                throw new Exception("Something is wrong, improper request for email address "+email);
            }

            String cmd = ar.reqParam("cmd");
            if ("Remove Me".equals(cmd)) {
                String role = ar.reqParam("role");
                NGRole specRole = ngp.getRoleOrFail(role);
                specRole.removePlayer(new AddressListEntry(email));
            }
            else {
                throw new Exception("emailAdjustmentActionForm does not understand the cmd "+cmd);
            }

            response.sendRedirect(go);
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.sent.note.by.email.page", null , ex);
        }
    }


     @RequestMapping(value = "/{accountId}/{pageId}/leaflet{lid}.htm", method = RequestMethod.GET)
     public ModelAndView displayOneLeaflet(@PathVariable String lid, @PathVariable String pageId,
            @PathVariable String accountId, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.setPageAccessLevels(nGPage);
            NoteRecord note = nGPage.getNoteOrFail(lid);

            modelAndView=new ModelAndView("NoteZoomView");

            request.setAttribute("lid", lid);
            request.setAttribute("zoomMode", true);
            request.setAttribute("noteName", note.getSubject() );
            request.setAttribute("title", note.getSubject()+" - "+nGPage.getFullName());
            request.setAttribute("tabId", "Project Notes");
            request.setAttribute("realRequestURL", ar.getRequestURL());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.zoom.note.page", new Object[]{lid,pageId,accountId} , ex);
        }
        return modelAndView;
    }

     @RequestMapping(value = "/previewNoteForEmail.htm", method = RequestMethod.GET)
     public ModelAndView previewNoteForEmail(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

         ModelAndView modelAndView = null;
         try{
             AuthRequest ar = AuthRequest.getOrCreate(request, response);
             if(!ar.isLoggedIn()){
                 return redirectToLoginView(ar, "message.loginalert.preview.note",null);
             }
             modelAndView = new ModelAndView("PreviewNoteForEmail");
             String editedSubject = ar.defParam("subject", "");
             request.setAttribute("subject", editedSubject);
         }catch(Exception ex){
             throw new NGException("nugen.operation.fail.project.preview.note.email.page", null , ex);
         }
         return modelAndView;
     }

     @RequestMapping(value = "/createLeafletSubmit.ajax", method = RequestMethod.POST)
     public void createLeaflet(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String responseText = null;
        AuthRequest ar = AuthRequest.getOrCreate(request, response);
        try {
            ar.assertLoggedIn("Must be logged in to create a note.");
            String p = ar.reqParam("p");
            NGContainer ngp = NGPageIndex.getContainerByKeyOrFail( p );
            ar.setPageAccessLevels(ngp);

            JSONObject paramMap = new JSONObject();
            String noteId = "";
            String action = ar.reqParam("action");
            if (!"Save".equals(action) && !"Undelete".equals(action) && !"SaveAsDraft".equals(action)) {
                String oid = ar.reqParam("oid");
                NoteRecord note = ngp.getNoteOrFail(oid);

                if(note.isDeleted()){
                    paramMap.put(Constant.MSG_TYPE, Constant.FAILURE);
                    paramMap.put(Constant.MESSAGE, "Note has already been deleted.");
                    paramMap.put(Constant.COMMENTS , "");
                }else{
                    if("publish".equals(action)){
                        note.setSaveAsDraft("no");
                        paramMap.put("visibility", String.valueOf(note.getVisibility()));
                        paramMap.put("subject", String.valueOf(note.getSubject()));
                    }else{
                        noteId = handleCreateLeaflet(ar, ngp);
                        if("UpdateAndPublish".equals(action)){
                            note.setSaveAsDraft("no");
                        }
                        note = ngp.getNoteOrFail(oid);
                    }
                    ngp.saveContent( ar, responseText);
                    paramMap.put(Constant.MSG_TYPE, Constant.SUCCESS);
                    paramMap.put("noteId", noteId);
                }
            }else{
                noteId = handleCreateLeaflet(ar, ngp);
                ngp.saveContent( ar, responseText);
                paramMap.put(Constant.MSG_TYPE, Constant.SUCCESS);
                paramMap.put("noteId", noteId);
            }

            responseText = paramMap.toString();
        }
        catch (Exception ex) {
            responseText = NGWebUtils.getExceptionMessageForAjaxRequest(ex, ar.getLocale());
            ar.logException("Caught by createLeafletSubmit.ajax", ex);
        }
        NGWebUtils.sendResponse(ar, responseText);
    }

    private String handleCreateLeaflet(AuthRequest ar, NGContainer ngp) throws Exception {

        ar.assertLoggedIn("Must be logged in to create a new note.");

        String action = ar.reqParam("action");
        int visibility = DOMFace.safeConvertInt(ar.reqParam("visibility"));
        String subject = ar.defParam("subj", "");
        if ("Save".equals(action) || "SaveAsDraft".equals(action)) {

            HtmlToWikiConverter htmlToWikiConverter = new HtmlToWikiConverter();
            String val = ar.defParam("val",  "");
            String wikiText = htmlToWikiConverter.htmlToWiki(ar.baseURL, val);

            NoteRecord note = ngp.createNote();
            note.setSubject( subject );
            note.setVisibility(visibility);
            note.setEditable(DOMFace.safeConvertInt(ar.reqParam("editable")));
            note.setData(wikiText);
            note.setEffectiveDate(ar.nowTime);
            note.setLastEdited(ar.nowTime);
            note.setLastEditedBy(ar.getBestUserId());
            if("SaveAsDraft".equals(action)){
                note.setSaveAsDraft("yes");
            }
            HistoryRecord.createHistoryRecord(ngp, note.getId(),
                    HistoryRecord.CONTEXT_TYPE_LEAFLET,0, HistoryRecord.EVENT_TYPE_CREATED,
                    ar, "");

            return note.getId();
        }

         //everything after this point requires an existing comment
        String oid =ar.reqParam("oid");
        if ("Update".equals(action) || "UpdateAndPublish".equals(action)) {
            NoteRecord note = ngp.getNoteOrFail(oid);
            assertEditNote(ngp, note, ar);

            HtmlToWikiConverter htmlToWikiConverter = new HtmlToWikiConverter();
            String wikiText = htmlToWikiConverter.htmlToWiki(ar.baseURL, ar.defParam("val",  ""));

            note.setVisibility(DOMFace.safeConvertInt(ar.reqParam("visibility")));
            note.setEditable(DOMFace.safeConvertInt(ar.reqParam("editable")));
            note.setEffectiveDate(SectionUtil.niceParseDate(ar.defParam("effDate", "")));
            note.setSubject(subject);
            note.setData(wikiText);

            String pin = ar.defParam("pin", null);
            if (pin!=null) {
                note.setPinOrder(DOMFace.safeConvertInt(pin));
            }
            String choices = ar.defParam("choices", null);
            if (choices!=null) {
                note.setChoices(choices);
            }

            note.setLastEdited(ar.nowTime);
            note.setLastEditedBy(ar.getBestUserId());
            HistoryRecord.createHistoryRecord(ngp, note.getId(),
                    HistoryRecord.CONTEXT_TYPE_LEAFLET,0, HistoryRecord.EVENT_TYPE_MODIFIED,
                    ar, "");

            return note.getId();
        }
        else if("Change Visibility".equals( action )){
            NoteRecord note = ngp.getNoteOrFail( oid );
            assertEditNote(ngp,note,ar);
            note.setVisibility(visibility);
            note.setEffectiveDate(SectionUtil.niceParseDate(ar.defParam("effDate", "")));
            HistoryRecord.createHistoryRecord(ngp, note.getId(),
                    HistoryRecord.CONTEXT_TYPE_LEAFLET,0, HistoryRecord.EVENT_LEVEL_CHANGE,
                    ar, "");
            return oid;
        }
        else if ("Remove".equals(action))
        {
            NoteRecord note = ngp.getNoteOrFail(oid);
            assertEditNote(ngp,note,ar);
            ngp.deleteNote(oid,ar);
            HistoryRecord.createHistoryRecord(ngp, note.getId(),
                    HistoryRecord.CONTEXT_TYPE_LEAFLET,0, HistoryRecord.EVENT_TYPE_DELETED,
                    ar, "");
            return oid;
        }
        else if ("Undelete".equals(action))
        {
            ngp.unDeleteNote( oid,ar);
            return oid;
        }

        throw new NGException("nugen.exceptionhandling.system.not.understand.action",new Object[]{action});
    }


    //Does this work on Page as well?
    public NoteRecord updateNoteBook(NGBook ngb, String id, String subject, AuthRequest ar)
            throws Exception {

        ar.assertLoggedIn("Must be logged in to update a note.");
        UserProfile up = ar.getUserProfile();

        String val = ar.defParam("val",  "");
        String choices = ar.defParam("choices", null);

        HtmlToWikiConverter htmlToWikiConverter = new HtmlToWikiConverter();
        String wikiText = htmlToWikiConverter.htmlToWiki(ar.baseURL,val);

        int visibility = DOMFace.safeConvertInt(ar.reqParam("visibility"));
        int editable   = DOMFace.safeConvertInt(ar.reqParam("editable"));

        NoteRecord note = ngb.getNoteOrFail( id );
        assertEditNote(ngb, note, ar);

        note.setLastEdited(ar.nowTime);
        note.setLastEditedBy(up.getUniversalId());
        note.setSubject(subject);
        note.setData(wikiText);
        note.setVisibility(visibility);
        note.setEditable(editable);
        String effDate = ar.defParam("effDate", null);
        if (effDate != null)
        {
            note.setEffectiveDate(SectionUtil.niceParseDate(effDate));
        }
        String pin = ar.defParam("pin", null);
        if (pin!=null)
        {
            note.setPinOrder(DOMFace.safeConvertInt(pin));
        }
        note.setChoices(choices);
        HistoryRecord.createHistoryRecord(ngb, note.getId(),
                HistoryRecord.CONTEXT_TYPE_LEAFLET,0, HistoryRecord.EVENT_TYPE_MODIFIED,
                ar, "");
        ngb.saveFile(ar, "updating a note");

        return note;
    }

    /**
     * Throws an exception if the currently logged in user does not have the right to
     * edit the passed in note.
     */
    private void assertEditNote(NGContainer ngc, NoteRecord note, AuthRequest ar)
            throws Exception {
        UserProfile up = ar.getUserProfile();

        //admins of the container can do anything
        if (ngc.primaryPermission(up)) {
            return;
        }
        if (note.getEditable() == NoteRecord.EDIT_OWNER) {
            if (!up.hasAnyId(note.getOwner())) {
                throw new Exception( "Unable to edit this note because it is marked "
                        + "to only be edited only by the owner: " + note.getOwner());
            }
        }
        else if (note.getEditable() == NoteRecord.EDIT_MEMBER) {
            if (!ngc.primaryOrSecondaryPermission(up)) {
                throw new Exception("User '" + up.getName() + "' can not edit note '"
                        + note.getId() + "'");
            }
        }
    }

    @RequestMapping(value = "/{accountId}/{pageId}/leafletResponse.htm", method = RequestMethod.POST)
    public ModelAndView handleLeafletResponse(@PathVariable String accountId,
            @PathVariable String pageId, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try {
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            NGPage ngp = NGPageIndex.getProjectByKeyOrFail(pageId);

            String go = ar.reqParam("go");
            String action = ar.reqParam("action");

            String lid = ar.reqParam("lid");
            String data = ar.defParam("data", null);
            String choice = ar.defParam("choice", null);

            ar.setPageAccessLevels(ngp);
            NoteRecord note = ngp.getNoteOrFail(lid);
            LeafletResponseRecord llr;

            String uid = ar.reqParam("uid");
            UserProfile designatedUser = UserManager.findUserByAnyId(uid);
            if (designatedUser == null) {
                // As Micro-profile concept has been introduced, so
                // Micro-profile will be created
                // instead of creating a new user profile.
                MicroProfileMgr.setDisplayName(uid, uid);
                MicroProfileMgr.save();
                //finds or creates a response for a user ID that has no profile.
                llr = note.accessResponse(uid);
            }
            else {
                //finds the response for a user with a profile
                llr = note.getOrCreateUserResponse(designatedUser);
            }

            if (action.startsWith("Update")) {
                //Note: we do not need to have "note edit" permission here
                //because we are only changing a response record.  We only need
                //note 'access' permissions which might come from magic number
                if (AccessControl.canAccessNote(ar, ngp, note)) {
                    llr.setData(data);
                    llr.setChoice(choice);
                    llr.setLastEdited(ar.nowTime);
                    ngp.save(uid, ar.nowTime, "Updated response to note");
                }
            }
            modelAndView = new ModelAndView(new RedirectView(go));
        }
        catch (Exception ex) {
            throw new NGException("nugen.operation.fail.project.note.response", new Object[] {
                    pageId, accountId }, ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/{pageId}/personal.htm", method = RequestMethod.GET)
    public ModelAndView showPersonalTab(@PathVariable String accountId,@PathVariable String pageId,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.setPageAccessLevels(nGPage);

            NGBook nGBook = nGPage.getAccount();

            ModelAndView modelAndView = null;
            if(!ar.isLoggedIn()){
                return needAccessView(request, "nugen.project.personal.login.msg");
            }

            modelAndView=new ModelAndView("personal");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.personal");
            request.setAttribute("visibility_value", "4");

            modelAndView.addObject("page", nGPage);
            modelAndView.addObject("tanent", nGBook);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Settings");
            request.setAttribute("title", nGPage.getFullName());
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.personal.page", new Object[]{pageId,accountId} , ex);
        }
     }

      @RequestMapping(value = "/{accountId}/{pageId}/project.htm", method = RequestMethod.GET)
      public ModelAndView showGwtProject(@PathVariable String accountId,@PathVariable String pageId,
              HttpServletRequest request, HttpServletResponse response)
              throws Exception {
        ModelAndView modelAndView = null;
        AuthRequest ar = null;
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            modelAndView=new ModelAndView("project");

            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Notes");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.public");
            request.setAttribute("visibility_value", "1");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.gwt.project.page", new Object[]{pageId,accountId} , ex);
        }
        return modelAndView;
     }

    @RequestMapping(value = "/{userKey}/Search.htm", method = RequestMethod.GET)
    public ModelAndView showSearch(@PathVariable String userKey,
                                   HttpServletRequest request, HttpServletResponse response)
                                    throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.access.search.page",null);
            }

            String headerType = ar.defParam("h",null);
            String tabId = ar.defParam("t","Home");
            String bookKey = ar.defParam("bookId",null);
            String pageKey = ar.defParam("pageId",null);

            if(pageKey == null && headerType == null ){
                headerType = "user";
            }else{
                request.setAttribute("pageId",pageKey);
                request.setAttribute("accountId",pageKey);
                request.setAttribute("book", bookKey);
            }

            request.setAttribute("headerType", headerType);
            request.setAttribute("tabId", tabId);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("userKey",userKey);

            modelAndView=new ModelAndView("search");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.search.page", null , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{userKey}/TagLinks.htm", method = RequestMethod.GET)
    public ModelAndView TagLinks(@PathVariable String userKey,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception
    {
        try
        {
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.access.taglink.page",null);
            }

            ModelAndView modelAndView=new ModelAndView("TagLinks");
            request.setAttribute("headerType", "user");
            request.setAttribute("tabId", "Home");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("userKey",userKey);
            return modelAndView;
        }
        catch(Exception ex)
        {
            throw new NGException("nugen.operation.fail.project.taglinks.page",null , ex);
        }
    }

    @RequestMapping(value = "/searchPublicNotes.htm")
    public ModelAndView searchPublicNotes(
              HttpServletRequest request, HttpServletResponse response)
              throws Exception {

        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            String searchText   = ar.reqParam("searchText");
            SearchResultRecord[] searchResultRecord = DataFeedServlet.performLuceneSearchOperationForPublicNotes(ar, searchText);
            request.setAttribute("searchResultRecord",searchResultRecord);
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.search.public.note.page", null , ex);
        }
        return  new ModelAndView("showSearchResult");
    }

    @RequestMapping(value = "/{accountId}/{pageId}/subprocess.htm", method = RequestMethod.GET)
    public ModelAndView showSubProcessTab(@PathVariable String accountId,@PathVariable String pageId,
              HttpServletRequest request, HttpServletResponse response,@RequestParam String subprocess)
              throws Exception {

        ModelAndView modelAndView = null;
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            modelAndView=new ModelAndView("ProjectActiveTasks");

            request.setAttribute("subprocess", subprocess);
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Tasks");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.subprocess.page", new Object[]{pageId,accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/{pageId}/reminders.htm", method = RequestMethod.GET)
    public ModelAndView remindersTab(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            ar.setPageAccessLevels(nGPage);

            ModelAndView modelAndView = null;
            if(!ar.isLoggedIn()){
                return needAccessView(request, "nugen.project.reminders.login.msg");
            }
            if(!ar.isMember()){
                return needAccessView(request, "nugen.projecthome.reminders.memberlogin");
            }

            modelAndView=new ModelAndView("reminders");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.reminders");

            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Documents");
            request.setAttribute("title", nGPage.getFullName());
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.reminder.page", new Object[]{pageId,accountId} , ex);
        }
    }

    @RequestMapping(value = "/{userKey}/sendDailyDigestMail.htm", method = RequestMethod.POST)
    public ModelAndView sendDailyMail(@PathVariable String userKey,
          HttpServletRequest request, HttpServletResponse response)
          throws Exception {

        try{
            /**
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            UserProfile up = UserManager.getUserProfileByKey(userKey);
            Vector<WatchRecord> watchList = up.getWatchList();
            List<NGContainer> containers = new ArrayList<NGContainer>();
            the following line makes no sense!
            NGPageIndex index = (NGPageIndex)watchList.get( 0 );
            containers.add(index.getContainer());
            long lastSendTime = SuperAdminHelper.getLastNotificationSentTime();
            // if the parameter is not found in the parameters list, then find it out in the attributes list
            NGWebUtils.constructDailyDigestEmail(ar,containers,context,lastSendTime,ar.nowTime);
            return redirectBrowser(ar,"watchedProjects.htm");
            */

            throw new Exception("sendDailyMail has a logic error ... is this method really used?");

        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.send.daily.digest.mail", null, ex);
        }
    }

    @RequestMapping(value = "/{accountId}/{pageId}/inviteUser.htm", method = RequestMethod.GET)
    public ModelAndView inviteUers(@PathVariable String accountId,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            NGBook nGBook = nGPage.getAccount();

            // Set the Index page name
            List<NGRole> roles=nGPage.getAllRoles();

            ar.setPageAccessLevels(nGPage);

            String userId = ar.reqParam( "emailId" );
            ModelAndView modelAndView = new ModelAndView("inviteUser");
            modelAndView.addObject("page", nGPage);
            modelAndView.addObject("tanent", nGBook);
            modelAndView.addObject("emailId", userId);

            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("roles", roles);
            request.setAttribute("tabId", "Project Settings");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.Permissions");
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.invite.user.page", new Object[]{pageId,accountId} , ex);
        }
    }

    @RequestMapping(value = "/CommentEmailAction.form", method = RequestMethod.POST)
    public void commentEmailAction(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        AuthRequest ar = AuthRequest.getOrCreate(request, response);
        if(!ar.isLoggedIn()){
            sendRedirectToLogin(ar, "message.loginalert.send.email",null);
            return;
        }

        String go = ar.reqParam("go");
        String encodingGuard  = ar.reqParam("encodingGuard");
        if (!"\u6771\u4eac".equals(encodingGuard)) {
            throw new Exception("values are corrupted");
        }

        String action = ar.reqParam("action");
        String p = ar.reqParam("p");
        String oid = ar.reqParam("oid");
        String emailto = ar.defParam("emailto", null);
        boolean fromPerson = "person".equals(ar.defParam("emailFrom", "person"));
        String toRole = ar.defParam("toRole", null);
        String note = ar.defParam("note", "");
        boolean exclude = (ar.defParam("exclude", null)!=null);
        boolean self    = (ar.defParam("self", null)!=null);
        boolean makeMember = (ar.defParam("makeMember", null)!=null);
        boolean includeBodyInMail = (ar.defParam("includeBody", null)!=null);
        NGContainer ngp = NGPageIndex.getContainerByKeyOrFail(p);
        ar.setPageAccessLevels(ngp);
        ar.assertContainerFrozen(ngp);

        String subject = ar.defParam("subject", null);

        NoteRecord noteRec = null;
        if (!oid.equals("x")) {
            noteRec = ngp.getNoteOrFail(oid);
        }

        StringBuffer outParams = new StringBuffer();
        appendIfNotNull(outParams, "?p=", p);
        appendIfNotNull(outParams, "&oid=", oid);
        appendIfNotNull(outParams, "&note=", note);
        appendIfNotNull(outParams, "&go=", go);
        appendIfNotNull(outParams, "&encodingGuard=", "\u6771\u4eac");
        if (exclude)
        {
            outParams.append("&exclude=true");
        }
        appendIfNotNull(outParams, "&toRole=", toRole);
        if (includeBodyInMail)
        {
            outParams.append("&includeBody=true");
        }
        appendIfNotNull(outParams, "&emailto=", emailto);
        appendIfNotNull(outParams, "&subject=", subject);
        for (AttachmentRecord att : ngp.getAllAttachments())
        {
            String paramId = "attach"+att.getId();
            String attParam = ar.defParam(paramId, null);
            if (attParam!=null)
            {
                outParams.append("&");
                outParams.append(paramId);
                outParams.append("=true");
            }
        }
        if (action.equals("Edit Mail"))
        {
            response.sendRedirect(ar.retPath+"t/sendNoteByEmail.htm"+outParams.toString());
            return;
        }
        if (action.equals("Preview Mail"))
        {
            response.sendRedirect(ar.retPath+"t/previewNoteForEmail.htm"+outParams.toString());
            return;
        }
        if (action.equals("Send Mail"))
        {
            if(!(ngp instanceof NGPage)) {
                throw new ProgramLogicError("The Send Mail Function is currently only able to handle NGPage objects");
                //the Members, Administrators, and Executives roles may not be available on other
                //classes, and may cause null pointer exceptions.
            }
            Vector<OptOutAddr> sendTo = new Vector<OptOutAddr>();
            if(toRole!=null){
                String[] sentToRole = UtilityMethods.splitOnDelimiter(toRole, ',');
                for(int i=0; i<sentToRole.length; i++){
                    String roleName = sentToRole[i];
                    NGRole role = ngp.getRole(roleName);
                    if (role!=null) {
                        NGWebUtils.appendUsersFromRole(ngp, roleName, sendTo);
                    }
                }
            }
            if (emailto!=null && emailto.length()>0) {
                NGRole memberRole = ngp.getRoleOrFail("Members");
                for (AddressListEntry enteredAddress : AddressListEntry.parseEmailList(emailto)) {
                    NGWebUtils.appendOneUser(new OptOutDirectAddress(enteredAddress), sendTo);
                    if(makeMember && !ngp.primaryOrSecondaryPermission(enteredAddress)) {
                        memberRole.addPlayer(enteredAddress);
                    }
                }
            }
            if (exclude) {
                for (LeafletResponseRecord llr : noteRec.getResponses()) {
                    String responder = llr.getUser();
                    removeFromList(sendTo, responder);
                }
            }
            if (self) {
                AddressListEntry aleself = new AddressListEntry(ar.getUserProfile());
                NGWebUtils.appendOneUser(new OptOutDirectAddress(aleself), sendTo);
            }

            StringBuffer historyNameList = new StringBuffer();
            boolean needComma = false;
            for (OptOutAddr ooa : sendTo)
            {
                String addr = ooa.getEmail();
                if (addr!=null && addr.length()>0)
                {
                    sendMessageToUser(ar, ngp, noteRec, ooa, fromPerson);
                    if (needComma)
                    {
                        historyNameList.append(",");
                    }
                    historyNameList.append(addr);
                    needComma= true;
                }
            }

            if(ngp instanceof NGPage){
                //OK, done, so write history about it
                HistoryRecord.createHistoryRecord(ngp,
                        oid, HistoryRecord.CONTEXT_TYPE_LEAFLET,0,
                        HistoryRecord.EVENT_EMAIL_SENT, ar, historyNameList.toString());
                ngp.saveContent(ar, "Sent a note by email");
                //note this also may save some of the new members if there are any
            }
        }
        response.sendRedirect(go);
    }




    private void appendIfNotNull(StringBuffer outParams, String prompt, String value) throws Exception {
        if (value!=null)
        {
            outParams.append(prompt);
            outParams.append(URLEncoder.encode(value, "UTF-8"));
        }
    }

    private void removeFromList(Vector<OptOutAddr> sendTo, String email) {
        OptOutAddr found = null;
        for (OptOutAddr ooa : sendTo) {
            if (ooa.matches(email)) {
                found = ooa;
                break;
            }
        }
        if (found!=null) {
            sendTo.remove(found);
        }
    }


      public void sendMessageToUser(AuthRequest ar, NGContainer ngp, NoteRecord noteRec, OptOutAddr ooa, boolean fromPerson)
          throws Exception
      {
          String userAddress = ooa.getEmail();
          if (userAddress==null || userAddress.length()==0)
          {
              //don't send anything if the user does not have an email address
              return;
          }
          String note = ar.defParam("note", "");

          StringWriter bodyWriter = new StringWriter();
          AuthRequest clone = new AuthDummy(ar.getUserProfile(), bodyWriter);
          clone.setNewUI(true);
          clone.retPath = ar.baseURL;
          clone.write("<html><body>");
          List<AttachmentRecord> attachList = NGWebUtils.getSelectedAttachments(ar, ngp);
          Vector<String> attachIds = new Vector<String>();

          //if you really want the files attached to the email message, then include a list of
          //their attachment ids here
          boolean includeFiles = (ar.defParam("includeFiles", null)!=null);
          if (includeFiles) {
              for (AttachmentRecord att : attachList) {
                  attachIds.add(att.getId());
              }
          }

          boolean tempmem = (ar.defParam("tempmem", null)!=null);
          boolean includeBody = (ar.defParam("includeBody", null)!=null);
          writeNoteAttachmentEmailBody(clone, ar.ngp, noteRec, tempmem, ooa.getAssignee(), note,
                includeBody, attachList);

          NGWebUtils.standardEmailFooter(clone, ar.getUserProfile(), ooa, ngp);

          clone.write("</body></html>");

          String subject = ar.defParam("subject", "Documents from Project "+ngp.getFullName());
          String fromAddress = null;
          if (fromPerson) {
              UserProfile up = ar.getUserProfile();
              if (up==null) {
                  throw new Exception("Problem with session: no user profile.  In order to send an email message usermust be logged in.");
              }
              fromAddress = up.getEmailWithName();
          }
          EmailSender.containerEmail(ooa, ngp, subject, bodyWriter.toString(), fromAddress, attachIds);
      }

      public static void writeNoteAttachmentEmailBody(AuthRequest ar,
              NGContainer ngp, NoteRecord selectedNote, boolean tempmem,
              AddressListEntry ale, String note, boolean includeBody,
              List<AttachmentRecord> selAtt) throws Exception {
          ar.write("<p><b>Note From:</b> ");
          ar.getUserProfile().writeLink(ar);
          ar.write(" &nbsp; <b>Project:</b> ");
          ngp.writeContainerLink(ar, 100);
          ar.write("</p>");
          ar.write("\n<p>");
          ar.writeHtml(note);
          ar.write("</p>");
          if (selAtt != null && selAtt.size() > 0) {
              ar.write("</p>");
              ar.write("\n<p><b>Attachments:</b> (click links for secure access to documents)<ul> ");
              for (AttachmentRecord att : selAtt) {
                  ar.write("<li><a href=\"");
                  ar.write(ar.retPath);
                  ar.write(ar.getResourceURL(ngp, "docinfo" + att.getId()
                          + ".htm?"));
                  ar.write(AccessControl.getAccessDocParams(ngp, att));
                  ar.write("\">");
                  ar.writeHtml(att.getNiceName());
                  ar.write("</a></li> ");
              }
              ar.write("</ul></p>");
          }
          if (selectedNote != null) {
              String noteURL = ar.retPath + ar.getResourceURL(ngp, selectedNote)
                      + "?" + AccessControl.getAccessNoteParams(ngp, selectedNote)
                      + "&emailId=" + URLEncoder.encode(ale.getEmail(), "UTF-8");
              if (includeBody) {
                  ar.write("\n<p><i>The note is copied below. You can access the most recent, ");
                  ar.write("most up to date version on the web at the following link:</i> <a href=\"");
                  ar.write(noteURL);
                  ar.write("\" title=\"Access the latest version of this message\"><b>");
                  if (selectedNote.getSubject() != "" && selectedNote.getSubject() != null) {
                      ar.writeHtml(selectedNote.getSubject());
                  }
                  else {
                      ar.writeHtml("Note Link");
                  }
                  ar.write("</b></a></p>");
                  ar.write("\n<hr/>\n");
                  ar.write("\n<div class=\"leafContent\" >");
                  WikiConverter.writeWikiAsHtml(ar, selectedNote.getData());
                  ar.write("\n</div>");
              }
              else {
                  ar.write("\n<p><i>Access the web page using the following link:</i> <a href=\"");
                  ar.write(noteURL);
                  ar.write("\" title=\"Access the latest version of this message\"><b>");
                  if (selectedNote.getSubject() != "" && selectedNote.getSubject() != null) {
                      ar.writeHtml(selectedNote.getSubject());
                  }
                  else {
                      ar.writeHtml("Note Link");
                  }
                  ar.write("</b></a></p>");
              }

              String choices = selectedNote.getChoices();
              String[] choiceArray = UtilityMethods
                      .splitOnDelimiter(choices, ',');
              UserProfile up = ale.getUserProfile();
              if (up != null && choiceArray.length > 0) {
                  selectedNote.getOrCreateUserResponse(up);
              }
              if (choiceArray.length > 0 & includeBody) {
                  ar.write("\n<p><font color=\"blue\"><i>This request has some response options.  Use the <a href=\"");
                  ar.write(noteURL);
                  ar.write("#Response\" title=\"Response form on the web\">web page</a> to respond to choose between: ");
                  int count = 0;
                  for (String ach : choiceArray) {
                      count++;
                      ar.write(" ");
                      ar.write(Integer.toString(count));
                      ar.write(". ");
                      ar.writeHtml(ach);
                  }
                  ar.write("</i></font></p>\n");
              }
          }
      }

    @RequestMapping(value = "/{accountId}/{pageId}/exportPDF.htm", method = RequestMethod.GET)
    public ModelAndView exportPDF(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String pageId,
            @PathVariable String accountId)
            throws Exception {

        ModelAndView modelAndView = null;
        try{
            request.setAttribute("book", accountId);
            request.setAttribute("pageId", pageId);
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            NGPageIndex.assertBook(accountId);
            if(!ar.isLoggedIn()){
                return needAccessView(request, "nugen.project.export.pdf.login.msg");
            }
            modelAndView=new ModelAndView("exportPDF");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Project Notes");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.export.pdf.page", new Object[]{pageId,accountId} , ex);
        }
        return modelAndView;
    }

      @RequestMapping(value = "/isNoteDeleted.ajax", method = RequestMethod.POST)
      public void isNoteDeleted(HttpServletRequest request, HttpServletResponse response)
             throws Exception {
         String responseText = null;
         AuthRequest ar = AuthRequest.getOrCreate(request, response);
         try {
             ar.assertLoggedIn("Must be logged in to create a note.");
             String p = ar.reqParam("p");
             NGContainer ngp = NGPageIndex.getContainerByKeyOrFail( p );
             ar.setPageAccessLevels(ngp);

             String oid = ar.reqParam("oid");
             JSONObject paramMap = new JSONObject();
             NoteRecord note = ngp.getNoteOrFail(oid);
             if(note.isDeleted()){
                 paramMap.put(Constant.MSG_TYPE, Constant.YES);
             }else{
                 paramMap.put(Constant.MSG_TYPE, Constant.No);
             }

             responseText = paramMap.toString();
         }
         catch (Exception ex) {
             responseText = NGWebUtils.getExceptionMessageForAjaxRequest(ex, ar.getLocale());
             ar.logException("Caught by isNoteDeleted.ajax", ex);
         }
         NGWebUtils.sendResponse(ar, responseText);
     }

      @RequestMapping(value = "/closeWindow.htm", method = RequestMethod.GET)
      public ModelAndView closeWindow(HttpServletRequest request, HttpServletResponse response)
              throws Exception {

          ModelAndView modelAndView = null;
          try{
              AuthRequest ar = AuthRequest.getOrCreate(request, response);
              if(!ar.isLoggedIn()){
                  return redirectToLoginView(ar, "message.must.be.login",null);
              }
              modelAndView=new ModelAndView("closeWindow");
              request.setAttribute("realRequestURL", ar.getRequestURL());
          }catch(Exception ex){
              throw new NGException("nugen.operation.fail.project.close.window", null , ex);
          }
          return modelAndView;
      }

      @RequestMapping(value = "/{accountId}/{pageId}/draftNotes.htm", method = RequestMethod.GET)
      public ModelAndView draftNotes(@PathVariable String accountId,@PathVariable String pageId,
              HttpServletRequest request, HttpServletResponse response)
             throws Exception {

          ModelAndView modelAndView = null;
          try{
              request.setAttribute("book", accountId);
              request.setAttribute("pageId", pageId);
              AuthRequest ar = AuthRequest.getOrCreate(request, response);

              NGPageIndex.assertBook(accountId);
              NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
              ar.setPageAccessLevels(nGPage);
              if(!ar.isLoggedIn()){
                  return needAccessView(request, "nugen.project.draft.notes.login.msg");
              }
              if(!ar.isMember()){
                  return needAccessView(request, "nugen.projecthome.draftnotes.memberlogin");
              }

              modelAndView=new ModelAndView("leaf_draftNotes");
              request.setAttribute("subTabId", "nugen.projecthome.subtab.draftNotes");
              request.setAttribute("visibility_value", "4");
              request.setAttribute("realRequestURL", ar.getRequestURL());
              request.setAttribute("tabId", "Project Notes");
          }catch(Exception ex){
              throw new NGException("nugen.operation.fail.project.draft.notes.page", new Object[]{pageId,accountId} , ex);
          }

          return modelAndView;
      }

      @RequestMapping(value = "/{accountId}/{pageId}/statusReport.form", method = RequestMethod.POST)
      public ModelAndView statusReport( @PathVariable String accountId,@PathVariable String pageId,
              HttpServletRequest request, HttpServletResponse response) throws Exception {
          try{
              request.setAttribute("book", accountId);
              request.setAttribute("pageId", pageId);
              AuthRequest ar = AuthRequest.getOrCreate(request, response);
              if(!ar.isLoggedIn()){
                  return redirectToLoginView(ar, "message.must.be.login.to.perform.action",null);
              }
              request.setAttribute("realRequestURL", ar.getRequestURL());
              request.setAttribute("tabId", "Status");

              String PROCESS_HTML = "statusReport.htm?startDate="+ ar.defParam("startDate", "")+"&endDate="+ar.defParam("endDate", "");
              return redirectBrowser(ar,PROCESS_HTML);
          }catch(Exception ex){
              throw new NGException("nugen.operation.fail.project.status.report.page", new Object[]{pageId,accountId} , ex);
          }
      }

      @RequestMapping(value = "/{accountId}/{pageId}/emailrecords.htm", method = RequestMethod.GET)
      public ModelAndView getEmailRecordsPage( @PathVariable String accountId,@PathVariable String pageId,
              HttpServletRequest request, HttpServletResponse response) throws Exception {
          ModelAndView modelAndView = null;

          try{
              request.setAttribute("book", accountId);
              request.setAttribute("pageId", pageId);
              AuthRequest ar = AuthRequest.getOrCreate(request, response);
              NGPage ngp =  getAccountProjectOrFail(accountId, pageId);
              ar.setPageAccessLevels(ngp);

              if(!ar.isLoggedIn()){
                  return needAccessView(request, "nugen.project.upload.email.reminder.login.msg");
              }
              if(!ar.isMember()){
                  return needAccessView(request, "nugen.projectsettings.emailRecords.memberlogin");
              }

              modelAndView=new ModelAndView("emailrecords");
              request.setAttribute("subTabId", "nugen.projectsettings.subtab.emailRecords");
              request.setAttribute("realRequestURL", ar.getRequestURL());
              request.setAttribute("tabId", "Project Settings");
          }catch(Exception ex){
              throw new NGException("nugen.operation.fail.project.emailrecords.page", new Object[]{pageId,accountId} , ex);
          }
          return modelAndView;
      }

      @RequestMapping(value = "/{accountId}/{pageId}/projectActiveTasks.htm", method = RequestMethod.GET)
      public ModelAndView projectActiveTasks(@PathVariable String accountId,@PathVariable String pageId,
              HttpServletRequest request, HttpServletResponse response)
              throws Exception {

          ModelAndView modelAndView = null;
          try{
              request.setAttribute("book", accountId);
              request.setAttribute("pageId", pageId);
             AuthRequest ar = AuthRequest.getOrCreate(request, response);
              NGPageIndex.assertBook(accountId);
              NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
              ar.setPageAccessLevels(nGPage);

              if(!ar.isLoggedIn()){
                  return needAccessView(request, "nugen.project.task.login.msg");
              }
              if(!ar.isMember()){
                  return needAccessView(request, "nugen.projecthome.task.memberlogin");
              }

              modelAndView=new ModelAndView("ProjectActiveTasks");

              //this page has a required parameter 'active', test it here so that any error
              //happens in the controller, not the page.
              String active = ar.defParam("active", "1");

              request.setAttribute("realRequestURL", ar.getRequestURL());
              request.setAttribute("tabId", "Project Tasks");
              request.setAttribute("title",  nGPage.getFullName());
              request.setAttribute("active", active);
              request.setAttribute("subTabId", "nugen.projecttasks.subtab.active.tasks");
          }catch(Exception ex){
              throw new NGException("nugen.operation.fail.project.process.page", new Object[]{pageId,accountId} , ex);
          }
          return modelAndView;

      }

      @RequestMapping(value = "/{accountId}/{pageId}/projectCompletedTasks.htm", method = RequestMethod.GET)
      public ModelAndView projectCompletedTasks(@PathVariable String accountId,@PathVariable String pageId,
              HttpServletRequest request, HttpServletResponse response)
              throws Exception {

          ModelAndView modelAndView = null;
          try{
              request.setAttribute("book", accountId);
              request.setAttribute("pageId", pageId);
              AuthRequest ar = AuthRequest.getOrCreate(request, response);
              NGPageIndex.assertBook(accountId);
              NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
              ar.setPageAccessLevels(nGPage);

              if(!ar.isLoggedIn()){
                  return needAccessView(request, "nugen.project.task.login.msg");
              }
              if(!ar.isMember()){
                  return needAccessView(request, "nugen.projecthome.task.memberlogin");
              }

              modelAndView=new ModelAndView("ProjectCompletedTasks");

              //this page has a required parameter 'active', test it here so that any error
              //happens in the controller, not the page.
              String active = ar.defParam("active", "1");

              request.setAttribute("realRequestURL", ar.getRequestURL());
              request.setAttribute("tabId", "Project Tasks");
              request.setAttribute("title",  nGPage.getFullName());
              request.setAttribute("active", active);
              request.setAttribute("subTabId", "nugen.projecttasks.subtab.completed.tasks");
          }catch(Exception ex){
              throw new NGException("nugen.operation.fail.project.process.page", new Object[]{pageId,accountId} , ex);
          }
          return modelAndView;

      }

      @RequestMapping(value = "/{accountId}/{pageId}/projectFutureTasks.htm", method = RequestMethod.GET)
      public ModelAndView projectFutureTasks(@PathVariable String accountId,@PathVariable String pageId,
              HttpServletRequest request, HttpServletResponse response)
              throws Exception {

          ModelAndView modelAndView = null;
          try{
              request.setAttribute("book", accountId);
              request.setAttribute("pageId", pageId);
              AuthRequest ar = AuthRequest.getOrCreate(request, response);
              NGPageIndex.assertBook(accountId);
              NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
              ar.setPageAccessLevels(nGPage);

              if(!ar.isLoggedIn()){
                  return needAccessView(request, "nugen.project.task.login.msg");
              }
              if(!ar.isMember()){
                  return needAccessView(request, "nugen.projecthome.task.memberlogin");
              }

              modelAndView=new ModelAndView("ProjectFutureTasks");

              //this page has a required parameter 'active', test it here so that any error
              //happens in the controller, not the page.
              String active = ar.defParam("active", "1");

              request.setAttribute("realRequestURL", ar.getRequestURL());
              request.setAttribute("tabId", "Project Tasks");
              request.setAttribute("title",  nGPage.getFullName());
              request.setAttribute("active", active);
              request.setAttribute("subTabId", "nugen.projecttasks.subtab.future.tasks");
          }catch(Exception ex){
              throw new NGException("nugen.operation.fail.project.process.page", new Object[]{pageId,accountId} , ex);
          }
          return modelAndView;

      }

      @RequestMapping(value = "/{accountId}/{pageId}/projectAllTasks.htm", method = RequestMethod.GET)
      public ModelAndView projectAllTasks(@PathVariable String accountId,@PathVariable String pageId,
              HttpServletRequest request, HttpServletResponse response)
              throws Exception {

          ModelAndView modelAndView = null;
          try{
              request.setAttribute("book", accountId);
              request.setAttribute("pageId", pageId);
              AuthRequest ar = AuthRequest.getOrCreate(request, response);
              NGPageIndex.assertBook(accountId);
              NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
              ar.setPageAccessLevels(nGPage);

              if(!ar.isLoggedIn()){
                  return needAccessView(request, "nugen.project.task.login.msg");
              }
              if(!ar.isMember()){
                  return needAccessView(request, "nugen.projecthome.task.memberlogin");
              }

              modelAndView=new ModelAndView("ProjectsAllTasks");

              //this page has a required parameter 'active', test it here so that any error
              //happens in the controller, not the page.
              String active = ar.defParam("active", "1");

              request.setAttribute("realRequestURL", ar.getRequestURL());
              request.setAttribute("tabId", "Project Tasks");
              request.setAttribute("title",  nGPage.getFullName());
              request.setAttribute("active", active);
              request.setAttribute("subTabId", "nugen.projecttasks.subtab.all.tasks");
          }catch(Exception ex){
              throw new NGException("nugen.operation.fail.project.process.page", new Object[]{pageId,accountId} , ex);
          }
          return modelAndView;

      }

      @RequestMapping(value = "/{accountId}/{pageId}/statusReport.htm", method = RequestMethod.GET)
      public ModelAndView projectStatusReport(@PathVariable String accountId,@PathVariable String pageId,
              HttpServletRequest request, HttpServletResponse response)
              throws Exception {

          ModelAndView modelAndView = null;
          try{
              request.setAttribute("book", accountId);
              request.setAttribute("pageId", pageId);
              AuthRequest ar = AuthRequest.getOrCreate(request, response);
              NGPageIndex.assertBook(accountId);
              NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
              ar.setPageAccessLevels(nGPage);

              if(!ar.isLoggedIn()){
                  return needAccessView(request, "nugen.project.task.login.msg");
              }
              if(!ar.isMember()){
                  return needAccessView(request, "nugen.projecthome.task.memberlogin");
              }

              modelAndView=new ModelAndView("ProjectStatusReport");

              //this page has a required parameter 'active', test it here so that any error
              //happens in the controller, not the page.
              String active = ar.defParam("active", "1");

              request.setAttribute("realRequestURL", ar.getRequestURL());
              request.setAttribute("tabId", "Project Tasks");
              request.setAttribute("title",  nGPage.getFullName());
              request.setAttribute("active", active);
              request.setAttribute("subTabId", "nugen.projecttasks.subtab.status.report");
          }catch(Exception ex){
              throw new NGException("nugen.operation.fail.project.process.page", new Object[]{pageId,accountId} , ex);
          }
          return modelAndView;

      }
}
