/**
 *
 */
package org.socialbiz.cog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author banerjso
 *
 *
 */
public class ErrorLogDetails extends DOMFace {

    public ErrorLogDetails(Document doc, Element ele, DOMFace p) {
        super(doc, ele, p);
    }
    
    public void setModified(String userId, long time) {
        setAttribute("modUser", userId);
        setAttribute("modTime", Long.toString(time));
    }
    public long getModTime() {
        return safeConvertLong(getAttribute("modTime"));
    }   
    public String getModUser() {
        return getAttribute("modUser");
    }
    public void setErrorNo(String errorNo) {
        setAttribute("errorNo", errorNo);
    }
    public String getErrorNo() {
        return getAttribute("errorNo");
    }

    public void setFileName(String fileName) {
        setScalar("errorfileName", fileName);
    }
    public void setErrorMessage(String errorMessage) {
        setScalar("errorMessage", errorMessage);
    }
    public void setURI(String URI) {
        setScalar("errorURI", URI);
    }
    public void setErrorDetails(String errorDetails) {
        setScalar("errorDetails", errorDetails);
    }
    
    public void setUserComment(String comments) {
        setScalar("userComments", comments);
    }
    
    public String getErrorDetails() {
        return getScalar("errorDetails");
    }
    public String getErrorMessage() {
        return getScalar("errorMessage");
    }
    
    public String getFileName() {
        return getScalar("errorfileName");
    }
    public String getURI() {
        return getScalar("errorURI");
    }  
    
    public String getUserComment() {
        return getScalar("userComments");
    }
}
