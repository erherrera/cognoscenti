package org.socialbiz.cog;

import java.io.Writer;
import java.util.Vector;
import org.socialbiz.cog.exception.ProgramLogicError;

@Deprecated
public class SectionFolders extends SectionUtil implements SectionFormat {

    public static final int TYPE_FOLDER = 0;
    public static final int TYPE_FILE = 1;
    public static final String PTCL_LOCAL = "Local";
    public static final String PTCL_SMB = "SMB";
    public static final String PTCL_WEBDAV = "WEBDAV";

    public SectionFolders() {

    }

    /**
     * get the name of the format
     */
    public String getName() {
        return "Folders Format";
    }


    public void deleteFolder(AuthRequest ar, NGPage ngp, NGSection section,
            String folderId) throws Exception {

        throw new ProgramLogicError("Method Not implemented");
    }

    public void writePlainText(NGSection section, Writer out) throws Exception {
        //silently ignore this request ... no text to produce.
        //necessary for search function
    }

    /*
     * Walk through whatever elements this owns and put all the four digit IDs
     * into the vector so that we can generate another ID and assure it does not
     * duplication any id found here.
     */
    public void findIDs(Vector<String> v, NGSection sec) throws Exception {

    }

    /**
     * This is a method to find a file, and output the file as a stream of bytes
     * to the request output stream.
     */
    public static void serveUpFile(AuthRequest ar, NGPage ngp, String fileId)
            throws Exception {

        throw new ProgramLogicError("Method Not implemented");

    }

    public boolean isEmpty(NGSection section) throws Exception {
        throw new ProgramLogicError("Method Not implemented");
    }

    public String editButtonName() {
        return "Mount";
    }


    public void displaySubFolder(AuthRequest ar, NGPage ngp, NGSection section,
            String folderId) throws Exception {
        throw new ProgramLogicError("Method Not implemented");
    }

}
