package org.socialbiz.cog.spring;

import java.util.ArrayList;
import java.util.List;

import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessageListener;



public class JTidyListener implements TidyMessageListener {

    static int count = 0;
    private List<XHTMLError> errorsNWarnings = new ArrayList<XHTMLError>();

    public void messageReceived(TidyMessage msg) {
        count++;
        XHTMLError error = new XHTMLError();
        error.setColumn(msg.getColumn());
        // dom is stripping the DOCTYPE line so in order to correct the line
        // no., subtract 1
        error.setLine(msg.getLine() - 1);
        error.setErrorMessage(msg.getMessage());
        String errorType = msg.getLevel().toString();
        error.setErrorType(errorType);
        errorsNWarnings.add(error);
    }

    public List<XHTMLError> getXHTMLErrors() {
        // dom is stripping the DOCTYPE line remove the first error
        if(errorsNWarnings.size()<2){
            return errorsNWarnings;
        }
       return errorsNWarnings.subList(1, errorsNWarnings.size());
    }

    public int getAllErrorCount() {
        return count;
    }

}
