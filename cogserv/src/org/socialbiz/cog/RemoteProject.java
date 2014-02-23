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

import java.net.URL;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
* A remote project is access purely through URLs and REST oriented web services
*/
public class RemoteProject
{

    JSONObject root;

    public RemoteProject(String urlStr) throws Exception {

        URL url = new URL(urlStr);
        InputStream is = url.openStream();
        JSONTokener jt = new JSONTokener(is);
        root = new JSONObject(jt);
    }

    public JSONArray getNotes() throws Exception {
        return root.getJSONArray("notes");
    }
    public JSONArray getDocs() throws Exception {
        return root.getJSONArray("docs");
    }
    public JSONArray getGoals() throws Exception {
        return root.getJSONArray("goals");
    }

}
