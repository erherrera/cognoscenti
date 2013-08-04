package org.socialbiz.cog.spring;

import java.io.StringWriter;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socialbiz.cog.AccessControl;
import org.socialbiz.cog.AccountReqFile;
import org.socialbiz.cog.AddressListEntry;
import org.socialbiz.cog.AdminEvent;
import org.socialbiz.cog.AuthDummy;
import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.EmailSender;
import org.socialbiz.cog.LeafletResponseRecord;
import org.socialbiz.cog.NGBook;
import org.socialbiz.cog.NGPageIndex;
import org.socialbiz.cog.NoteRecord;
import org.socialbiz.cog.SuperAdminLogFile;
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

    @RequestMapping(value = "/{userKey}/accountRequests.form", method = RequestMethod.POST)
    public ModelAndView requestForNewAccount(@PathVariable
            String userKey, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.approve.account.requests",null);
            }

            String action = ar.reqParam( "action" );

            if(action.equals( "Submit" )){

                String accountID = ar.reqParam("accountID");
                String accountName = ar.reqParam("accountName");
                String accountDesc = ar.defParam("accountDesc","");
                AccountRequest accountDetails = AccountReqFile.requestForNewAccount(accountID,
                    accountName, accountDesc, ar);

                sendAccountRequestEmail( ar,  accountDetails);
            }

            modelAndView = new ModelAndView(new RedirectView("userAccounts.htm"));
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.new.account.request", null , ex);
        }
        return modelAndView;
    }

    /**
     * sends an email to the super admins of the server
     */
    private static void sendAccountRequestEmail(AuthRequest ar,
            AccountRequest accountDetails) throws Exception {
        StringWriter bodyWriter = new StringWriter();
        AuthRequest clone = new AuthDummy(ar.getUserProfile(), bodyWriter);
        clone.setNewUI(true);
        clone.retPath = ar.baseURL;
        clone.write("<html><body>\n");
        clone.write("<table>\n<tr><td>Purpose: &nbsp;</td><td>New Account Request</td></tr>");
        clone.write("\n<tr><td>Account Name: &nbsp;</td><td>");
        clone.writeHtml(accountDetails.getName());
        clone.write("</td></tr>");
        clone.write("\n<tr><td>Description: &nbsp;</td><td>");
        clone.writeHtml(accountDetails.getDescription());
        clone.write("</td></tr>");
        clone.write("\n<tr><td>Requested By: &nbsp;</td><td>");
        ar.getUserProfile().writeLink(clone);
        clone.write("</td></tr>");
        clone.write("\n<tr><td>Action: &nbsp;</td><td>");
        clone.write("<a href=\"");
        clone.write(ar.baseURL);
        clone.write("v/approveAccountThroughMail.htm?requestId=");
        clone.write(accountDetails.getRequestId());

        UserProfile up = UserManager.getSuperAdmin(ar);
        if (up != null) {
            clone.write("&userId=");
            clone.write(up.getKey());

            clone.write("&");
            clone.write(AccessControl.getAccessAccountRequestParams(
                    up.getKey(), accountDetails));
        }

        clone.write("\">Click here to Accept/Deny</a>");
        clone.write("</td></tr>");
        clone.write("</table>\n");
        clone.write("<p>Being a <b>Super Admin</b> of the Cognoscenti console, you have rights to accept or deny this request.</p>");
        clone.write("</body></html>");

        EmailSender.simpleEmail(NGWebUtils.getSuperAdminMailList(ar), null,
                "Account Approval for " + ar.getBestUserId(),
                bodyWriter.toString());
    }


    @RequestMapping(value = "/{accountId}/$/roleRequest.htm", method = RequestMethod.GET)
    public ModelAndView remindersTab(@PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.open.role.request",null);
            }
            NGPageIndex.assertBook(accountId);

            NGBook nGBook = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(nGBook);

            modelAndView = new ModelAndView("account_role_request");
            request.setAttribute("headerType", "account");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Account Settings");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.role.request");
            request.setAttribute("accountId", accountId);
            request.setAttribute("pageTitle", nGBook.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.role.request.page", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }


    @RequestMapping(value = "/acceptOrDeny.form", method = RequestMethod.POST)
    public ModelAndView acceptOrDeny(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);

            String userKey = ar.defParam("userKey", null);

            String requestId = ar.reqParam("requestId");
            AccountRequest accountDetails = AccountReqFile.getRequestByKey(requestId);
            if (accountDetails==null)
            {
                throw new NGException("nugen.exceptionhandling.not.find.account.request",new Object[]{requestId});
            }

            boolean canAccess = false;

            if(userKey != null){
                canAccess = AccessControl.canAccessAccountRequest(ar, userKey, accountDetails);
            }

            if(!canAccess){
                if(!ar.isLoggedIn()){
                    return redirectToLoginView(ar, "message.loginalert.account.requests",null);
                }

                if (!ar.isSuperAdmin())
                {
                    throw new NGException("nugen.exceptionhandling.account.approval.rights",null);
                }
            }

            String action = ar.reqParam("action");
            String modUser = "";
            if(ar.getUserProfile() != null){
                modUser = ar.getUserProfile().getPreferredEmail();
            }
            //Assign default member.
            AddressListEntry ale = new AddressListEntry(accountDetails.getUniversalId());
            String context=null;
            String uniqueId=requestId;
            boolean cancel = false;
            if ("Granted".equals(action)) {

                //Create new Account
                NGBook ngb = NGBook.createNewAccount(accountDetails.getAccountId(), accountDetails.getName());
                ngb.setKey(accountDetails.getAccountId());
                ngb.getPrimaryRole().addPlayer(ale);
                ngb.getSecondaryRole().addPlayer(ale);
                ngb.setDescription( ar.reqParam("description") );

                ngb.saveFile(ar, "New Account created");
                NGPageIndex.makeIndex(ngb);

                //Change the status accepted
                accountDetails.setStatus("Granted");
                accountDetails.setDescription(ar.reqParam("description"));
                context = AdminEvent.ACCOUNT_CREATED;
                uniqueId=ngb.getKey();

            } else if("Denied".equals(action)) {
                //Change the status Denied
                accountDetails.setStatus("Denied");
                //update the description if change
                accountDetails.setDescription(ar.reqParam("description"));
                context = AdminEvent.ACCOUNT_DENIED;
            }else{
                cancel= true;
            }

            if(!cancel){

                NGWebUtils.sendAccountGrantedEmail( ar, ale.getUserProfile(), accountDetails);
                AccountReqFile.saveAll();

                if(ar.getUserProfile() != null){
                    modelAndView = new ModelAndView(new RedirectView(ar.retPath+"v/"
                            +ar.getUserProfile().getKey()+"/userAccounts.htm"));
                }else{
                    modelAndView = new ModelAndView(new RedirectView("accountRequestResult.htm?requestId="+requestId));
                }
                SuperAdminLogFile.createAdminEvent(uniqueId, ar.nowTime,modUser, context);
            }

        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.acceptOrDeny.account.request", null, ex);
        }
        return modelAndView;

    }

    //this URL address is deprecated, but keep the redirect in case someone can stored a URL someplace
    @RequestMapping(value = "/{accountId}/$/accountHome.htm", method = RequestMethod.GET)
    public ModelAndView redirectToPublic1(@PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = new ModelAndView( new RedirectView("public.htm"));
        return modelAndView;
    }

    //this URL address is deprecated, but keep the redirect in case someone can stored a URL someplace
    @RequestMapping(value = "/{accountId}/$/account_public.htm", method = RequestMethod.GET)
    public ModelAndView redirectToPublic2(@PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = new ModelAndView( new RedirectView("public.htm"));
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/account_process.htm", method = RequestMethod.GET)
    public ModelAndView showAccountTaskTab(@PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.see.page",null);
            }

            NGPageIndex.assertBook(accountId);
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);

            modelAndView = new ModelAndView("account_projects");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("headerType", "account");
            request.setAttribute("tabId", "Account Projects");
            request.setAttribute("accountId", accountId);
            request.setAttribute("title", ngb.getFullName());
            request.setAttribute("pageTitle", ngb.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.process.page", new Object[]{accountId} , ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/account_attachment.htm", method = RequestMethod.GET)
    public ModelAndView showAccountDocumentTab(@PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.open.attachment.page",null);
            }
            NGPageIndex.assertBook(accountId);
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            modelAndView = new ModelAndView("accountDocumentPage");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("headerType", "account");
            request.setAttribute("tabId", "Account Documents");
            request.setAttribute("accountId", accountId);
            request.setAttribute("subTabId", "nugen.projecthome.subtab.documents");
            request.setAttribute("title", ngb.getFullName());
            request.setAttribute("pageTitle", ngb.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.attachment.page", new Object[]{accountId}, ex);
        }
        return modelAndView;

    }
    @RequestMapping(value = "/{accountId}/$/admin.htm", method = RequestMethod.GET)
    public ModelAndView showAccountSettingTab(@PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.open.admin.page",null);
            }
            NGPageIndex.assertBook(accountId);

            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);

            modelAndView = new ModelAndView("accountSettingPage");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("headerType", "account");
            request.setAttribute("tabId", "Account Settings");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.Admin");
            request.setAttribute("visibility_value", "3");
            request.setAttribute("accountId", accountId);
            request.setAttribute("title", ngb.getFullName());
            request.setAttribute("pageTitle", ngb.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.admin.page", new Object[]{accountId}, ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/account_settings.htm", method = RequestMethod.GET)
    public ModelAndView showProjectSettingsTab(@PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = new ModelAndView(new RedirectView("personal.htm"));
        return modelAndView;
    }


    @RequestMapping(value = "/approveAccountThroughMail.htm", method = RequestMethod.GET)
    public ModelAndView approveAccountThroughEmail(
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);

            String requestId = ar.reqParam("requestId");
            String userId = ar.defParam("userId", null);
            boolean canAccess = false;
            if(userId != null){
                AccountRequest accountDetails=AccountReqFile.getRequestByKey(requestId);
                canAccess = AccessControl.canAccessAccountRequest(ar, userId, accountDetails);
            }

            if(!canAccess && !ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.approve.account.request",null);
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

    @RequestMapping(value = "/{accountId}/$/CreateAccountRole.form", method = RequestMethod.POST)
    public ModelAndView createRole(@PathVariable String accountId,HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {
        try {
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.create.role",null);
            }
            NGPageIndex.assertBook(accountId);
            NGBook account = NGPageIndex.getAccountByKeyOrFail(accountId );
            ar.setPageAccessLevels(account);

            String roleName=ar.reqParam("rolename");
            String des=ar.reqParam("description");

            account.createRole(roleName,des);
            account.saveFile(ar, "Add New Role "+roleName+" to roleList");

            return new ModelAndView(new RedirectView("permission.htm"));
        } catch (Exception e) {
            throw new NGException("nugen.operation.fail.account.create.role",new Object[]{accountId}, e);
        }
    }

    @RequestMapping(value = "/{accountId}/$/public.htm", method = RequestMethod.GET)
    public ModelAndView viewAccountPublic(@PathVariable String accountId,HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.open.public.notes",null);
            }
            NGPageIndex.assertBook(accountId);
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            modelAndView=new ModelAndView("account_public");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("headerType", "account");
            request.setAttribute("accountId", accountId);
            request.setAttribute("tabId", "Account Notes");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.public");
            request.setAttribute("visibility_value", "1");
            request.setAttribute("title", ngb.getFullName());
            request.setAttribute("pageTitle", ngb.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.public.page", new Object[]{accountId}, ex);
        }
        return modelAndView;

    }

    @RequestMapping(value = "/{accountId}/$/member.htm", method = RequestMethod.GET)
    public ModelAndView viewAccountMember(@PathVariable String accountId,HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.open.member.notes",null);
            }
            NGPageIndex.assertBook(accountId);
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            modelAndView = new ModelAndView("account_member");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("headerType", "account");
            request.setAttribute("accountId", accountId);
            request.setAttribute("tabId", "Account Notes");
            request.setAttribute("subTabId", "nugen.projecthome.subtab.member");
            request.setAttribute("visibility_value", "1");
            request.setAttribute("title", ngb.getFullName());
            request.setAttribute("pageTitle", ngb.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.member.page", new Object[]{accountId}, ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/account_history.htm", method = RequestMethod.GET)
    public ModelAndView viewAccountHistory(@PathVariable String accountId,HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.open.history.page",null);
            }
            NGPageIndex.assertBook(accountId);
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            modelAndView = new ModelAndView("account_history");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("headerType", "account");
            request.setAttribute("accountId", accountId);
            request.setAttribute("tabId", "Account Notes");
            request.setAttribute("subTabId", "nugen.accounthome.subtab.accountbulletin");
            request.setAttribute("visibility_value", "1");
            request.setAttribute("title", ngb.getFullName());
            request.setAttribute("pageTitle", ngb.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.history.page", new Object[]{accountId}, ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{userKey}/requestAccount.htm", method = RequestMethod.GET)
    public ModelAndView requestAccount(@PathVariable String userKey,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.account.info",null);
            }
            modelAndView = new ModelAndView("RequestAccount");
            request.setAttribute("userKey", userKey);
            request.setAttribute("pageTitle", "New Account Request Form");
            request.setAttribute("tabId", "Settings");
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.request.page", null, ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/leafletResponse.htm", method = RequestMethod.POST)
    public ModelAndView handleLeafletResponse(@PathVariable String accountId,HttpServletRequest request, HttpServletResponse response)
           throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.see.page",null);
            }
            NGPageIndex.assertBook(accountId);

            NGBook account = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(account);
            String lid = ar.reqParam("lid");
            NoteRecord note = account.getNoteOrFail(lid);

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
                account.saveContent(ar, "Updated response to note");
            }
            modelAndView=new ModelAndView(new RedirectView(ar.retPath+go ));
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.note", new Object[]{accountId}, ex);
        }
        return modelAndView;

    }

    @RequestMapping(value = "/{accountId}/$/addmemberrole.htm", method = RequestMethod.GET)
    public ModelAndView addMemberRole(@PathVariable String accountId,HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.add.member.to.role",null);
            }
            NGPageIndex.assertBook(accountId);
            NGBook account = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(account);
            String roleMember = ar.reqParam("rolemember");
            roleMember = pasreFullname(roleMember);

            String roleName = ar.reqParam("roleList");

            account.addMemberToRole(roleName,roleMember);
            NGWebUtils.sendInviteEmail( ar, accountId, roleMember, roleName );
            account.saveFile(ar, "Add New Member ("+roleMember+") to Role "+roleName);

            String emailIds = ar.reqParam("rolemember");
            NGWebUtils.updateUserContactAndSaveUserPage(ar, "Add", emailIds);

            modelAndView = new ModelAndView(new RedirectView("permission.htm"));
            modelAndView.addObject("page", account);
            request.setAttribute("tabId", "Project Settings");
            request.setAttribute("pageId", accountId);
            request.setAttribute("pageTitle", account.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.add.member.role", new Object[]{accountId}, ex);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/{accountId}/$/permission.htm", method = RequestMethod.GET)
    public ModelAndView showPermissionTab(@PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.open.permission.page",null);
            }
            NGPageIndex.assertBook(accountId);

            NGBook nGBook = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(nGBook);

            modelAndView = new ModelAndView("account_permission");
            request.setAttribute("headerType", "account");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Account Settings");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.Permissions");
            request.setAttribute("accountId", accountId);
            request.setAttribute("title", nGBook.getFullName());
            request.setAttribute("pageTitle", nGBook.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.permission.page", new Object[]{accountId}, ex);
        }
        return modelAndView;
    }


    @RequestMapping(value = "/{accountId}/$/personal.htm", method = RequestMethod.GET)
    public ModelAndView showPersonalTab(@PathVariable String accountId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.open.personal.page",null);
            }
            NGPageIndex.assertBook(accountId);

            NGBook nGBook = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(nGBook);

            modelAndView=new ModelAndView("account_personal");
            request.setAttribute("headerType", "account");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Account Settings");
            request.setAttribute("subTabId", "nugen.projectsettings.subtab.personal");
            request.setAttribute("visibility_value", "4");
            request.setAttribute("accountId", accountId);
            request.setAttribute("title", nGBook.getFullName());
            request.setAttribute("pageTitle", nGBook.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.personal.page", new Object[]{accountId}, ex);
        }
        return modelAndView;
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

    @RequestMapping(value = "/{accountId}/$/getUsers.ajax", method = RequestMethod.GET)
    public void getUsers(HttpServletRequest request, HttpServletResponse response, @PathVariable String accountId) throws Exception {
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                sendRedirectToLogin(ar, "message.loginalert.get.userlist",null);
                return;
            }
            NGPageIndex.assertBook(accountId);
            String matchKey = ar.defParam("matchkey", "");
            String users = UserManager.getUserFullNameList(matchKey);
            users = users.replaceAll("\"", "");
            NGWebUtils.sendResponse(ar, users);
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.get.users", null, ex);
        }
    }

    @RequestMapping(value = "/{accountId}/$/EditRoleBook.htm", method = RequestMethod.GET)
    public ModelAndView editRoleBook(@PathVariable String accountId,
            @RequestParam String roleName, @RequestParam String projectName,
            HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.loginalert.edit.role",null);
            }
            NGPageIndex.assertBook(accountId);
            NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
            ar.setPageAccessLevels(ngb);
            modelAndView=new ModelAndView("editRoleAccount");
            request.setAttribute("headerType", "account");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("tabId", "Account Settings");
            request.setAttribute("accountId", accountId);
            request.setAttribute("roleName", roleName);
            request.setAttribute("projectName", projectName);
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.account.editrolebook",new Object[]{accountId});
        }
        return modelAndView;
    }

    @RequestMapping(value="/{accountId}/$/a/{docId}.{ext}", method = RequestMethod.GET)
    public void loadDocument(
          @PathVariable String accountId,
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

           NGPageIndex.assertBook(accountId);

           String attachmentName = URLDecoder.decode(docId,"UTF-8")+"."+ext;

           NGBook ngb = NGPageIndex.getAccountByKeyOrFail(accountId);
           ar.setPageAccessLevels(ngb);
           String version = ar.reqParam("version");
           AttachmentHelper.serveUpFileNewUI(ar, ngb, attachmentName,Integer.parseInt(version));

       }catch(Exception ex){
           throw new NGException("nugen.operation.fail.account.download.document", new Object[]{docId, accountId}, ex);
       }
   }

}
