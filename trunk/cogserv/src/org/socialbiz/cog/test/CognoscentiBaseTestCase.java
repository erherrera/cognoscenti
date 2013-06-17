package org.socialbiz.cog.test;

import java.util.Properties;

import junit.framework.TestCase;

import com.gargoylesoftware.htmlunit.WebClient;

public class CognoscentiBaseTestCase extends TestCase {

    protected static String username = null;

    protected static String password = null;

    public static final String ADMINUSERNAME = "adminuser";

    public static final String ADMINPASSWORD = "adminpassword";

    protected static Properties loginCredentials = null;

    public static final String GBL_LOGIN_CREDENTIALS = "logincredentials"; // maintained


    private WebClient browser;

    protected void setUp() throws Exception {
        super.setUp();
        browser = new WebClient();
    }

    protected void tearDown() {
        browser = null;
    }
    /**
     * @return
     */
    public WebClient getWebClient() {
        return browser;
    }

    /**
     * @param client
     */
    public void setWebClient(WebClient client) {
        browser = client;
    }


    public void login() throws Exception {


        username = ConTestEnvironment.getGlobal(ADMINUSERNAME).toString();
        password = ConTestEnvironment.getGlobal(ADMINPASSWORD).toString();

        if (null == (loginCredentials = (Properties) ConTestEnvironment .getGlobal(GBL_LOGIN_CREDENTIALS))) {
            loginCredentials = new Properties();
            loginCredentials.put(username, password);
            ConTestEnvironment.setGlobal(GBL_LOGIN_CREDENTIALS, loginCredentials);
        } else {
            loginCredentials.put(username, password);
        }


    }


}
