package org.socialbiz.cog;

import java.io.Writer;
import java.util.Vector;

import org.socialbiz.cog.exception.ProgramLogicError;

/**
 * This section format is used whenever a section element is found
 * and the name of the section is not recognized or unknown.
 * Currently this allows the unknown section to persist, but you
 * can not edit it or anything.
 */
public class SectionUnknown extends SectionWiki {

    public SectionUnknown() {

    }

    public String getName() {
        return "(Unknown Format)";
    }

    public void findLinks(Vector<String> v, NGSection section) throws Exception
    {
        //no links to find
    }


    public void writePlainText(NGSection section, Writer out) throws Exception
    {
        // nothing to write
    }

    /**
     * assume that there is something
     */
    public boolean isEmpty(NGSection section) throws Exception
    {
        return true;
    }


    public NoteRecord convertToLeaflet(NGSection noteSection,
                   NGSection wikiSection) throws Exception
    {
        throw new ProgramLogicError("Method convertToLeaflet not implemented for Unknown Format");
    }

}
