package org.socialbiz.cog.spring;


public class LoginHelper{

    /*
    public DiscoveryInformation login(HttpServletRequest request, HttpServletResponse response) throws Exception{

        DiscoveryInformation discovered = null;
        request.setCharacterEncoding("UTF-8");
        HttpSession session =request.getSession();
        String uopenid = request.getParameter("openid");
        String go = request.getParameter("go");

        if (uopenid==null)
        {
            throw new ProgramLogicError("need openid parameter on login form");
        }

        uopenid = uopenid.trim();

        //if this looks like a email address, then redirect to the page that handles email
        //and password style logins
        if (uopenid.indexOf('@')>=0)
        {
            String password = request.getParameter("password");
            if (password==null)
            {
                password = "";
            }
            String handleEmailLogin = "EmailLoginAction.form?email="+URLEncoder.encode(uopenid, "UTF-8")
                +"&option=Login&go="+URLEncoder.encode(go, "UTF-8")
                +"&password="+URLEncoder.encode(password, "UTF-8");
                response.sendRedirect(handleEmailLogin);
            return discovered;
        }


        //open id login

        Object o = request.getAttribute("consumermanager");
        if (o == null)
        {
            ConsumerManager newmgr=new ConsumerManager();
            newmgr.setAssociations(new InMemoryConsumerAssociationStore());
            newmgr.setNonceVerifier(new InMemoryNonceVerifier(5000));
            request.setAttribute("consumermanager", newmgr);
        }
        ConsumerManager manager = (ConsumerManager) request.getAttribute("consumermanager");

        // I don't like this hack, but I don't see any other way.  We need to redirect the user
        // back to the page they were on when they were logging in.  If we pass this as a parameter
        // in the returnToUrl, then they have to log into each page, since the OpenID site
        // asks for a verification for each return-to URL.
        // Solution here is store in the "session" the page that you were logging in to, and
        // retrieve that from the session after authentication.  This is a problem only if
        // one person logging in to two pages at once -- seems relatively unlikely and safe.
        if (go == null)
        {
            throw new ProgramLogicError("login helper class requires a 'go' parameter specifying the page that the user has attempted to access before requesting to login, so that after loging in we can redirect back to there.");
        }
        session.setAttribute("login-page", go);

        // there may be more than one profile with the key (if none are confirmed yet,
        // or if one was added after the other was confirmed.  To be sure to update the
        // and confirm the right user profile, the key is stored here for use later.
        String upKey = request.getParameter("key");
        if (upKey!=null)
        {
            session.setAttribute("user-profile-key", upKey);
        }

        // perform discovery on the user-supplied identifier


        List<?> discoveries = manager.discover(uopenid);

        // attempt to associate with an OpenID provider
        // and retrieve one service endpoint for authentication
        discovered = manager.associate(discoveries);

        // store the discovery information in the user's session
        session.setAttribute("openid-disco", discovered);

        return discovered;
    }
    */


}
