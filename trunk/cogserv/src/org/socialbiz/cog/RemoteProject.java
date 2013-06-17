package org.socialbiz.cog;

import java.util.Vector;
import java.net.URL;
import java.io.InputStream;
import org.w3c.dom.Document;

/**
* A remote project is access purely through URLs and REST oriented web services
*/
public class RemoteProject
{

    //inheritance of these classes does not work right, so making here a
    //member to hold the DOM tree
    DOMFace dfroot;
    DOMFace sections;
    DOMFace attachsection;
    DOMFace tasksection;
    DOMFace commentsection;

    public RemoteProject(String urlStr) throws Exception {

        URL url = new URL(urlStr);
        InputStream is = url.openStream();
        Document udoc = DOMUtils.convertInputStreamToDocument(is, false, false);
        dfroot = new DOMFace(udoc, udoc.getDocumentElement(), null);
        sections = dfroot.getChild("sections", DOMFace.class);
        attachsection  = sections.getChild("attachsection", DOMFace.class);
        tasksection    = sections.getChild("tasksection", DOMFace.class);
        commentsection = sections.getChild("commentsection", DOMFace.class);
    }

    public Vector<DOMFace> getNotes() throws Exception {

        DOMFace comments = commentsection.getChild("comments", DOMFace.class);
        return comments.getChildren("comment", DOMFace.class);

    }
    public Vector<DOMFace> getAttachments() throws Exception {

        DOMFace attachments = attachsection.getChild("attachments", DOMFace.class);
        return attachments.getChildren("attachment", DOMFace.class);

    }
    public Vector<DOMFace> getTasks() throws Exception {

        DOMFace process = tasksection.getChild("process", DOMFace.class);
        DOMFace activities = process.getChild("activities", DOMFace.class);
        return activities.getChildren("activity", DOMFace.class);

    }

}
