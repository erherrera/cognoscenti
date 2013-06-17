/**
 *
 */
package org.socialbiz.cog.test;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.tidy.Tidy;

public class XHTMLValidator {

    private String outFileName;
    private String errOutFileName;

    /**
     * The class logger.
     */
    private static Log log = LogFactory
            .getLog(org.socialbiz.cog.test.XHTMLValidator.class);

    public static final String errorLog = "./testing_output/errorLog.txt";
    public static final String output = "./testing_output/output.txt";


    public boolean validateHTMLPage(String requestUri,InputStream in) throws Exception {

        // Called by tidy when a warning or error occurs.
        TidyErrorsListener errors = new TidyErrorsListener();
        // create parser
        Tidy tidy = null;
        FileOutputStream out = null;
        try {
            this.errOutFileName = errorLog;
            this.outFileName = output;

            log.debug("Start : validateHTMLPage");

            // Create a new JTidy instance and set options
            tidy = new Tidy();
            tidy.setMessageListener(errors);

            // Set Jtidy warnings on-off
            tidy.setShowWarnings(log.isWarnEnabled());

            // Set Jtidy final result summary on-off
            tidy.setQuiet(!log.isInfoEnabled());

            tidy.setXHTML(true);
            tidy.setDocType("strict");
            tidy.setXmlTags(true);

            tidy.setOnlyErrors(true);
            tidy.setXmlOut(true);
            tidy.setMakeClean(true);

            tidy
                    .setErrout(new PrintWriter(new FileWriter(errOutFileName),
                            true));
            out = new FileOutputStream(outFileName);

            if (log.isDebugEnabled()) {
                log.debug("validate HTML Page : tidy parser created - " + tidy);
            }

        } catch (Exception e) {
            log.error("Unable to instantiate tidy parser", e);
        }


            // Parse an HTML page into a DOM document
            tidy.parseDOM(in, out);

            if (errors.hasErrors()) {
                // If found an error. Logger it with the page to make it easier
                // to trace.
                // TODO This is using a non localized string conversion.
                String message = "There where validation errors for "
                        + requestUri + ":\n" + errors.getErrorMessage() + "\n";

                System.out.println(message);

                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
            }

        log.debug("End : validateHTMLPage");

        return errors.hasErrors();
    }

    public String HTMLOutput(InputStream is) throws IOException {

        // read it with BufferedReader
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        br.close();
        System.out.println(sb.toString());

        return sb.toString();
    }

}
