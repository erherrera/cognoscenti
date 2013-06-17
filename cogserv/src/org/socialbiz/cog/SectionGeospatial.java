package org.socialbiz.cog;
import java.io.Writer;
import java.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.socialbiz.cog.exception.ProgramLogicError;

public class SectionGeospatial extends SectionUtil implements SectionFormat
{
    public SectionGeospatial()
    {
    }

    public String getName()
    {
        return "Geospatial";
    }

    public void findLinks(Vector<String> v, NGSection section) throws Exception
    {
        //not implemented yet, no links from this section found
        throw new ProgramLogicError("Geospatial not supported");
    }


    public void writePlainText(NGSection section, Writer out) throws Exception
    {
        throw new ProgramLogicError("Geospatial not supported");
    }

    public void findIDs(Vector<String> v, NGSection sec) throws Exception
    {
        throw new ProgramLogicError("Geospatial not supported");
    }

    public void addGeoData(AuthRequest ar, NGSection section, Element geospatial)
    {
        DOMUtils.removeAllNamedChild(section.getElement(), "geospatial");
        Node tempNode = section.getDocument().importNode(geospatial, true);
        section.getElement().appendChild(tempNode);
    }

    public boolean isEmpty(NGSection section) throws Exception
    {
        return false;
    }

}
