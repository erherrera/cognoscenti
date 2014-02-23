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
 */

package org.socialbiz.cog.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.socialbiz.cog.AttachmentRecord;
import org.socialbiz.cog.AttachmentVersion;
import org.socialbiz.cog.AuthRequest;
import org.socialbiz.cog.GoalRecord;
import org.socialbiz.cog.HtmlToWikiConverter;
import org.socialbiz.cog.MimeTypes;
import org.socialbiz.cog.NGPageIndex;
import org.socialbiz.cog.NoteRecord;
import org.socialbiz.cog.ServerInitializer;
import org.socialbiz.cog.UtilityMethods;
import org.socialbiz.cog.WikiConverter;
import org.workcast.streams.HTMLWriter;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet serves up pages using the following URL format:
 *
 * http://{machine:port}/{application}/api/{site}/{project}/{resource}
 *
 * http://{machine:port}/{application} is whatever you install the application to on
 * Tomcat could be multiple levels deep.
 *
 * "api" is fixed. This is the indicator within the system that says
 * this servlet will be invoked.
 *
 * {site} unique identifier for the site.
 *
 * {project} unique identifier for the project.
 *
 * All of the stuff above can be abbreviated {site-proj}
 * so the general pattern is:
 * {site-proj}/{resource}
 *
 * {resource} specifies the resource you are trying to access.
 * See below for the details.  NOTE: you only receive resources
 * that you have access to.  Resources you do not have access
 * to will not be included in the list, and will not be accessible
 * in any way.
 *
 * {site-proj}/summary.json
 * This will list all the goals, notes, and attachments to this project.
 * and include some info like modified date, owner, and file size.
 *
 * {site-proj}/doc{docid}/docname.ext
 * documents can be accessed directly with this, the docname and extension
 * is the name of the document and the proper extension, but can actually
 * be anything.  The only thing that matters is the docid.
 * A PUT to this address will create a new version of the document.
 *
 * {site-proj}/doc{docid}-{version}/docname.ext
 * Gets a version of the document directly if that version exists.
 * Again, the name is just so the browser works acceptably, the
 * document is found using docid and version alone.
 * You can not PUT to this, versions are immutable.
 *
 * {site-proj}/note{noteid}/note1.html
 * This will retrieve the contents of a note in HTML format
 * A PUT to this address will update the note
 *
 * {site-proj}/note{noteid}/note1.sp
 * This will retrieve the contents of a note in SmartPage Wiki format
 * A PUT to this address will update the note
 *
 * {site-proj}/goal{goalid}/goal.json
 * Will retrieve a goal in JSON format
 * A POST to this address will update the goal in JSON format
 */
@SuppressWarnings("serial")
public class APIServlet extends javax.servlet.http.HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        AuthRequest ar = AuthRequest.getOrCreate(req, resp);
        try {
            if (!ServerInitializer.isRunning()) {
                throw new Exception("Server is not ready to handle requests.");
            }

            doAuthenticatedGet(ar);
        }
        catch (Exception e) {
            handleException(e, ar);
        }
        finally {
            NGPageIndex.clearLocksHeldByThisThread();
        }
        ar.logCompletedRequest();
    }

    private void doAuthenticatedGet(AuthRequest ar)  throws Exception {

        try {
            ResourceDecoder resDec = new ResourceDecoder(ar);

            if (resDec.isListing){
                genProjectListing(ar, resDec);
            }
            else if (resDec.isDoc) {
                streamDocument(ar, resDec);
            }
            else if (resDec.isGoal) {
                genGoalInfo(ar, resDec);
            }
            else if (resDec.isNote) {
                streamNote(ar, resDec);
            }
            else {
                throw new Exception("don't understand that resource URL: "+ar.getCompleteURL());
            }
            ar.flush();

        } catch (Exception e) {
            handleException(e, ar);
        }
    }

    public void doPut(HttpServletRequest req, HttpServletResponse resp) {
        AuthRequest ar = AuthRequest.getOrCreate(req, resp);
        try {
            ResourceDecoder resDec = new ResourceDecoder(ar);

            if (resDec.isDoc) {
                receiveDocument(ar, resDec);
            }
            else if (resDec.isNote) {
                receiveNote(ar, resDec);
            }
            else {
                throw new Exception("Can not do a PUT to that resource URL: "+ar.getCompleteURL());
            }
            ar.flush();
        }
        catch (Exception e) {
            handleException(e, ar);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        AuthRequest ar = AuthRequest.getOrCreate(req, resp);
        handleException(new Exception("not implemented yet"), ar);
    }

    public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        AuthRequest ar = AuthRequest.getOrCreate(req, resp);
        handleException(new Exception("not implemented yet"), ar);
    }

    public void init(ServletConfig config)
          throws ServletException {
        //don't initialize here.  Instead, initialize in SpringServlet!
    }

    private void handleException(Exception e, AuthRequest ar)
    {
        try
        {
            ar.logException("API Servlet", e);

            ar.resp.setContentType("text/html;charset=UTF-8");
            ar.write("<html><body><ul><li>Exception: ");
            ar.writeHtml(e.toString());
            ar.write("</li></ul>\n");
            ar.write("<hr/>\n");
            ar.write("<a href=\"");
            ar.write(ar.retPath);
            ar.write("\" title=\"Access the root\">Main</a>\n");
            ar.write("<hr/>\n<pre>");
            e.printStackTrace(new PrintWriter(new HTMLWriter(ar.w)));
            ar.write("</pre></body></html>\n");
            ar.flush();
        } catch (Exception eeeee) {
            // nothing we can do here...
        }
    }




    private void genProjectListing(AuthRequest ar, ResourceDecoder resDec) throws Exception {
        JSONObject root = new JSONObject();

        String urlRoot = ar.baseURL + "api/" + resDec.siteId + "/" + resDec.projId + "/";

        JSONArray goals = new JSONArray();
        for (GoalRecord goal : resDec.project.getAllGoals()) {
            JSONObject thisGoal = new JSONObject();
            String contentUrl = urlRoot + "goal" + goal.getId() + "/goal.json";
            thisGoal.put("universalid", goal.getUniversalId());
            thisGoal.put("id", goal.getId());
            thisGoal.put("synopsis", goal.getSynopsis());
            thisGoal.put("description", goal.getDescription());
            thisGoal.put("modifiedtime", goal.getModifiedDate());
            thisGoal.put("modifieduser", goal.getModifiedBy());
            thisGoal.put("state", goal.getState());
            thisGoal.put("priority", goal.getPriority());
            thisGoal.put("startdate", goal.getStartDate());
            thisGoal.put("enddate", goal.getEndDate());
            thisGoal.put("rank", goal.getRank());
            thisGoal.put("content", contentUrl);
            goals.put(thisGoal);
        }
        root.put("goals", goals);

        JSONArray docs = new JSONArray();
        for (AttachmentRecord att : resDec.project.getAllAttachments()) {
            if (att.isDeleted()) {
                continue;
            }
            if (att.isUnknown()) {
                continue;
            }
            if (!"FILE".equals(att.getType())) {
                continue;
            }
            JSONObject thisDoc = new JSONObject();
            String contentUrl = urlRoot + "doc" + att.getId() + "/"
                    + URLEncoder.encode(att.getNiceName(), "UTF-8");
            thisDoc.put("universalid", att.getUniversalId());
            thisDoc.put("id", att.getId());
            thisDoc.put("name", att.getNiceName());
            thisDoc.put("size", att.getFileSize(resDec.project));
            thisDoc.put("modifiedtime", att.getModifiedDate());
            thisDoc.put("modifieduser", att.getModifiedBy());
            thisDoc.put("content", contentUrl);
            docs.put(thisDoc);
        }
        root.put("docs", docs);

        JSONArray notes = new JSONArray();
        for (NoteRecord note : resDec.project.getAllNotes()) {
            JSONObject thisNote = new JSONObject();
            thisNote.put("subject", note.getSubject());
            thisNote.put("modifiedtime", note.getLastEdited());
            thisNote.put("modifieduser", note.getLastEditedBy());
            thisNote.put("universalid", note.getUniversalId());
            thisNote.put("contentURL", "figure this out");
            thisNote.put("id", note.getId());
            notes.put(thisNote);
        }
        root.put("notes", notes);

        ar.resp.setContentType("application/json");
        root.write(ar.resp.getWriter(), 2, 0);
        ar.flush();
    }

    private void streamDocument(AuthRequest ar, ResourceDecoder resDec) throws Exception {
        AttachmentRecord att = resDec.project.findAttachmentByIDOrFail(resDec.docId);
        ar.resp.setContentType(MimeTypes.getMimeType(att.getNiceName()));
        AttachmentVersion aVer = att.getLatestVersion(resDec.project);
        File realPath = aVer.getLocalFile();
        UtilityMethods.streamFileContents(realPath, ar.resp.out);
    }

    private void genGoalInfo(AuthRequest ar, ResourceDecoder resDec) throws Exception {
        GoalRecord goal = resDec.project.getGoalOrFail(resDec.goalId);
        JSONObject thisGoal = new JSONObject();
        thisGoal.put("universalid", goal.getUniversalId());
        thisGoal.put("id", goal.getId());
        thisGoal.put("synopsis", goal.getSynopsis());
        thisGoal.put("description", goal.getDescription());
        thisGoal.put("modifiedtime", goal.getModifiedDate());
        thisGoal.put("modifieduser", goal.getModifiedBy());
        thisGoal.put("state", goal.getState());
        thisGoal.put("priority", goal.getPriority());
        thisGoal.put("startdate", goal.getStartDate());
        thisGoal.put("enddate", goal.getEndDate());
        thisGoal.put("rank", goal.getRank());

        ar.resp.setContentType("application/json");
        thisGoal.write(ar.resp.getWriter(), 2, 0);
        ar.flush();
    }

    private void streamNote(AuthRequest ar, ResourceDecoder resDec) throws Exception {
        NoteRecord note = resDec.project.getNoteOrFail(resDec.noteId);
        String contents = note.getData();
        if (contents.length()==0) {
            contents = "-no contents-";
        }
        if (resDec.isHtmlFormat) {
            ar.resp.setContentType("text/html;charset=UTF-8");
            WikiConverter.writeWikiAsHtml(ar, contents);
        }
        else {
            ar.resp.setContentType("text");
            ar.write(contents);
        }
        ar.flush();
    }

    private void receiveDocument(AuthRequest ar, ResourceDecoder resDec) throws Exception {
        AttachmentRecord att = resDec.project.findAttachmentByIDOrFail(resDec.docId);
        InputStream is = ar.req.getInputStream();
        att.streamNewVersion(ar, resDec.project, is);
        resDec.project.save();
    }

    private void receiveNote(AuthRequest ar, ResourceDecoder resDec) throws Exception {
        NoteRecord note = resDec.project.getNoteOrFail(resDec.noteId);
        InputStream is = ar.req.getInputStream();
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        StringBuffer sb = new StringBuffer();
        char[] buf = new char[800];
        int amt = isr.read(buf);
        while (amt>0) {
            sb.append(buf, 0, amt);
            amt = isr.read(buf);
        }
        String recContent = sb.toString();
        if (resDec.isHtmlFormat) {
            HtmlToWikiConverter h = new HtmlToWikiConverter();
            String contents = h.htmlToWiki(ar.baseURL, recContent);
            note.setData(contents);
        }
        else {
            note.setData(recContent);
        }
    }


}
