package org.socialbiz.cog;

import org.socialbiz.cog.exception.ProgramLogicError;
import java.io.Writer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SectionPrivate extends SectionWiki
{

    private static String NOTE_NODE_NAME = "note";
    private static String OWNER_NODE_NAME = "owner";
    private static String DATA_NODE_NAME = "data";
    //private static String PRIVATE_SECTION_NAME = "Private";

    public SectionPrivate()
    {
    }

    public String getName()
    {
        return "Private Format";
    }

    public void writePlainText(NGSection section, Writer out) throws Exception
    {
        //don't throw exception in this case, because it can effect search function
        return;
    }


    public boolean isEmpty(NGSection section) throws Exception
    {
        return false;
    }

    // not a wiki tag, don't convert to a wiki tag.
    public boolean isJustText()
    {
        return false;
    }


    /**
    * Converts a Wiki section to a note, copying appropriate information
    * from the wiki section to the note.  The idea is that all (displayable)
    * sections will become leaflets in the future.
    * This might be called just before deleting the section.
    * Returns NULL if the section is empty.
    *
    * Even though this class is not used in new pages, we have to leave
    * this class around so we can convert old pages.
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
        NodeList nl = DOMUtils.findNodesOneLevel(wikiSection.getElement(), NOTE_NODE_NAME);
        int size = nl.getLength();
        for(int i=0; i<size; i++)
        {
            Element ei = (Element)nl.item(i);
            String owner = DOMUtils.getChildText(ei,OWNER_NODE_NAME);
            String data = DOMUtils.getChildText(ei, DATA_NODE_NAME).trim();
            if (data==null || data.length()==0)
            {
                //this section is empty, so don't create any leaflet, and return null
                return null;
            }
            NoteRecord newNote = noteSection.createChildWithID(
                SectionComments.LEAFLET_NODE_NAME, NoteRecord.class, "id", IdGenerator.generateKey());
            newNote.setOwner(owner);
            newNote.setLastEditedBy(owner);
            newNote.setLastEdited(wikiSection.getLastModifyTime());
            newNote.setEffectiveDate(wikiSection.getLastModifyTime());
            newNote.setSubject("Private Note for "+owner);
            newNote.setData(data);
            newNote.setVisibility(SectionDef.PRIVATE_ACCESS);
            newNote.setEditable(NoteRecord.EDIT_MEMBER);
        }
        return null;
    }


}
