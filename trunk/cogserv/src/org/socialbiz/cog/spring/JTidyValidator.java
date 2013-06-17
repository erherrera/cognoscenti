package org.socialbiz.cog.spring;

import java.io.StringReader;
import java.io.Writer;
import java.util.List;

import org.w3c.tidy.Tidy;



public class JTidyValidator {

    private static final Tidy tidy = new Tidy();
    private static Writer w =null;

    static {
        tidy.setQuiet(true);
        tidy.setShowWarnings(true);
        tidy.setOnlyErrors(true);
        tidy.setXHTML(true);
        // tidy.setXmlTags(true);
        tidy.setXmlOut(true);
        //tidy.setForceOutput(true);
    }


    public List<XHTMLError> getXHTMLErrors(String dom, String errorFileName)
            throws Exception {
        List<XHTMLError> errors = getXHTMLErrors(dom);
        return errors;
    }

    /*
    private void writeErrors(List<XHTMLError> errors, String errorFileName) throws IOException {

        for (XHTMLError error : errors) {
            w.write("Line : ");

            w.write(error.getLine());
            w.write(" Column : ");
            w.write(error.getColumn());
            w.write(" - ");
            w.write(error.getErrorMessage());
            w.write("\n");
        }
        w.write("\n\n\t Total Error on this Page = " + errors.size());
    }
    */

    public List<XHTMLError> getXHTMLErrors(String dom) throws Exception {
        // tidy.setErrout(new PrintWriter(new FileWriter("error.txt"), true));
        JTidyListener jTidyListener = new JTidyListener();
        tidy.setMessageListener(jTidyListener);
        StringReader reader = new StringReader(dom);
        tidy.parseDOM(reader, w);
        return jTidyListener.getXHTMLErrors();
    }

    public List<XHTMLError> validate(String dom, Writer out) throws Exception {
        w = out;
        return getXHTMLErrors(dom,"test.output");

    }

}
