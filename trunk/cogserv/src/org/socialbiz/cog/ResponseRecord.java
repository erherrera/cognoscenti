package org.socialbiz.cog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ResponseRecord extends DOMFace
{

    public ResponseRecord(Document definingDoc, Element definingElement,
        DOMFace p)
    {
        super(definingDoc, definingElement, p);
        throw new RuntimeException("ResponseRecord class is not being used");
    }

}
