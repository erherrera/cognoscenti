package org.socialbiz.cog.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.BaseRecord;
import org.socialbiz.cog.GoalRecord;
import org.socialbiz.cog.HistoryRecord;
import org.socialbiz.cog.IdGenerator;
import org.socialbiz.cog.LicensedURL;
import org.socialbiz.cog.NGBook;
import org.socialbiz.cog.NGPage;
import org.socialbiz.cog.NGPageIndex;
import org.socialbiz.cog.ProcessRecord;
import org.socialbiz.cog.SectionWiki;
import org.socialbiz.cog.UtilityMethods;
import org.socialbiz.cog.exception.NGException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This class will handle all requests that are coming to create a new Project.
 * Currently this is handling only requests that are coming to create a new
 * project from scratch. Later this can be extended to edit a project or to
 * create a project from Template.
 *
 * @author Pawan Chopra Jul 6, 2010
 */
@Controller
public class CreateProjectController extends BaseController {


    private ApplicationContext context;
    @Autowired
    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    @RequestMapping(value = "/{book}/{pageId}/addmemberrole.htm", method = RequestMethod.GET)
    public ModelAndView addMemberRole(@PathVariable String book,@PathVariable String pageId,HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.add.member.to.role",null);
            }
            NGPageIndex.assertBook(book);
            NGPage page = (NGPage)NGPageIndex.getContainerByKeyOrFail(pageId);

            String invitedUser = ar.defParam( "invitedUser","false" );

            String action = ar.defParam( "action","submit" );
            if(!action.equals( "cancel" )){
                Vector<String> roleMembers = parseFullname(ar.reqParam("rolemember"));
                String roleName = ar.reqParam("roleList");
                for (String newUser : roleMembers) {
                    page.addMemberToRole(roleName,newUser);
                    NGWebUtils.sendInviteEmail( ar, pageId, newUser, roleName );
                    HistoryRecord.createHistoryRecord(page,newUser,HistoryRecord.CONTEXT_TYPE_PERMISSIONS,
                            0,HistoryRecord.EVENT_PLAYER_ADDED, ar, roleName);
                }
                page.savePage(ar, "Add New Members ("+roleMembers.size()+") to Role "+roleName);

                String emailIds = ar.reqParam("rolemember");
                NGWebUtils.updateUserContactAndSaveUserPage(ar, "Add", emailIds);
            }
            if(invitedUser.equals( "true" )){
                modelAndView = new ModelAndView(new RedirectView("projectActiveTasks.htm"));
            }else{
                modelAndView = new ModelAndView(new RedirectView("permission.htm"));
            }
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.add.member.role", new Object[]{pageId,book} , ex);
        }
        return modelAndView;
    }

    private Vector<String> parseFullname(String fullNames) throws Exception {

        Vector<String> assignees = new Vector<String>();
        String[] fullnames = UtilityMethods.splitOnDelimiter(fullNames, ',');
        for(int i=0; i<fullnames.length; i++){
            String fname = fullnames[i];
            if(!fname.equalsIgnoreCase("")){
                int bindx = fname.indexOf('<');
                int length = fname.length();
                if(bindx > 0){
                    fname = fname.substring(bindx+1,length-1);
                }
                assignees.add(fname);
            }
        }
        return assignees;
    }


    @RequestMapping(value = "/{book}/isProjectExist.ajax", method = RequestMethod.POST)
    public void isProjectExist(@RequestParam String book,
        ModelMap model, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        AuthRequest ar = NGWebUtils.getAuthRequest(request, response, "Could not check project name.");

        String message=projectNameValidity(book, ar,context);
        NGWebUtils.sendResponse(ar, message);
    }

    @RequestMapping(value = "/getProjectNames.ajax", method = RequestMethod.POST)
    public void getProjectNames(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String message="";
        AuthRequest ar = null;
        try{
            ar = NGWebUtils.getAuthRequest(request, response, "Could not find project name.");

            String matchKey = ar.defParam("matchkey", "").trim();
            String projects = NGPageIndex.getAllProjectFullNameList(matchKey);

            if(projects.length() >0){
                message = NGWebUtils.getJSONMessage(Constant.SUCCESS , projects , "");
            }else{
                message = NGWebUtils.getJSONMessage(Constant.FAILURE , context.getMessage("nugen.no.project.found",null, ar.getLocale()) , "");
            }
        }
        catch(Exception ex){
            message = NGWebUtils.getExceptionMessageForAjaxRequest(ex, ar.getLocale());
            ar.logException(message, ex);
        }
        NGWebUtils.sendResponse(ar, message);
    }

    @RequestMapping(value = "/getProjects.ajax", method = RequestMethod.GET)
    public void getProjects(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        AuthRequest ar = null;
        try{
            ar = NGWebUtils.getAuthRequest(request, response, "can not get projects");
            String matchKey = ar.defParam("matchkey", "").trim();
            String bookKey = ar.defParam("book", "").trim();

            String projects = NGPageIndex.getProjectFullNameList(matchKey,bookKey);
            NGWebUtils.sendResponse(ar, projects);
        }
        catch(Exception ex){
            String message = NGWebUtils.getExceptionMessageForAjaxRequest(ex, ar.getLocale());
            ar.logException(message, ex);
        }
    }

    //the unknown part of the path may be either a user id or an account id
    //because this is used in two different places.  Should reconsider this.
    @RequestMapping(value = "/{accountId}/$/createprojectFromTemplate.form", method = RequestMethod.POST)
    public void createprojectFromTemplate(@PathVariable String accountId, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                sendRedirectToLogin(ar, "message.login.to.create.page",null);
                return;
            }
            NGPage project= createTemplateProject(ar,accountId);
            response.sendRedirect(ar.retPath+"t/"+accountId+"/"+project.getKey()+"/public.htm");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.create.project.from.template", new Object[]{accountId} , ex);
        }
    }


    @RequestMapping(value = "/{book}/{pageId}/createProjectFromTask.htm", method = RequestMethod.GET)
    public ModelAndView createProjectFromTask(@PathVariable String book,@PathVariable String pageId,
            @RequestParam String newTaskname,@RequestParam String parentProcess,@RequestParam String goToUrl,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ModelAndView  modelAndView =null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.login.create.project.from.tasklist",null);
            }

            modelAndView = new ModelAndView("CreateProjectFromTask");

            List<NGBook> memberOfAccounts = new ArrayList<NGBook>();
            for(NGBook aBook : NGBook.getAllAccounts()) {
                if (aBook.primaryOrSecondaryPermission(ar.getUserProfile())) {
                    memberOfAccounts.add(aBook);
                }
            }

            request.setAttribute("newTaskname",newTaskname);
            request.setAttribute("bookList",memberOfAccounts);
            request.setAttribute("goUrl",goToUrl);
            request.setAttribute("book",book);

            String realRequestURL = request.getRequestURL().toString();
            request.setAttribute("realRequestURL", realRequestURL);
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.create.project.from.task", new Object[]{pageId,book} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{book}/{pageId}/isProjectExistOnSystem.ajax", method = RequestMethod.GET)
    public void isProjectExistOnSystem(@PathVariable
    String book, @RequestParam
    String projectname, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String message = "";
        AuthRequest ar = null;

        try{
            ar = NGWebUtils.getAuthRequest(request, response,"Could not check project name.");

            message=projectNameValidity(book, ar,context);

        }catch(Exception ex){
            ar.logException(message, ex);
        }
        NGWebUtils.sendResponse(ar, message);

    }

    @RequestMapping(value = "/{account}/{pageId}/createTemplateProject.form", method = RequestMethod.POST)
    public void createTemplateProject(@PathVariable String account,String pageId,
            ModelMap model, HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {

        try {
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                sendRedirectToLogin(ar, "message.login.create.template.from.project",null);
                return;
            }
            String accountId = ar.reqParam("accountId");
            String goUrl = ar.reqParam("goUrl");
            String parentTaskId=goUrl.substring(goUrl.lastIndexOf("=")+1,goUrl.length());
            String parentProcessUrl=ar.reqParam("parentProcessUrl");

            NGPage subProcess= createTemplateProject(ar,accountId);
            linkSubProcessToTask(ar,subProcess,parentTaskId,parentProcessUrl);

            response.sendRedirect(goUrl);
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.create.template.project", new Object[]{pageId,account} , ex);
        }
    }

    @RequestMapping(value = "/{userKey}/createProject.form", method = RequestMethod.POST)
    public void createProject(@PathVariable String userKey, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        try {
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                sendRedirectToLogin(ar, "message.login.create.project",null);
                return;
            }
            String accountId = ar.reqParam("accountId");
            NGPage project= createTemplateProject(ar,accountId);
            response.sendRedirect(ar.retPath+"t/"+accountId+"/"+project.getKey()+"/public.htm");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.create.project", null , ex);
        }
    }


    ////////////////// HELPER FUNCTIONS /////////////////////////////////


    public static boolean  isProjectExist(String projectName) throws Exception {

        Vector<NGPageIndex> foundPages = NGPageIndex
                .getPageIndexByName(projectName);
        if (foundPages.size() > 0) {
            return true;
        }
        return false;
    }

    private static String projectNameValidity(String book, AuthRequest ar,
            ApplicationContext context) throws Exception {
        String message = "";
        String projectName = ar.reqParam("projectname");
        try {
            NGPageIndex.assertBook(book);
            if (isProjectExist(projectName)) {
                message = NGWebUtils.getJSONMessage(Constant.YES, context.getMessage(
                        "nugen.userhome.project.name.already.exists",null, ar.getLocale()), "");
            } else {
                message = NGWebUtils.getJSONMessage(Constant.No, projectName,"");
            }
        } catch (Exception ex) {
            message = NGWebUtils.getExceptionMessageForAjaxRequest(ex, ar
                    .getLocale());
            ar.logException(message, ex);
        }
        return message;
    }


    private static String sanitizeHyphenate(String p) throws Exception {
        String plc = p.toLowerCase();
        StringBuffer result = new StringBuffer();
        boolean wasPunctuation = false;
        for (int i = 0; i < plc.length(); i++) {
            char ch = plc.charAt(i);
            boolean isAlphaNum = ((ch >= 'a') && (ch <= 'z'))
                    || ((ch >= '0') && (ch <= '9'));
            if (isAlphaNum) {
                if (wasPunctuation) {
                    result.append('-');
                    wasPunctuation = false;
                }
                result.append(ch);
            } else {
                wasPunctuation = true;
            }
        }
        return result.toString();
    }

    private static String findGoodFileName(String pt) throws Exception {
        String p = sanitizeHyphenate(pt);
        if (p.length() == 0) {
            p = IdGenerator.generateKey();
        }
        String extp = p;
        int incrementedExtension = 0;
        while (true) {
            File theFile = NGPage.getRealPath(extp + ".sp");
            if (!theFile.exists()) {
                return extp;
            }
            extp = p + "-" + (++incrementedExtension);
        }
    }


    private static NGPage createPage(AuthRequest ar, NGBook ngb)
            throws Exception {

        if (!ngb.primaryOrSecondaryPermission(ar.getUserProfile())) {
            throw new NGException("nugen.exception.not.member.of.account",
                    new Object[]{ngb.getName()});
        }

        String projectName = ar.reqParam("projectname");
        String projectFileName = findGoodFileName(projectName);

        String pageAddress = projectFileName + ".sp";
        NGPage ngPage = NGPage.createPage(ar, pageAddress, ngb);

        String[] nameSet = new String[] { projectName };
        ngPage.setPageNames(nameSet);

        //check for and set the upstream link
        String upstream = ar.defParam("upstream", null);
        if (upstream!=null && upstream.length()>0) {
            ngPage.setUpstreamLink(upstream);
        }

        ngPage.setAccount(ngb);
        ngPage.savePage(ar, "Creating a page");

        NGPageIndex.makeIndex(ngPage);
        ar.setPageAccessLevels(ngPage);

        return ngPage;
    }

    private static NGPage createTemplateProject(AuthRequest ar, String accountId) throws Exception {
        NGPage project = null;
        try {

            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            if (!ngb.primaryOrSecondaryPermission(ar.getUserProfile())) {
                throw new NGException("nugen.exception.not.a.member.of.account",
                        new Object[] { ngb.getFullName() });
            }

            String templateName = ar.defParam("templateName", "");
            if (!"".equals(templateName)) {
                NGPage template_ngp = (NGPage) NGPageIndex
                        .getContainerByKeyOrFail(templateName);

                String projectName = ar.reqParam("projectname");
                String projectFileName = findGoodFileName(projectName);

                String pageAddress = SectionWiki.sanitize(projectFileName)
                        + ".sp";

                project = NGPage.createFromTemplate(ar, pageAddress, ngb, template_ngp);

                String[] nameSet = new String[] { projectName };

                project.setPageNames(nameSet);

                //check for and set the upstream link
                String upstream = ar.defParam("upstream", null);
                if (upstream!=null && upstream.length()>0) {
                    project.setUpstreamLink(upstream);
                }

                project.setAccount(ngb);
                project.savePage(ar, "Creating a project");
                NGPageIndex.makeIndex(project);
            } else {
                project = createPage(ar, ngb);
            }


        } catch (Exception ex) {
            throw new Exception("Unable to create a project from template for account "
                    +accountId, ex);
        }
        return project;
    }

    private static void linkSubProcessToTask(AuthRequest ar, NGPage subProject, String goalId,
            String parentProcessUrl) throws Exception {

        int beginOfPageKey = parentProcessUrl.indexOf("/p/") + 3;
        int endOfPageKey = parentProcessUrl.indexOf("/", beginOfPageKey);
        String projectKey = parentProcessUrl.substring(beginOfPageKey, endOfPageKey);

        ProcessRecord process = subProject.getProcess();
        process.setSynopsis("Goal Setting");
        process.setDescription("Purpose of Projec Setting");

        subProject.savePage(ar, "Changed Goal and/or Purpose of Project");
        LicensedURL parentLicensedURL = null;

        if (parentProcessUrl != null && parentProcessUrl.length() > 0) {
            parentLicensedURL = LicensedURL.parseCombinedRepresentation(parentProcessUrl);
            process.addLicensedParent(parentLicensedURL);
        }
        // link up the project with the parent task link
        if (parentLicensedURL != null) {
            LicensedURL thisUrl = process.getWfxmlLink(ar);

            // this is the subprocess address to link to
            String subProcessURL = thisUrl.getCombinedRepresentation();
            NGPage parentProject = NGPageIndex.getProjectByKeyOrFail(projectKey);

            GoalRecord goal = parentProject.getGoalOrFail(goalId);
            goal.setSub(subProcessURL);
            goal.setState(BaseRecord.STATE_WAITING);
            parentProject.savePage(ar, "Linked with Subprocess");
        }
    }
}
