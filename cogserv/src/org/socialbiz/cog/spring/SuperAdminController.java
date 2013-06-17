package org.socialbiz.cog.spring;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;

import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.exception.ProgramLogicError;
import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.DOMUtils;
import org.socialbiz.cog.ErrorLog;
import org.socialbiz.cog.UserManager;
import org.socialbiz.cog.UserProfile;
import org.socialbiz.cog.spring.NGWebUtils;

@Controller
public class SuperAdminController extends BaseController {

     @Autowired
     public void setContext(ApplicationContext context) {
         NGWebUtils.srvContext = context;
     }

     @RequestMapping(value = "/{userKey}/errorLog.htm", method = RequestMethod.GET)
     public ModelAndView errorLogPage(@PathVariable String userKey,HttpServletRequest request,
             HttpServletResponse response)
     throws Exception {

         ModelAndView modelAndView = null;
         AuthRequest ar = null;
         UserProfile up = null;
         try{
             ar = AuthRequest.getOrCreate(request, response);
             if(!ar.isLoggedIn()){
                 return redirectToLoginView(ar, "message.loginalert.errors.log",null);
             }
             if(!ar.isSuperAdmin()){
                 throw new NGException("nugen.exceptionhandling.system.admin.rights",null);
             }
             up = UserManager.getUserProfileOrFail(userKey);
             request.setAttribute("title", up.getName());
             modelAndView = createModelAndView(ar, up, null, "Administration", "errorLog");
             request.setAttribute("subTabId", "nugen.admin.subtab.errors.log");

         }catch(Exception ex){
             throw new NGException("nugen.operation.fail.administration.page", new Object[]{userKey} , ex);
         }
         return modelAndView;
     }

     @RequestMapping(value = "/{userKey}/emailListnerSettings.htm", method = RequestMethod.GET)
     public ModelAndView emailListnerSettings(@PathVariable String userKey,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {


         ModelAndView modelAndView = null;
         AuthRequest ar = null;
         UserProfile up = null;
         try{
             ar = AuthRequest.getOrCreate(request, response);
             if(!ar.isLoggedIn()){
                 return redirectToLoginView(ar, "message.loginalert.email.listener.setting",null);
             }
             if(!ar.isSuperAdmin()){
                 throw new NGException("nugen.exceptionhandling.system.admin.rights",null);
             }
             up = UserManager.getUserProfileOrFail(userKey);
             request.setAttribute("title", up.getName());
             modelAndView = createModelAndView(ar, up, null, "Administration", "emailListnerSettings");
             request.setAttribute("subTabId", "nugen.admin.subtab.email");

         }catch(Exception ex){
             throw new NGException("nugen.operation.fail.administration.page", new Object[]{userKey} , ex);
         }
         return modelAndView;

     }

     @RequestMapping(value = "/{userKey}/lastNotificationSend.htm", method = RequestMethod.GET)
     public ModelAndView lastNotificationSend(@PathVariable String userKey,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {


         ModelAndView modelAndView = null;
         AuthRequest ar = null;
         UserProfile up = null;
         try{
             ar = AuthRequest.getOrCreate(request, response);
             if(!ar.isLoggedIn()){
                 return redirectToLoginView(ar, "message.loginalert.last.notification.send",null);
             }
             if(!ar.isSuperAdmin()){
                 throw new NGException("nugen.exceptionhandling.system.admin.rights",null);
             }
             up = UserManager.getUserProfileOrFail(userKey);
             request.setAttribute("title", up.getName());
             modelAndView = createModelAndView(ar, up, null, "Administration", "lastNotificationSend");
             request.setAttribute("subTabId", "nugen.admin.subtab.last.notification.send");

         }catch(Exception ex){
             throw new NGException("nugen.operation.fail.administration.page", new Object[]{userKey} , ex);
         }
         return modelAndView;

     }

     @RequestMapping(value = "/{userKey}/newAccounts.htm", method = RequestMethod.GET)
     public ModelAndView newAccounts(@PathVariable String userKey,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {


         ModelAndView modelAndView = null;
         AuthRequest ar = null;
         UserProfile up = null;
         try{
             ar = AuthRequest.getOrCreate(request, response);
             if(!ar.isLoggedIn()){
                 return redirectToLoginView(ar, "message.loginalert.new.accounts",null);
             }
             if(!ar.isSuperAdmin()){
                 throw new NGException("nugen.exceptionhandling.system.admin.rights",null);
             }
             up = UserManager.getUserProfileOrFail(userKey);
             request.setAttribute("title", up.getName());
             modelAndView = createModelAndView(ar, up, null, "Administration", "newAccounts");
             request.setAttribute("subTabId", "nugen.admin.subtab.new.accounts");

         }catch(Exception ex){
             throw new NGException("nugen.operation.fail.administration.page", new Object[]{userKey} , ex);
         }
         return modelAndView;

     }

     @RequestMapping(value = "/{userKey}/deniedAccounts.htm", method = RequestMethod.GET)
     public ModelAndView deniedAccounts(@PathVariable String userKey,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {


         ModelAndView modelAndView = null;
         AuthRequest ar = null;
         UserProfile up = null;
         try{
             ar = AuthRequest.getOrCreate(request, response);
             if(!ar.isLoggedIn()){
                 return redirectToLoginView(ar, "message.loginalert.denied.accounts",null);
             }
             if(!ar.isSuperAdmin()){
                 throw new NGException("nugen.exceptionhandling.system.admin.rights",null);
             }
             up = UserManager.getUserProfileOrFail(userKey);
             request.setAttribute("title", up.getName());
             modelAndView = createModelAndView(ar, up, null, "Administration", "deniedAccounts");
             request.setAttribute("subTabId", "nugen.admin.subtab.denied.accounts");

         }catch(Exception ex){
             throw new NGException("nugen.operation.fail.administration.page", new Object[]{userKey} , ex);
         }
         return modelAndView;

     }

     @RequestMapping(value = "/{userKey}/getErrorLogXML.ajax", method = RequestMethod.GET)
     public void errorLogXMLData(@PathVariable String userKey,@RequestParam String searchByDate,HttpServletRequest request,
             HttpServletResponse response)
     throws Exception {
         AuthRequest ar = NGWebUtils.getAuthRequest(request, response, "User must be logged in as a Super admin to see the error Log.");
         Document doc=ErrorLog.getErrorLogByDate(searchByDate);
         writeXMLToResponse(ar,doc);
     }

     @RequestMapping(value = "/{userKey}/errorDetails{errorId}.htm", method = RequestMethod.GET)
     public ModelAndView errorDetailsPage(@PathVariable String errorId, @RequestParam String searchByDate,HttpServletRequest request,
             HttpServletResponse response)
     throws Exception {

         ModelAndView modelAndView = null;
         try{
             AuthRequest ar=NGWebUtils.getAuthRequest(request, response, "User must be logged in as a Super admin to see the error Log.");
             HashMap<String, String> searchResult=ErrorLog.displayErrorDetailsByErrorID(errorId ,searchByDate);
             modelAndView = new ModelAndView("ErrorDetailLog");
             modelAndView.addObject("searchResult", searchResult);
             modelAndView.addObject("goURL", ar.getCompleteURL());
         }catch(Exception ex){
             throw new NGException("nugen.operation.fail.error.detail.page", null , ex);
         }
         return modelAndView;
     }

     @RequestMapping(value = "/{userKey}/logUserComents.form", method = RequestMethod.POST)
     public ModelAndView logUserComents(@RequestParam String errorNo,HttpServletRequest request,
             HttpServletResponse response)
     throws Exception {

         try{
             AuthRequest ar =NGWebUtils.getAuthRequest(request, response, "User must be logged in as a Super admin to see the error Log.");
             String userComments=ar.defParam("comments", "");

             String searchByDate=ar.reqParam("searchByDate");
             String goURL=ar.reqParam("goURL");

             ErrorLog.logUserComments(errorNo ,searchByDate,userComments);
             return redirectBrowser(ar,goURL);
         }catch(Exception ex){
             throw new NGException("nugen.operation.fail.error.log.user.comment", null , ex);
         }
     }
     private void writeXMLToResponse(AuthRequest ar, Document doc) throws Exception {

         if (ar == null){
             throw new ProgramLogicError("writeXMLToResponse requires a non-null AuthRequest parameter");
         }
         ar.resp.setContentType("text/xml;charset=UTF-8");
         DOMUtils.writeDom(doc, ar.w);
         ar.flush();
     }

     public static ModelAndView createModelAndView(AuthRequest ar,
             UserProfile up, ModelAndView modelAndView, String tabId,
             String modelAndViewName) {

         HttpServletRequest request = ar.req;
         modelAndView = new ModelAndView(modelAndViewName);

         String realRequestURL = request.getRequestURL().toString();
         request.setAttribute("realRequestURL", realRequestURL);

         request.setAttribute("userKey", up.getKey());
         request.setAttribute("userProfile", up);
         request.setAttribute("pageTitle", "User: " + up.getName());
         request.setAttribute("tabId", tabId);
         return modelAndView;
     }

}
