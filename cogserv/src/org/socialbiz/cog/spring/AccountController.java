/*
 * Copyright 2013 Keith D Swenson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors Include: Shamim Quader, Sameer Pradhan, Kumar Raja, Jim Farris,
 * Sandia Yang, CY Chen, Rajiv Onat, Neal Wang, Dennis Tam, Shikha Srivastava,
 * Anamika Chaudhari, Ajay Kakkar, Rajeev Rastogi
 */

package org.socialbiz.cog.spring;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socialbiz.cog.AccessControl;
import org.socialbiz.cog.HistoricActions;
import org.socialbiz.cog.SiteReqFile;
import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.LeafletResponseRecord;
import org.socialbiz.cog.NGBook;
import org.socialbiz.cog.NGPageIndex;
import org.socialbiz.cog.NoteRecord;
import org.socialbiz.cog.SiteRequest;
import org.socialbiz.cog.UserManager;
import org.socialbiz.cog.UserProfile;
import org.socialbiz.cog.UtilityMethods;
import org.socialbiz.cog.exception.NGException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class AccountController extends BaseController {

    @RequestMapping(value = "/{userKey}/requestAccount.htm", method = RequestMethod.GET)
    public ModelAndView requestSite(@PathVariable String userKey,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            if (needsToSetName(ar)) {
                return new ModelAndView("requiredName");
            }
            if (needsToSetEmail(ar)) {
                return new ModelAndView("requiredEmail");
            }
            if (UserManager.getAllSuperAdmins(ar).size()==0) {
                return showWarningView(ar, "nugen.missingSuperAdmin");
            }

            ModelAndView modelAndView = new ModelAndView("RequestAccount");
            request.setAttribute("userKey", userKey);
            request.setAttribute("pageTitle", "New Site Request Form");
            request.setAttribute("tabId", "Settings");
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.request.page", null, ex);
        }
    }

    @RequestMapping(value = "/{userKey}/accountRequests.form", method = RequestMethod.POST)
    public ModelAndView requestNewSite(@PathVariable
            String userKey, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }

            String action = ar.reqParam( "action" );

            if(action.equals( "Submit" )){

                String accountID = ar.reqParam("accountID");
                String accountName = ar.reqParam("accountName");
                String accountDesc = ar.defParam("accountDesc","");

                HistoricActions ha = new HistoricActions(ar);
                ha.createNewSiteRequest(accountID, accountName, accountDesc);
            }
            else {
                throw new Exception("Method requestNewSite does not understand the action: "+action);
            }

            modelAndView = new ModelAndView(new RedirectView("userAccounts.htm"));
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.new.account.request", null , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/acceptOrDeny.form", method = RequestMethod.POST)
    public ModelAndView acceptOrDeny(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            if (!ar.isSuperAdmin()) {
                throw new NGException("nugen.exceptionhandling.account.approval.rights",null);
            }

            String requestId = ar.reqParam("requestId");
            SiteRequest siteRequest = SiteReqFile.getRequestByKey(requestId);
            if (siteRequest==null) {
                throw new NGException("nugen.exceptionhandling.not.find.account.request",new Object[]{requestId});
            }

            String action = ar.reqParam("action");
            String description = ar.defParam("description", "");
            HistoricActions ha = new HistoricActions(ar);
            if ("Granted".equals(action)) {
                ha.completeSiteRequest(siteRequest, true, description);
            }
            else if("Denied".equals(action)) {
                ha.completeSiteRequest(siteRequest, false, description);
            }
            else{
                throw new Exception("Unrecognized action '"+action+"' in acceptOrDeny.form");
            }
            
            //TODO: need a go parameter
            return new ModelAndView(new RedirectView("requestedAccounts.htm"));
        }
        catch(Exception ex){
            throw new NGException("nugen.operation.fail.acceptOrDeny.account.request", null, ex);
        }
    }

     
    /**
    * This displays the page of site requests that have been made by others
    * and their current status.  Thus, only current executives and owners should see this.
    */
    @RequestMapping(value = "/{siteId}/$/roleRequest.htm", method = RequestMethod.GET)
    public ModelAndView remindersTab(@PathVariable String siteId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }

            modelAndView = new ModelAndView("account_role_request");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Site Settings");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.role.request");
            request.setAttribute("pageTitle", site.getFullName());
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.role.request.page", new Object[]{siteId} , ex);
        }
    }


    @RequestMapping(value = "/{siteId}/$/accountListProjects.htm", method = RequestMethod.GET)
    public ModelAndView showSiteTaskTab(@PathVariable String siteId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }

            modelAndView = new ModelAndView("accountListProjects");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Site Projects");
            request.setAttribute("pageTitle", site.getFullName());
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.process.page", new Object[]{siteId} , ex);
        }
    }

    @RequestMapping(value = "/{siteId}/$/accountCreateProject.htm", method = RequestMethod.GET)
    public ModelAndView accountCreateProject(@PathVariable String siteId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }

            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Site Projects");
            request.setAttribute("pageTitle", site.getFullName());
            return new ModelAndView("accountCreateProject");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.process.page", new Object[]{siteId} , ex);
        }
    }

    @RequestMapping(value = "/{siteId}/$/convertFolderProject.htm", method = RequestMethod.GET)
    public ModelAndView convertFolderProject(@PathVariable String siteId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }

            return new ModelAndView("convertFolderProject");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.process.page",
                    new Object[]{siteId} , ex);
        }
    }


    @RequestMapping(value = "/{siteId}/$/account_attachment.htm", method = RequestMethod.GET)
    public ModelAndView showAccountDocumentTab(@PathVariable String siteId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        AuthRequest ar = AuthRequest.getOrCreate(request, response);
        return showWarningView(ar, "nugen.deprecatedView");
    }
    @RequestMapping(value = "/{siteId}/$/admin.htm", method = RequestMethod.GET)
    public ModelAndView showSiteSettingTab(@PathVariable String siteId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }

            modelAndView = new ModelAndView("UserAccountSetting");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Site Settings");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.Admin");
            request.setAttribute("visibility_value", "3");
            request.setAttribute("pageTitle", site.getFullName());
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.admin.page", new Object[]{siteId}, ex);
        }
    }

    @RequestMapping(value = "/{siteId}/$/account_settings.htm", method = RequestMethod.GET)
    public ModelAndView showProjectSettingsTab(@PathVariable String siteId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = new ModelAndView(new RedirectView("personal.htm"));
        return modelAndView;
    }


    @RequestMapping(value = "/approveAccountThroughMail.htm", method = RequestMethod.GET)
    public ModelAndView approveSiteThroughEmail(
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);

            String requestId = ar.reqParam("requestId");
            String userId = ar.defParam("userId", null);
            boolean canAccess = false;
            if(userId != null){
                SiteRequest accountDetails=SiteReqFile.getRequestByKey(requestId);
                canAccess = AccessControl.canAccessSiteRequest(ar, userId, accountDetails);
            }

            if(!canAccess) {
                if(!ar.isLoggedIn()){
                    return showWarningView(ar, "message.loginalert.see.page");
                }
                throw new Exception("Not able to access.");
            }
            //Note: the approval page works in two modes.
            //1. if you are super admin, you have buttons to grant or deny
            //2. if you are not super admin, you can see status, but can not change status

            modelAndView = new ModelAndView("approveAccountThroughMail");
            modelAndView.addObject("requestId", requestId);
            modelAndView.addObject("canAccess", String.valueOf(canAccess));
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.approve.through.mail", null, ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{siteId}/$/CreateAccountRole.form", method = RequestMethod.POST)
    public ModelAndView createRole(@PathVariable String siteId,HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {
        try {
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }

            String roleName=ar.reqParam("rolename");
            String des=ar.reqParam("description");

            site.createRole(roleName,des);
            site.saveFile(ar, "Add New Role "+roleName+" to roleList");

            return new ModelAndView(new RedirectView("permission.htm"));
        } catch (Exception e) {
            throw new NGException("nugen.operation.fail.account.create.role",new Object[]{siteId}, e);
        }
    }

    @RequestMapping(value = "/{siteId}/$/public.htm", method = RequestMethod.GET)
    public ModelAndView viewSitePublic(@PathVariable String siteId,HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);

            ModelAndView modelAndView=new ModelAndView("account_public");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Site Notes");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.public");
            request.setAttribute("visibility_value", "1");
            request.setAttribute("pageTitle", site.getFullName());
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.public.page", new Object[]{siteId}, ex);
        }
    }

    @RequestMapping(value = "/{siteId}/$/member.htm", method = RequestMethod.GET)
    public ModelAndView viewAccountMember(@PathVariable String siteId,HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }

            modelAndView = new ModelAndView("account_member");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Site Notes");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.member");
            request.setAttribute("visibility_value", "1");
            request.setAttribute("pageTitle", site.getFullName());
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.member.page", new Object[]{siteId}, ex);
        }
    }

    @RequestMapping(value = "/{siteId}/$/account_history.htm", method = RequestMethod.GET)
    public ModelAndView viewAccountHistory(@PathVariable String siteId,HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }

            modelAndView = new ModelAndView("account_history");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Site Notes");
            request.setAttribute("subTabId", "nugen.accounthome.subtab.accountbulletin");
            request.setAttribute("visibility_value", "1");
            request.setAttribute("pageTitle", site.getFullName());
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.history.page", new Object[]{siteId}, ex);
        }
    }

    @RequestMapping(value = "/{siteId}/$/leafletResponse.htm", method = RequestMethod.POST)
    public ModelAndView handleLeafletResponse(@PathVariable String siteId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }
            String lid = ar.reqParam("lid");
            NoteRecord note = site.getNoteOrFail(lid);

            String go = ar.reqParam("go");
            String action = ar.reqParam("action");
            String data = ar.defParam("data", null);
            String choice = ar.defParam("choice", null);
            String uid = ar.reqParam("uid");
            UserProfile designatedUser = UserManager.findUserByAnyId(uid);
            if (designatedUser==null)
            {
                //create a user profile for this user at this point because you have to have
                //a user profile in order to access the response record.
                designatedUser = UserManager.createUserWithId(uid);
                designatedUser.setLastUpdated(ar.nowTime);
                UserManager.writeUserProfilesToFile();
            }

            LeafletResponseRecord llr = note.getOrCreateUserResponse(designatedUser);

            if (action.startsWith("Update"))
            {
                llr.setData(data);
                llr.setChoice(choice);
                llr.setLastEdited(ar.nowTime);
                site.saveContent(ar, "Updated response to note");
            }
            modelAndView=new ModelAndView(new RedirectView(ar.retPath+go ));
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.note", new Object[]{siteId}, ex);
        }
    }

    @RequestMapping(value = "/{siteId}/$/addmemberrole.htm", method = RequestMethod.GET)
    public ModelAndView addMemberRole(@PathVariable String siteId,HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }
            String roleMember = ar.reqParam("rolemember");
            roleMember = pasreFullname(roleMember);

            String roleName = ar.reqParam("roleList");

        	HistoricActions ha = new HistoricActions(ar);
        	ha.addMembersToRole(site, site.getRoleOrFail(roleName), roleMember, true);
            
            site.saveFile(ar, "Add New Member ("+roleMember+") to Role "+roleName);

            String emailIds = ar.reqParam("rolemember");
            NGWebUtils.updateUserContactAndSaveUserPage(ar, "Add", emailIds);

            modelAndView = new ModelAndView(new RedirectView("permission.htm"));
            modelAndView.addObject("page", site);
            request.setAttribute("tabId", "Project Settings");
            request.setAttribute("pageId", siteId);
            request.setAttribute("pageTitle", site.getFullName());
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.add.member.role", new Object[]{siteId}, ex);
        }
    }

    @RequestMapping(value = "/{siteId}/$/permission.htm", method = RequestMethod.GET)
    public ModelAndView showPermissionTab(@PathVariable String siteId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }
            modelAndView = new ModelAndView("account_permission");
            request.setAttribute("headerType", "site");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Site Settings");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.Permissions");
            request.setAttribute("pageTitle", site.getFullName());
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.permission.page", new Object[]{siteId}, ex);
        }
    }


    @RequestMapping(value = "/{siteId}/$/personal.htm", method = RequestMethod.GET)
    public ModelAndView showPersonalTab(@PathVariable String siteId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            NGBook site = prepareSiteView(ar, siteId);
            //personal view is available to everyone, regardless of whether they
            //have a role or access to that project.  This is the page that
            //one can request access.

            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Site Settings");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.personal");
            request.setAttribute("visibility_value", "4");
            request.setAttribute("pageTitle", site.getFullName());
            return new ModelAndView("account_personal");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.personal.page", new Object[]{siteId}, ex);
        }
    }

    public String pasreFullname(String fullNames) throws Exception {

        String assigness = "";
        String[] fullnames = UtilityMethods.splitOnDelimiter(fullNames, ',');
        for(int i=0; i<fullnames.length; i++){
            String fname = fullnames[i];
           if(!fname.equalsIgnoreCase("")){
                int bindx = fname.indexOf('<');
                int length = fname.length();
                if(bindx > 0){
                    fname = fname.substring(bindx+1,length-1);
                }
                assigness = assigness + "," + fname;
           }
        }
        if(assigness.startsWith(",")){
            assigness = assigness.substring(1);
        }
        return assigness;
    }

    @RequestMapping(value = "/{siteId}/$/getUsers.ajax", method = RequestMethod.GET)
    public void getUsers(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String siteId) throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                sendRedirectToLogin(ar, "message.loginalert.get.userlist",null);
                return;
            }
            NGPageIndex.assertBook(siteId);
            String matchKey = ar.defParam("matchkey", "");
            String users = UserManager.getUserFullNameList(matchKey);
            users = users.replaceAll("\"", "");
            NGWebUtils.sendResponse(ar, users);
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.get.users", null, ex);
        }
    }

    @RequestMapping(value = "/{siteId}/$/EditRoleBook.htm", method = RequestMethod.GET)
    public ModelAndView editRoleBook(@PathVariable String siteId,
            @RequestParam String roleName, @RequestParam String projectName,
            HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return showWarningView(ar, "message.loginalert.see.page");
            }
            prepareSiteView(ar, siteId);
            ModelAndView modelAndView = executiveCheckViews(ar);
            if (modelAndView != null) {
                return modelAndView;
            }
            modelAndView=new ModelAndView("editRoleAccount");
            request.setAttribute("headerType", "site");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Site Settings");
            request.setAttribute("roleName", roleName);
            request.setAttribute("projectName", projectName);
            return modelAndView;
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.editrolebook",new Object[]{siteId});
        }
    }

    @RequestMapping(value="/{siteId}/$/a/{docId}.{ext}", method = RequestMethod.GET)
    public void loadDocument(
          @PathVariable String siteId,
          @PathVariable String docId,
          @PathVariable String ext,
          HttpServletRequest request,
          HttpServletResponse response) throws Exception {
       try{
           AuthRequest ar = AuthRequest.getOrCreate(request, response);
           if(!ar.isLoggedIn()){
               sendRedirectToLogin(ar, "message.loginalert.access.attachment",null);
               return;
           }

           NGPageIndex.assertBook(siteId);

           String attachmentName = URLDecoder.decode(docId,"UTF-8")+"."+ext;

           NGBook ngb = NGPageIndex.getSiteByIdOrFail(siteId);
           ar.setPageAccessLevels(ngb);
           String version = ar.reqParam("version");
           AttachmentHelper.serveUpFileNewUI(ar, ngb, attachmentName,Integer.parseInt(version));

       }catch(Exception ex){
           throw new NGException("nugen.operation.fail.account.download.document", new Object[]{docId, siteId}, ex);
       }
   }

}
