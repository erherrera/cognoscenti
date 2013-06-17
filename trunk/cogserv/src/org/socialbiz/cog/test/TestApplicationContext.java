package org.socialbiz.cog.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;


/**
 *
 */
public class TestApplicationContext {

    private static ApplicationContext context = getInstance();

    /**
     * Empty contstructor that exists only to defeat instantiation.
     */
    private TestApplicationContext() {
        //Exists only to defeat instantiation.
    }

    public synchronized static ApplicationContext getContext() {
        return TestApplicationContext.context;
    }

    private synchronized static ApplicationContext getInstance() {
        String[] paths = {"webapp/WEB-INF/nugen-service.xml"};
        return new FileSystemXmlApplicationContext(paths);
    }
}
