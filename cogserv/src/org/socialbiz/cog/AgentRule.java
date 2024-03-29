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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* A ProfileRef is a reference to a remote profile.  This is the address that your
* retrieve remote tasklists (goallists) from.  There may be a number of remote
* sites that belong to a single remote profile, so they are not quite the same
* thing as a remote site.
*/
public class AgentRule extends DOMFace
{

    public AgentRule(Document nDoc, Element nEle, DOMFace p) {
        super(nDoc, nEle, p);
    }

    public String getId() throws Exception {
        return getAttribute("id");
    }
    public void setId(String newVal) throws Exception {
        setAttribute("id", newVal);
    }

    public String getTitle() throws Exception {
        return getScalar("title");
    }
    public void setTitle(String newVal) throws Exception {
        setScalar("title", newVal);
    }

    public String getSubjExpr() throws Exception {
        return getScalar("subjexpr");
    }
    public void setSubjExpr(String newVal) throws Exception {
        setScalar("subjexpr", newVal);
    }

    public String getDescExpr() throws Exception {
        return getScalar("descexpr");
    }
    public void setDescExpr(String newVal) throws Exception {
        setScalar("descexpr", newVal);
    }

    public String getTemplate() throws Exception {
        return getScalar("template");
    }
    public void setTemplate(String newVal) throws Exception {
        setScalar("template", newVal);
    }

    public String getSiteKey() throws Exception {
        return getScalar("site");
    }
    public void setSiteKey(String newVal) throws Exception {
        setScalar("site", newVal);
    }

    public String getOwner() throws Exception {
        return getScalar("owner");
    }
    public void setOwner(String newVal) throws Exception {
        setScalar("owner", newVal);
    }

}
