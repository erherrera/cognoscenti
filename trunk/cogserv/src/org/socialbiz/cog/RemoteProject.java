/*
 * Copyright 2013 Keith D Swenson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors Include: Shamim Quader, Sameer Pradhan, Kumar Raja, Jim Farris,
 * Sandia Yang, CY Chen, Rajiv Onat, Neal Wang, Dennis Tam, Shikha Srivastava,
 * Anamika Chaudhari, Ajay Kakkar, Rajeev Rastogi
 */

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
