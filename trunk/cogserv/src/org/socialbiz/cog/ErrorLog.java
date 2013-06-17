/**
 *
 */
package org.socialbiz.cog;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.w3c.dom.Document;

import org.socialbiz.cog.exception.NGException;

/**
 * @author banerjso
 *
 *
 */
public class ErrorLog extends DOMFile {

    public ErrorLog(File path, Document newDoc) throws Exception {
        super(path, newDoc);
    }

    private static  File loadedErrorFile = null;
    private static  ErrorLog errorLog=null;

    private static ErrorLog manageErrorLogFile(String nowTimeString) throws Exception {

        // create a log file name based on the current time.
        String fileName = "errorLog_"+ nowTimeString.substring(0,10)+".xml";
        String userFolder = ConfigFile.getProperty("userFolder");
        File newPlace = new File(userFolder, fileName);

        //check to see if the file is there
        if (!newPlace.exists())  {
            //it might be in the OLD config directory.
            File oldPlace = ConfigFile.getFile(fileName);
            //if so, move it to the proper place
            if (oldPlace.exists()) {
                DOMFile.moveFile(oldPlace, newPlace);
            }
        }

        if(errorLog==null || loadedErrorFile==null || !loadedErrorFile.equals(newPlace)){

            Document errorLogDoc = readOrCreateFile(newPlace, "errorlog");
            errorLog=new ErrorLog(newPlace, errorLogDoc);
            loadedErrorFile = newPlace;
        }
        return errorLog;
    }

    private long logsError(UserProfile up,String msg,Throwable ex, String errorURL,long nowTime) throws Exception {

        String userName="GUEST";

        if (up!=null) {
            userName = up.getName()+"("+up.getKey()+")";
        }
        StackTraceElement[] element =ex.getStackTrace()  ;

        ErrorLogDetails errorLogDetails = (ErrorLogDetails) createChild("error", ErrorLogDetails.class);
        long exceptionNO = SuperAdminLogFile.getNextExceptionNo();
        SuperAdminLogFile.setLastExceptionNo(exceptionNO);
        errorLogDetails.setErrorNo(String.valueOf(exceptionNO));

        errorLogDetails.setModified(userName, nowTime);

        errorLogDetails.setFileName(element[0].getFileName());
        errorLogDetails.setURI(errorURL);

        if (msg!=null && msg.length()>0) {
            errorLogDetails.setErrorMessage(msg+"\n"+NGException.getFullMessage(ex, Locale.getDefault()));
        } else {
            errorLogDetails.setErrorMessage(NGException.getFullMessage(ex, Locale.getDefault()));
        }
        errorLogDetails.setErrorDetails(getStackMessage(ex));

        save();
        return exceptionNO;
    }

    private static File getAbsolutePath(String searchByDate) throws Exception {

        File xmlFolder = ConfigFile.getWebINFPath();
        File readXMLFile=null;
        String path=xmlFolder.getPath() + "/errorLog_"+searchByDate+".xml";

        readXMLFile = new File(path);
        return readXMLFile;
    }

    public static Document getErrorLogByDate(String searchByDate) throws Exception
    {
        try {
            Date date = new SimpleDateFormat("MM/dd/yyyy").parse(searchByDate);
            searchByDate=new SimpleDateFormat("yyyy.MM.dd").format(date);
            File xmlFile=getAbsolutePath(searchByDate);

            return readOrCreateFile(xmlFile, "errorlog");
        } catch (Exception e) {
            throw new NGException("nugen.exception.unable.to.load.errorfile",
                    new Object[]{searchByDate}, e);
        }
    }

    public static HashMap<String, String> displayErrorDetailsByErrorID(String errorId,String searchByDate) throws Exception {

        Date searchDate = new Date(Long.valueOf(searchByDate));
        String newSearchDate = new SimpleDateFormat("yyyy.MM.dd").format(searchDate);

        ErrorLog errorLog = manageErrorLogFile(newSearchDate);

        Iterator<ErrorLogDetails> errorLogIterator = errorLog.getChildren("error", ErrorLogDetails.class).iterator();
        HashMap<String, String> detailsError=new HashMap<String, String>();
        if (errorLogIterator != null) {
            while (errorLogIterator.hasNext()) {
                ErrorLogDetails ErrorLogDetails = errorLogIterator.next();

                if(ErrorLogDetails.getErrorNo().equals(errorId)){
                    detailsError.put("ErrorDescription", ErrorLogDetails.getErrorDetails());
                    detailsError.put("ErrorMessage", ErrorLogDetails.getErrorMessage());

                    Date newDate = new Date(ErrorLogDetails.getModTime());
                    detailsError.put("Date&Time", new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS").format(newDate));

                    detailsError.put("URL", ErrorLogDetails.getURI());
                    detailsError.put("errorNo", ErrorLogDetails.getErrorNo());
                    detailsError.put("userDetails", ErrorLogDetails.getModUser());
                    detailsError.put("userComments", ErrorLogDetails.getUserComment());
                }
            }
        }
        return detailsError;
    }

    public static void logUserComments(String errorId,String searchByDate,String comments) throws Exception {

        Date searchDate = new Date(Long.valueOf(searchByDate));
        String newSearchDate = new SimpleDateFormat("yyyy.MM.dd").format(searchDate);

        ErrorLog errorLog = manageErrorLogFile(newSearchDate);
        Iterator<ErrorLogDetails> errorLogIterator = errorLog.getChildren("error", ErrorLogDetails.class).iterator();

        if (errorLogIterator != null) {
            while (errorLogIterator.hasNext()) {
                ErrorLogDetails errorLogDetails = errorLogIterator.next();

                if(errorLogDetails.getErrorNo().equals(errorId)){
                    errorLogDetails.setUserComment(comments);
                    errorLog.save();
                }
            }
        }

    }

    private static String getStackMessage(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.print(" [ ");
        exception.printStackTrace(pw);
        pw.print(" ] ");
        return sw.toString();
    }


    public static synchronized long logException(String msg, Throwable ex, String nowTimeString, long nowTime,
            UserProfile userProfile, String errorURL)
    {
       long exceptionNO=0;
        try
        {
            ErrorLog errorLog=ErrorLog.manageErrorLogFile(nowTimeString);
            if(errorLog!=null)
            {
                exceptionNO=errorLog.logsError(userProfile,msg,ex,errorURL,nowTime);
            }
        }
        catch (Exception e)
        {
            //what else to do? ... crash the server.  If your log file
            //is not working there is very little else to be done.
            //Might as well try throwing the exception...
            throw new RuntimeException("Can not write other exception to log file", e);
        }

        return exceptionNO;
    }



}

