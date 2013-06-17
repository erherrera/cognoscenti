package org.socialbiz.cog;

import org.socialbiz.cog.exception.ProgramLogicError;
import java.io.Writer;
import java.util.Vector;

/**
* Implements the Bidirectional Link formatting
*
* This format is very simple: it is a block of text where every line
* is a link to another page.  Each line contains the name of a page
* the HTML generated is a link to that page.
*
* Currently the editor is a manual editor, but a more sophisticated
* editor will allow for list style editing: a list of links, ADD, and
* DELETE buttons.
*
* FUTURE:
* This should define bi-directional links: a link from page 1 to page 2
* causes a reverse link from page 2 to page 1.  This will require the
* pages to be read, and an index of reverse links to be formed across
* all pages, so that page 2 can find out about all the pages pointing to
* it.  The section pointing one direction, and there will be an opposite
* section pointing back.  For example: a company page may have a
* "products" section, and the pointed to product page will then get a
* "product of" section pointing back.  The section def will include the
* name of the forward link section name, and the backward link section name
*/
public class SectionLink extends SectionWiki
{

    public SectionLink()
    {

    }

    public String getName()
    {
        return "Link Format";
    }


    public void findLinks(Vector<String> v, NGSection section)
        throws Exception
    {
        String tv = section.asText().trim();
        int pos = 0;

        int returnPos = tv.indexOf("\n");
        while (returnPos>=pos) {
            String thisLine = tv.substring(pos, returnPos).trim();
            if (thisLine.length()>0)
            {
                v.add(thisLine);
            }

            pos = returnPos+1;
            //strip the line feed if there is one.
            if (tv.charAt(pos)=='\r')
            {
                pos++;
            }
            returnPos = tv.indexOf("\n", pos);
        }
        if (pos<tv.length())
        {
            String thisLine = tv.substring(pos).trim();
            if (thisLine.length()>0)
            {
                v.add(thisLine);
            }
        }
    }

   public void writePlainText(NGSection section, Writer out) throws Exception
   {

        if (section == null || out == null) {
            return;
        }

        LineIterator li = new LineIterator(section.asText());
        while (li.moreLines())
        {
            String thisLine = li.nextLine();
            writeTextWithLB(thisLine, out);
        }
   }

    // returns true so that this type gets migrated to a wiki tag.
    public boolean isJustText()
    {
        return true;
    }

    /**
    * Converts a Link section to a note, converting the links
    * appropriately.  The idea is that all (displayable)
    * sections will become notes in the future.
    * This might be called just before deleting the section.
    * Returns NULL if the section is empty.
    */
    public NoteRecord convertToLeaflet(NGSection noteSection,
                   NGSection wikiSection) throws Exception
    {
        SectionDef def = wikiSection.def;
        SectionFormat sf = def.format;
        if (sf != this)
        {
            throw new ProgramLogicError("Method convertToLeaflet must be called on the format object for the section being converted");
        }
        String data = wikiSection.asText();
        if (data==null || data.length()==0)
        {
            //this section is empty, so don't create any note, and return null
            return null;
        }
        StringBuffer modifiedSource = new StringBuffer();
        LineIterator li = new LineIterator(data);
        while (li.moreLines())
        {
            String thisLine = li.nextLine().trim();
            if (thisLine.length()>0)
            {
                modifiedSource.append("* [");
                modifiedSource.append(thisLine);
                modifiedSource.append("]\n");
            }
        }

        NoteRecord newNote = noteSection.createChildWithID(
            SectionComments.LEAFLET_NODE_NAME, NoteRecord.class, "id", IdGenerator.generateKey());
        newNote.setOwner(wikiSection.getLastModifyUser());
        newNote.setLastEditedBy(wikiSection.getLastModifyUser());
        newNote.setLastEdited(wikiSection.getLastModifyTime());
        newNote.setEffectiveDate(wikiSection.getLastModifyTime());
        newNote.setSubject(def.displayName + " - " + wikiSection.parent.getFullName());
        newNote.setData(modifiedSource.toString());
        newNote.setVisibility(def.viewAccess);
        newNote.setEditable(NoteRecord.EDIT_MEMBER);
        return newNote;
    }

}
