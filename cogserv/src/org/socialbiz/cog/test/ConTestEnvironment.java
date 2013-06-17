package org.socialbiz.cog.test;


import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Static environment of key-value pairs that allows tests to exchange test
 * parameters across component boundaries. 
 */
public class ConTestEnvironment {

    private static Map<String,Object> m_globals = new HashMap<String,Object>();    

    // Prevents instances of this purely static class to be created.
    private ConTestEnvironment() {
    }

    /**
     * Sets a parameter in the global scope.
     * 
     * @param key
     *                The parameter's key.
     * @param value
     *                The parameter's value.
     */
    public static void setGlobal(String key, Object value) {
        m_globals.put(key.toLowerCase(), value);
    }

    /**
     * Attempts to retrieve a parameter from the <em>global</em> scope only.     
     * @param key
     *                The parameter's key.
     * @return The parameter's value, if the parameter exists in the global
     *         scope, otherwise null.    
     */
    public static Object getGlobal(String key) {
        Object value = m_globals.get(key.toLowerCase());
        return value;
    }

    /**
     * Removes a parameter from the global scope, if it exists.
     * 
     * @param key
     *                The parameter's key.    
     */
    public static void removeGlobal(String key) {
        m_globals.remove(key.toLowerCase());
    }



    }