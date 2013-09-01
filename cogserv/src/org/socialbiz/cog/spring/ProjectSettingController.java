/**
 *
 */
package org.socialbiz.cog.spring;

import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socialbiz.cog.AddressListEntry;
import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.HistoryRecord;
import org.socialbiz.cog.NGBook;
import org.socialbiz.cog.NGContainer;
import org.socialbiz.cog.NGPage;
import org.socialbiz.cog.NGPageIndex;
import org.socialbiz.cog.NGRole;
import org.socialbiz.cog.RoleRequestRecord;
import org.socialbiz.cog.UtilityMethods;
import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.exception.ProgramLogicError;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author banerjso
 *
 */
@Controller
public class ProjectSettingController extends BaseController {

    @RequestMapping(value = "/{book}/{pageId}/EditRole.htm", method = RequestMethod.GET)
    public ModelAndView editRole(@PathVariable String book,@PathVariable String pageId,
            @RequestParam String roleName,
            HttpServletRequest request,
            HttpServletResponse response)
    throws Exception {

        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.can.not.edit.role",null);
            }
            NGPageIndex.assertBook(book);

            NGContainer nGPage  = NGPageIndex.getContainerByKeyOrFail(pageId);

            List<NGRole> roles = nGPage.getAllRoles();

            modelAndView = new ModelAndView("editrole");
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("book", book);
            request.setAttribute("tabId", "Project Settings");
            request.setAttribute("pageId", pageId);
            request.setAttribute("roleName", roleName);
            request.setAttribute("roles", roles);
            request.setAttribute("title", " : " + nGPage.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.edit.role.page", new Object[]{pageId,book} , ex);
        }
        return modelAndView;

    }
    @RequestMapping(value = "/{book}/{pageId}/roleRequest.htm", method = RequestMethod.GET)
    public ModelAndView remindersTab(@PathVariable String book,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);

            NGPageIndex.assertBook(book);

            NGPage nGPage = NGPageIndex.getProjectByKeyOrFail(pageId);
            NGBook nGBook = nGPage.getAccount();
            ar.setPageAccessLevels(nGPage);
            if(!ar.isLoggedIn()){
                request.setAttribute("property_msg_key", "nugen.project.role.request.login.msg");
                modelAndView=new ModelAndView("Warning");
            }else if(!ar.isMember()){
                request.setAttribute("property_msg_key", "nugen.projecthome.rolerequest.memberlogin");
                modelAndView=new ModelAndView("Warning");
            }else{
                modelAndView=new ModelAndView("roleRequest");
                modelAndView.addObject("page", nGPage);
                request.setAttribute("subTabId", "nugen.projectsettings.subtab.role.request");
            }
            request.setAttribute("realRequestURL", ar.getRequestURL());
            request.setAttribute("book", book);
            request.setAttribute("tabId", "Project Settings");
            request.setAttribute("pageId", pageId);
            request.setAttribute("title", nGPage.getFullName());
        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.role.request.page", new Object[]{pageId,book} , ex);
        }
        return modelAndView;

    }

    @RequestMapping(value = "/{book}/{pageId}/pageRoleAction.form", method = RequestMethod.POST)
    public ModelAndView pageRoleAction(@PathVariable String book,@PathVariable String pageId,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = null;
        try{
            AuthRequest ar = AuthRequest.getOrCreate(request, response);
            if(!ar.isLoggedIn()){
                return redirectToLoginView(ar, "message.must.be.login.to.perform.action",null);
            }
            NGPageIndex.assertBook(book);

            String r  = ar.reqParam("r");   //role name
            boolean sendEmail  = ar.defParam("sendEmail", null)!=null;
            String op = ar.reqParam("op");  //operation: add or remove
            String go = ar.reqParam("go");  //where to go afterwards

            NGContainer ngp = NGPageIndex.getContainerByKeyOrFail(pageId);
            ar.setPageAccessLevels(ngp);
            ar.assertMember("Unable to modify roles.");
            ar.assertNotFrozen(ngp);

            NGRole role = ngp.getRole(r);
            if (role==null)
            {
                if (op.equals("Create Role"))
                {
                    String desc = ar.reqParam("desc");
                    ngp.createRole(r,desc);
                    ngp.saveContent(ar, "create new role "+r);
                    response.sendRedirect(go);
                    return null;
                }
                throw new NGException("nugen.exception.role.not.found", new Object[]{r,ngp.getFullName()});
            }

            boolean isPlayer = role.isExpandedPlayer(ar.getUserProfile(), ngp);
            if (!isPlayer)
            {
                ar.assertAuthor("You must be a page administrator to change role '"+r+"' when you are not a player of the role.");
            }

            String id = ar.reqParam("id");  //user being added/removed

            AddressListEntry ale =null;
            Vector<AddressListEntry> emailList=null;
            if(op.equals("Add Member"))
            {
                emailList = AddressListEntry.parseEmailList(id);
            }else{
                String parseId = pasreFullname(id );
                ale = AddressListEntry.newEntryFromStorage(parseId);
            }

            int eventType = 0;
            String pageSaveComment = null;

            if (op.equals("Add"))
            {
                if (id.length()<5)
                {
                    throw new NGException("nugen.exception.id.too.small", new Object[]{id});
                }
                eventType = HistoryRecord.EVENT_PLAYER_ADDED;
                pageSaveComment = "added user "+id+" to role "+r;
                //role.addPlayer(ale);
                role.addPlayerIfNotPresent(ale);
            }
            else if (op.equals("Remove"))
            {
                eventType = HistoryRecord.EVENT_PLAYER_REMOVED;
                pageSaveComment = "removed user "+id+" from role "+r;
                role.removePlayer(ale);
            }
            else if (op.equals("Add Role"))
            {
                eventType = HistoryRecord.EVENT_ROLE_ADDED;
                pageSaveComment = "added new role "+r;
                ale.setRoleRef(true);
                role.addPlayer(ale);
            }
            else if (op.equals("Update Details"))
            {
                eventType = HistoryRecord.EVENT_ROLE_MODIFIED;
                pageSaveComment = "modified details of role "+r;
                String desc = ar.defParam("desc", "");
                String reqs = ar.defParam("reqs", "");
                role.setDescription(desc);
                role.setRequirements(reqs);
            }
            else if(op.equals("Add Member"))
            {
                for (AddressListEntry addressListEntry : emailList) {
                    eventType = HistoryRecord.EVENT_PLAYER_ADDED;
                    pageSaveComment = "added user "+addressListEntry.getUniversalId()+" to role "+r;

                    RoleRequestRecord roleRequestRecord = ngp.getRoleRequestRecord(role.getName(),addressListEntry.getUniversalId());
                    if(roleRequestRecord != null){
                        roleRequestRecord.setState("Approved");
                    }

                    role.addPlayerIfNotPresent(addressListEntry);
                    if (sendEmail) {
                        NGWebUtils.sendInviteEmail( ar, pageId,  addressListEntry.getEmail(), r );
                    }
                }
            }
            else
            {
                throw new NGException("nugen.exceptionhandling.did.not.understand.option", new Object[]{op});
            }

            //make sure that the options above set the variables with the right values.
            if (eventType == 0 || pageSaveComment == null)
            {
                throw new ProgramLogicError("variables eventType and pageSaveComment have not been maintained properly.");
            }

            HistoryRecord.createHistoryRecord(ngp,id, HistoryRecord.CONTEXT_TYPE_ROLE,0,eventType, ar, "");
            ngp.saveContent(ar, "added user "+id+" to role "+r);

           if(go!=null){
               response.sendRedirect(go);
           }else{
               modelAndView = new ModelAndView(new RedirectView("EditRole.htm"));
               // modelAndView.addObject works in case of redirect. It adds the parameter in query string.
               modelAndView.addObject("roleName",r);
           }

        }catch(Exception ex){
            throw new NGException("nugen.operation.fail.project.update.role.or.member", new Object[]{pageId,book} , ex);
        }
        return modelAndView;
    }

    private static String pasreFullname(String fullNames) throws Exception
    {
        String assigness = "";
        String[] fullnames = UtilityMethods.splitOnDelimiter(fullNames, ',');
        for(int i=0; i<fullnames.length; i++){
            String fname = fullnames[i];
            int bindx = fname.indexOf('<');
            int length = fname.length();
            if(bindx > 0){
                fname = fname.substring(bindx+1,length-1);
            }
            assigness = assigness + "," + fname;

        }
        if(assigness.startsWith(",")){
            assigness = assigness.substring(1);
        }
        return assigness;
    }

}
