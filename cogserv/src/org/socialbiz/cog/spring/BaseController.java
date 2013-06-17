package org.socialbiz.cog.spring;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.NGPage;
import org.socialbiz.cog.NGPageIndex;
import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.exception.ServletExit;

@Controller
public class BaseController {

    public static final String ACCOUNT_ID = "book";
    public static final String TAB_ID = "tabId";
    public static final String PAGE_ID = "pageId";

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex, HttpServletRequest request, HttpServletResponse response) {

        //if a ServletExit has been thrown, then the browser has already been redirected,
        //so just return null and get out of here.
        if (ex instanceof ServletExit) {
            return null;
        }
        AuthRequest ar = AuthRequest.getOrCreate(request, response);
        long exceptionNO=ar.logException("", ex);
        ModelAndView modelAndView = new ModelAndView(Constant.COMMON_ERROR);
        modelAndView.addObject("exceptionNO", Long.toString(exceptionNO));
        return modelAndView;
    }

    public static NGPage getAccountProjectOrFail(String accountId, String projectId) throws Exception
    {
        NGPageIndex.assertBook(accountId);
        NGPage ngp = NGPageIndex.getProjectByKeyOrFail( projectId );
        if (!accountId.equals(ngp.getAccountKey())) {
            throw new NGException("nugen.operation.fail.account.match", new Object[]{projectId,accountId});
        }
        return ngp;
    }


    public static AuthRequest getLoggedInAuthRequest(HttpServletRequest request,
            HttpServletResponse response, String assertLoggedInMsgKey) throws Exception {

        AuthRequest ar = AuthRequest.getOrCreate(request, response);
        ar.assertLoggedIn(ar.getMessageFromPropertyFile(assertLoggedInMsgKey, null));
        return ar;
    }

    public ModelAndView createRedirectView(AuthRequest ar, String redirectAddress) throws Exception {
        return new ModelAndView(new RedirectView(redirectAddress));
    }

    public ModelAndView createNamedView(String accountId, String pageId,
            AuthRequest ar,  String viewName, String tabId)
            throws Exception {
        ar.req.setAttribute(ACCOUNT_ID, accountId);
        ar.req.setAttribute(TAB_ID, tabId);
        ar.req.setAttribute(PAGE_ID, pageId);
        ar.req.setAttribute("realRequestURL", ar.getRequestURL());
        return new ModelAndView(viewName);
    }

    /*
    * Redirect from a fetched page to the login page.  Returns to this page.
    * Should not have two of these.  Clean up and have just one
    */
    public static ModelAndView redirectToLoginView(AuthRequest ar, String msgKey, Object[] param) throws Exception {
        sendRedirectToLogin(ar, msgKey, param);
        return null;
    }

    public static void sendRedirectToLogin(AuthRequest ar, String msgKey, Object[] param) throws Exception {
        String go = ar.getCompleteURL();
        String message = ar.getMessageFromPropertyFile(msgKey, param);
        String loginUrl = ar.baseURL+"t/EmailLoginForm.htm?go="+URLEncoder.encode(go,"UTF-8")
        +"&msg="+URLEncoder.encode(message,"UTF-8");
        ar.resp.sendRedirect(loginUrl);
        return;
    }


    /**
    * Redirect to the login page from a form POST controller when an error occurred.
    * parameter go is the web address to redirect to on successful login
    * parameter error is an exception that represents the message to display to the user
    * Error message is displayed only once.  Refreshing the page will clear the message.
    */
    protected void redirectToLoginPage(AuthRequest ar, String go, Exception error) throws Exception
    {
        //pass the 'last' error message to the login page through the session (not parameter)
        String msgtxt = NGException.getFullMessage(error, ar.getLocale());
        ar.session.setAttribute("error-msg", msgtxt);

        String err1return = ar.retPath+"t/EmailLoginForm.htm?go="+URLEncoder.encode(go, "UTF-8");
        ar.resp.sendRedirect(err1return);
        return;
    }

    /*
    * Pass in the relative URL and
    * this will redirect the browser to that address.
    * It will return a null ModelAndView object so that you can
    * say "return redirectToURL(myurl);"
    */
    protected ModelAndView redirectBrowser(AuthRequest ar, String pageURL) throws Exception
    {
        ar.resp.sendRedirect(pageURL);
        return null;
    }

}


