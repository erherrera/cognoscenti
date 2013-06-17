package org.socialbiz.cog.test;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

/**
 * @author Somnath Banerjee
 *
 */
public class BaseTest extends TestCase {

    /**
     * The application context.
     */
    protected static ApplicationContext ctx = null;

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (ctx == null) {
            ctx = TestApplicationContext.getContext();
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor.
     */
    public BaseTest() {
        // Empty constructor for now
    }

}
