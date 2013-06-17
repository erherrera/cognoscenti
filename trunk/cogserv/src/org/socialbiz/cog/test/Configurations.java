
package org.socialbiz.cog.test;

import java.util.Properties;


public final class Configurations {

    private static Properties prop = new Properties();

    public static String getStringProperty(String key, String defaultValue) {
        if (prop == null || prop.getProperty(key) == null) {
            return defaultValue;
        }
        return prop.getProperty(key);
    }

    public static int getIntProperty(String key, int defaultValue) {
        if (prop == null || prop.getProperty(key) == null) {
            return defaultValue;
        }
        return Integer.parseInt(prop.getProperty(key));
    }

    public static short getShortProperty(String key, short defaultValue) {
        if (prop == null || prop.getProperty(key) == null) {
            return defaultValue;
        }
        return Short.parseShort(prop.getProperty(key));
    }

    public static long getLongProperty(String key, long defaultValue) {
        if (prop == null || prop.getProperty(key) == null) {
            return defaultValue;
        }
        return Long.parseLong(prop.getProperty(key));
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        if (prop == null || prop.getProperty(key) == null) {
            return defaultValue;
        }
        return prop.getProperty(key).toLowerCase().trim().equals("true");
    }

    static {
        try {
            prop.load(Configurations.class.getClassLoader()
                    .getResourceAsStream("crawal.properties"));
        } catch (Exception e) {
            prop = null;
            System.err.println("WARNING: Could not find test.config file in class path.Will use the default values.");
        }
    }
}
