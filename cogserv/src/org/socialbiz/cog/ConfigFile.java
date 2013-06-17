package org.socialbiz.cog;

import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.exception.ProgramLogicError;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import javax.servlet.ServletContext;

/**
* Holds configuration settings
*/
public class ConfigFile
{
    /**
    * This is the path to the WEB-INF folder where all config information lives
    */
    private static String webInfPath;

    /**
    * the name of the file that the properties are expected to be in
    */
    private static File configFile = null;

    /**
    * This is where the properties are cached internally in memory
    * when the class is properly initialized.
    */
    private static Properties props = null;


    /**
    * Given a name of a configuration file, this will return the
    * File object to access it.
    */
    public static File getFile(String fileName)
    {
        return new File(webInfPath,fileName);
    }


    public static File getWebINFPath()
    {
        return new File(webInfPath);
    }

    public static void initialize(ServletContext sc)
        throws Exception
    {
        try
        {
            webInfPath = sc.getRealPath("WEB-INF");
            configFile = getFile("config.txt");

            if (!configFile.exists())
            {
                //config file must exist.  The server context folder is a given, there
                //are no options about that, and the config file must exist in that folder
                //so if there is no file there, go ahead and create one with reasonable
                //default values
                Properties newConfig = new Properties();
                newConfig.put("dataFolder", sc.getRealPath("data_folder"));
                newConfig.put("attachFolder", sc.getRealPath("attach_folder"));
                saveConfigFile(props);
            }

            initializeFromPath();
            MimeTypes.initialize(webInfPath);
        }
        catch (Exception e)
        {
            throw new NGException("nugen.exception.configfile.initialization.fail",null, e);
        }
    }

    private static void initializeFromPath()
        throws Exception
    {
        FileInputStream fis = new FileInputStream(configFile);
        Properties tprop = new Properties();
        tprop.load(fis);
        fis.close();
        props = tprop;
    }


    /**
    * Check whether this class has been initialized and can say
    * anything about the configuration parameters.
    */
    public static boolean isInitialized()
    {
        return (props!=null);
    }

    /**
    * getConfigProperties will return the configuration properties installed
    * in WEB-INF/config.txt.  Complains if it can not find such a file.
    * This is the static version.
    * Will never return null.
    */
    public static Properties getConfigProperties()
        throws Exception
    {
        if (props==null)
        {
            //this will only happen if the server is not initializing classes in the right order
            //so no reason to translate.
            throw new NGException("nugen.exception.unable.to.read.config.properties",null);
        }
        return props;
    }


    public static String getProperty(String name)
    {
        if (props==null)
        {
            return null;
        }
        return props.getProperty(name);
    }

    /**
    * Returns an array of strings from a configuration settings which is
    * SEMICOLON delimited.  That is, the config value is a list of values
    * separated by semicolons.  The values can not themselves have semicolons
    * in them.  This method will split them and return a proper array.
    */
    public static String[] getArrayProperty(String name)
    {
        if (props==null)
        {
            return null;
        }
        String list = props.getProperty(name);
        if (list==null) {
            return new String[0];
        }
        String[] vals = UtilityMethods.splitOnDelimiter(list, ';');
        return vals;
    }


    /**
    * Set a ConfigFile property.  Pass a null to clear a setting.
    */
    public static void setProperty(String name, String value)
        throws Exception
    {
        if (props==null)
        {
            throw new NGException("nugen.exception.configfile.not.initialized",null);
        }
        if (value==null)
        {
            props.remove(name);
        }
        else
        {
            props.setProperty(name, value);
        }
    }


    public static void save()
        throws Exception
    {
        if (props==null)
        {
            throw new NGException("nugen.exception.configfile.not.initialized",null);
        }
        saveConfigFile(props);
    }



    private static void saveConfigFile(Properties nProp)
        throws Exception
    {
        if (nProp==null)
        {
            throw new ProgramLogicError("Call was made to 'saveConfigFile' with null properties object");
        }
        if (configFile==null)
        {
            throw new ProgramLogicError("Can not save properties when config file path has not been initialized.");
        }
        FileOutputStream fos = new FileOutputStream(configFile);
        nProp.store(fos, "Configured using ConfigFile.saveConfigFile");
        fos.close();
        initializeFromPath();
    }


    /**
    * Checks that the supplied path exists, and that it is a directory.
    * Throws standard errors if it is not.
    */
    public static File getFolderOrFail(String folderPath) throws Exception
    {
        File root = new File(folderPath);
        if (!root.exists())
        {
            throw new NGException("nugen.exception.root.dir.does.not.exist", new Object[]{folderPath});
        }
        if (!root.isDirectory())
        {
            throw new NGException("nugen.exception.root.dir.must.be.folder", new Object[]{folderPath});
        }
        return root;
    }



    /**
    * This test can be used to prevent pages from appearing when the configuration is
    * not complete.  It is a safeguard, and not a configuration utility.
    */
    public static void assertConfigureCorrect() throws Exception
    {
        if (!NGPageIndex.isInitialized())
        {
            throw new NGException("nugen.exception.server.not.initialized",null);
        }
        assertConfigureCorrectInternal();
    }

    public static void assertConfigureCorrectInternal() throws Exception
    {
        Properties props = getConfigProperties();

        //check that configuration is complete
        String dataFolder = props.getProperty("dataFolder");
        if (dataFolder == null)
        {
            throw new NGException("nugen.exception.system.configured.incorrectly", new Object[]{"dataFolder"});
        }
        File testDir = new File(dataFolder);
        if (!testDir.exists() || !testDir.isDirectory())
        {
            throw new NGException("nugen.exception.folder.not.found",new Object[]{"dataFolder",testDir.getAbsolutePath()});
        }

        String attachFolder = props.getProperty("attachFolder");
        if (attachFolder == null)
        {
            throw new NGException("nugen.exception.system.configured.incorrectly", new Object[]{"attachFolder"});
        }
        testDir = new File(attachFolder);
        if (!testDir.exists() || !testDir.isDirectory())
        {
            throw new NGException("nugen.exception.folder.not.found",new Object[]{"attachFolder",testDir.getAbsolutePath()});
        }

        String userFolder = props.getProperty("userFolder");
        if (userFolder == null)
        {
            throw new NGException("nugen.exception.system.configured.incorrectly", new Object[]{"userFolder"});
        }
        testDir = new File(userFolder);
        if (!testDir.exists() || !testDir.isDirectory())
        {
            throw new NGException("nugen.exception.folder.not.found",new Object[]{"userFolder",testDir.getAbsolutePath()});
        }

    }


    /**
    * Returns a user specified globally unique value, or else a random value if
    * one was not set.  The purpose is to provide a unique jey for the server.
    * If the user does not set a value, then the unique key may be different every
    * time the server is started.  However, two servers are highly unlikely to have
    * the same value, and that is the purpose, to distinguish servers.
    *
    * Use can specify the value in the config file setting: ServerId
    */
    static String serverId = null;
    public static synchronized String getServerGlobalId() {
        if (serverId!=null) {
            return serverId;
        }
        serverId = getProperty("ServerId");
        if (serverId!=null && serverId.length()>0) {
            return serverId;
        }
        serverId = IdGenerator.generateKey();
        return serverId;
    }

}
