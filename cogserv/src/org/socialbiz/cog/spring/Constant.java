package org.socialbiz.cog.spring;


/**
 * This is a Constant class for whose sole job is to define widely-used constants.
 * @author Somnath
 */

/**
Collected constants of general utility.
<P>All members of this class are immutable.
*/

public class Constant {

    public static final String FORWARD_REQUEST_URI_ATTRIBUTE = "javax.servlet.forward.request_uri";

    public static final String FORWARD_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.forward.context_path";

    public static final String FORWARD_SERVLET_PATH_ATTRIBUTE = "javax.servlet.forward.servlet_path";

    public static final String FORWARD_PATH_INFO_ATTRIBUTE = "javax.servlet.forward.path_info";

    public static final String FORWARD_QUERY_STRING_ATTRIBUTE = "javax.servlet.forward.query_string";


    public static final String COMMON_ERROR = "commonError";


    // Default maximum days for a record to be included in history list otherwise it would not be displayed in list.
    public static final int HISTORY_MAX_DAYS = 15;

    //Property in config file to store maximum days for a history record
    public static final String HISTORY_MAX_DAYS_PROPERTY = "max_days_interval";

    public static final String MSG_SEPARATOR = "<:>";
    public static final String FAILURE = "failure";
    public static final String SUCCESS = "success";

    public static final String YES = "yes";
    public static final String No = "no";

    public static final String MSG_TYPE = "msgType";
    public static final String MESSAGE = "msg";
    public static final String MSG_DETAIL = "msgDetail";
    public static final String COMMENTS = "comments";

 // PRIVATE //

  /**
   The caller references the constants using <tt>Constant.FORWARD_REQUEST_URI_ATTRIBUTE</tt>,
   and so on. Thus, the caller should be prevented from constructing objects of
   this class, by declaring this private constructor.
  */
  private Constant(){
    //this prevents even the native class from calling this constructor as well :
    throw new AssertionError();
  }

}
