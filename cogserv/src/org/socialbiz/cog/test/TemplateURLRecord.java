package org.socialbiz.cog.test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.socialbiz.cog.DOMFace;

public class TemplateURLRecord extends DOMFace
{

    public TemplateURLRecord(Document doc, Element ele, DOMFace p)
    {
        super(doc, ele, p);
    }

    public String getTestableUrl()
        throws Exception
    {
        return getScalar("url");
    }
    public void setTestableUrl(String url)
        throws Exception
    {
        setScalar("url", url);
    }


}

