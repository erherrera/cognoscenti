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

import java.util.List;
import java.util.Vector;
import java.io.InputStream;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

/**
* supports comparing a local and remote project
*/
public class ProjectSync
{
    NGPage local;
    RemoteProject remote;
    AuthRequest ar;
    String licenseID;

    Vector<SyncStatus> statii;

    public ProjectSync(NGPage _local, RemoteProject _remote, AuthRequest _ar, String _licenseID) throws Exception {

        local     = _local;
        remote    = _remote;
        ar        = _ar;
        licenseID = _licenseID;

        statii = new Vector<SyncStatus>();

        figureAttachments();
        figureNotes();
        figureGoals();

    }

    private void figureAttachments() throws Exception {

        Vector<String> docNames = new Vector<String>();
        List<AttachmentRecord> allAtts = local.getAllAttachments();
        for (AttachmentRecord att : allAtts) {
            if (!"FILE".equals(att.getType())) {
                continue;
            }
            if (att.isDeleted()) {
                continue;
            }
            if (!att.isUpstream()) {
                //simply ignore any attachments not marked for upstream.
                //this has the problem that if you UNCHECK the upstream,
                //then it will think you need to download another version
                //of the document.  However, that seems like the only
                //option, otherwise the document would 'hide' the upstream
                //document.  User has control and it is consistent.
                continue;
            }
            String attName = att.getUniversalId();
            docNames.add(attName);
        }
        JSONArray att2s = remote.getDocs();
        int len = att2s.length();
        for (int i=0; i<len; i++) {
            JSONObject oneAtt = att2s.getJSONObject(i);
            String attName = oneAtt.getString("universalid");
            if (!docNames.contains(attName)) {
                docNames.add(attName);
            }
        }
        for (String docName : docNames) {
            statii.add( findDocStatus( docName, allAtts, att2s ) );
        }
    }

    private SyncStatus findDocStatus(String docName, List<AttachmentRecord> atts, JSONArray att2s) throws Exception {
        SyncStatus retval = new SyncStatus(this, SyncStatus.TYPE_DOCUMENT, docName);

        for (AttachmentRecord att : atts) {

            //this avoids deleted files .. there might be multiple deleted files
            //with the same name.  Be sure not to look for them!
            if (!"FILE".equals(att.getType())) {
                continue;
            }
            String attName = att.getUniversalId();
            if (docName.equals(attName)) {
                retval.isLocal = true;
                retval.nameLocal = att.getNiceName();
                retval.timeLocal = att.getModifiedDate();
                retval.urlLocal = att.getLicensedAccessURL(ar, local, licenseID);
                retval.idLocal = att.getId();
                retval.sizeLocal = att.getFileSize(local);
                retval.editorLocal = att.getModifiedBy();
                retval.descLocal   = att.getComment();
                break;
            }
        }
        int len = att2s.length();
        for (int i=0; i<len; i++) {
            JSONObject att2 = att2s.getJSONObject(i);
            String attName = att2.getString("universalid");
            if (docName.equals(attName)) {
                retval.isRemote = true;
                retval.idRemote = att2.getString("id");
                retval.nameRemote = att2.getString("name");
                retval.timeRemote = att2.getLong("modifiedtime");
                if (retval.timeRemote==0) {
                    throw new Exception("Something is wrong with information about remote document ("
                            +retval.nameRemote+") (id="+retval.idRemote
                            +") because the timestamp is zero");
                }
                retval.urlRemote = att2.getString("content");
                retval.sizeRemote = att2.getLong("size");
                retval.editorRemote = att2.getString("modifieduser");
                break;
            }
        }

        return retval;
    }

    private void figureNotes() throws Exception {

        Vector<String> noteIds = new Vector<String>();

        List<NoteRecord> allNotes = local.getAllNotes();
        for (NoteRecord note : allNotes) {
            if (note.getVisibility()>SectionDef.MEMBER_ACCESS) {
                continue;
            }
            if (note.isDraftNote()) {
                //never communicate drafts
                continue;
            }
            noteIds.add(note.getUniversalId());
        }
        JSONArray notes2 = remote.getNotes();
        int len = notes2.length();
        for (int i=0; i<len; i++) {
            JSONObject noteRef = notes2.getJSONObject(i);
            String noteName = noteRef.getString("universalid");
            if (!noteIds.contains(noteName)) {
                noteIds.add(noteName);
            }
        }
        for (String noteId : noteIds) {
            statii.add( findNoteStatus( noteId, allNotes, notes2 ) );
        }
    }

    private SyncStatus findNoteStatus(String noteId, List<NoteRecord> noteList,
            JSONArray notes2) throws Exception {
        SyncStatus retval = new SyncStatus(this, SyncStatus.TYPE_NOTE, noteId);

        for (NoteRecord note : noteList) {

            //this avoids deleted notes!
            if (note.getVisibility()>SectionDef.MEMBER_ACCESS) {
                continue;
            }
            String uid = note.getUniversalId();
            if (uid.equals(noteId)) {
                retval.isLocal = true;
                retval.timeLocal = note.getLastEdited();
                retval.urlLocal = note.getData();
                retval.idLocal = note.getId();
                retval.nameLocal = note.getSubject();
                retval.editorLocal = note.getLastEditedBy();
                break;
            }
        }
        int len = notes2.length();
        for (int i=0; i<len; i++) {
            JSONObject noteRef = notes2.getJSONObject(i);
            String uid = noteRef.getString("universalid");
            if (noteId.equals(uid)) {
                retval.isRemote = true;
                retval.timeRemote = noteRef.getLong("modifiedtime");
                retval.urlRemote = noteRef.getString("content");
                retval.idRemote = noteRef.getString("id");
                retval.nameRemote = noteRef.getString("subject");
                retval.editorRemote = noteRef.getString("modifieduser");
                break;
            }
        }
        return retval;
    }


    private void figureGoals() throws Exception {
        Vector<String> goalIds = new Vector<String>();

        List<GoalRecord> allGoals = local.getAllGoals();
        for (GoalRecord goal : allGoals) {
            //apparently all tasks are considered ... no such thing as a hidden task
            String uid = goal.getUniversalId();
            if (uid==null || uid.length()==0) {
                throw new Exception("Task "+goal.getId()+" has no universal ID ("+goal.getSynopsis()+") -- nust have one!");
            }
            goalIds.add(uid);
        }
        JSONArray goals2 = remote.getGoals();
        int len = goals2.length();
        for (int i=0; i<len; i++) {
            JSONObject goal2 = goals2.getJSONObject(i);
            String goalName = goal2.getString("universalid");
            if (!goalIds.contains(goalName)) {
                goalIds.add(goalName);
            }
        }
        for (String goalId : goalIds) {
            statii.add( findGoalStatus( goalId, allGoals, goals2 ) );
        }
    }


    private SyncStatus findGoalStatus(String goalId, List<GoalRecord> atts, JSONArray goals2) throws Exception {
        SyncStatus retval = new SyncStatus(this, SyncStatus.TYPE_TASK, goalId);
        for (GoalRecord goal : atts) {
            String uid = goal.getUniversalId();
            if (uid.equals(goalId)) {
                retval.isLocal = true;
                retval.timeLocal = goal.getModifiedDate();
                retval.editorLocal = goal.getModifiedBy();
                retval.idLocal = goal.getId();
                retval.nameLocal = goal.getSynopsis();
                retval.assigneeLocal = goal.getAssigneeCommaSeparatedList();
                retval.sizeLocal = goal.getState();
                retval.priorityLocal = goal.getPriority();
                retval.descLocal   = goal.getDescription();
                break;
            }
        }
        int len = goals2.length();
        for (int i=0; i<len; i++) {
            JSONObject att2 = goals2.getJSONObject(i);
            String uid = att2.getString("universalid");
            if (goalId.equals(uid)) {
                retval.isRemote = true;
                retval.timeRemote = att2.getLong("modifiedtime");
                retval.idRemote = att2.getString("id");
                retval.nameRemote = att2.getString("synopsis");
                retval.editorRemote = att2.getString("modifieduser");
                retval.assigneeRemote = att2.getString("assignee");
                retval.sizeRemote = DOMFace.safeConvertInt(att2.getString("state"));
                retval.priorityRemote = DOMFace.safeConvertInt(att2.getString("priority"));
                retval.descRemote = att2.getString("description");
                break;
            }
        }
        return retval;
    }


    public Vector<SyncStatus> getStatus() {
        return statii;
    }

    /**
    * Pass the type of resource, either SyncStatus.TYPE_DOCUMENT,
    * SyncStatus.TYPE_NOTE, or SyncStatus.TYPE_TASK.
    * Returns a collection that represents all the resources that
    * need to be downloaded.
    */
    public Vector<SyncStatus> getToDownload(int resourceType) {
        Vector<SyncStatus> retset = new Vector<SyncStatus>();
        for (SyncStatus stat : statii) {
            if (stat.type != resourceType) {
                //only interested specified resource type
                continue;
            }
            if (!stat.isRemote) {
                //if there is no remote then you can't download
                continue;
            }
            // if there is no local, or if local is older, or if sizes different regardless of age
            boolean couldDown = (!stat.isLocal
                    ||  stat.timeLocal < stat.timeRemote
                    || (stat.timeLocal == stat.timeRemote  && stat.sizeLocal != stat.sizeRemote)  );

            if (couldDown) {
                retset.add(stat);
            }
        }
        return retset;
    }

    /**
    * Pass the type of resource, either SyncStatus.TYPE_DOCUMENT,
    * SyncStatus.TYPE_NOTE, or SyncStatus.TYPE_TASK.
    * Returns a collection that represents all the resources that
    * need to be uploaded to the upstream site.
    */
    public Vector<SyncStatus> getToUpload(int resourceType) {
        Vector<SyncStatus> retset = new Vector<SyncStatus>();
        for (SyncStatus stat : statii) {
            if (stat.type != resourceType) {
                //only interested specified resource type
                continue;
            }
            if (!stat.isLocal) {
                //if there is no local then you can't upload
                continue;
            }
            // if there is no remote, or if local is newer
            boolean couldUp   = (!stat.isRemote || stat.timeLocal > stat.timeRemote );

            if (couldUp) {
                retset.add(stat);
            }
        }
        return retset;
    }

    /**
    * Pass the type of resource, either SyncStatus.TYPE_DOCUMENT,
    * SyncStatus.TYPE_NOTE, or SyncStatus.TYPE_TASK.
    * Returns a collection that represents all the resources that
    * are fully synchronized and need no additional handling
    */
    public Vector<SyncStatus> getEqual(int resourceType) {
        Vector<SyncStatus> retset = new Vector<SyncStatus>();
        for (SyncStatus stat : statii) {
            if (stat.type != resourceType) {
                //only interested specified resource type
                continue;
            }
            if (!stat.isLocal || !stat.isRemote) {
                //can't be equal if one is missing
                continue;
            }
            // only if they both have same timestamp and size
            boolean isEqual = (stat.timeLocal == stat.timeRemote
                               && stat.sizeLocal == stat.sizeRemote);

            if (isEqual) {
                retset.add(stat);
            }
        }
        return retset;
    }


    /**
    * This will walk through the discrepancies, and it will transfer documents or
    * notes until the projects are in sync.
    */
    public void downloadAll() throws Exception {
        Vector<SyncStatus> docsNeedingDown  = getToDownload(SyncStatus.TYPE_DOCUMENT);

        for (SyncStatus docStat : docsNeedingDown) {
            if (docStat.timeRemote==0) {
                throw new Exception("Something is wrong with information about remote document ("
                        +docStat.nameRemote+") because the timestamp is zero");
            }
            AttachmentRecord newAtt;
            if (docStat.isLocal) {
                newAtt = local.findAttachmentByID(docStat.idLocal);
                if (!newAtt.isUpstream()) {
                    throw new Exception("Synchronization error with document ("+docStat.idLocal
                            +") because local version has been marked as NOT being sychronized upstream.");
                }
            }
            else {
                newAtt = local.createAttachment();
                newAtt.setUniversalId(docStat.universalId);
                //this document came from upstream, so set to synch in the future upstream
                newAtt.setUpstream(true);
            }
            newAtt.setDisplayName(docStat.nameRemote);
            URL link = new URL(docStat.urlRemote);
            InputStream is = link.openStream();
            newAtt.streamNewVersion(ar, local, is);
            newAtt.setModifiedDate(docStat.timeRemote);
            newAtt.setComment(docStat.descRemote);
        }

        Vector<SyncStatus> notesNeedingDown  = getToDownload(SyncStatus.TYPE_NOTE);

        for (SyncStatus noteStat : notesNeedingDown) {

            NoteRecord note;
            if (noteStat.isLocal) {
                note = local.getNote(noteStat.idLocal);
            }
            else {
                note = local.createNote();
                note.setUniversalId(noteStat.universalId);
            }
            note.setSubject(noteStat.nameRemote);
            note.setData(noteStat.urlRemote);
            note.setLastEdited(noteStat.timeRemote);
            note.setLastEditedBy(noteStat.editorRemote);
        }

        Vector<SyncStatus> goalsNeedingDown  = getToDownload(SyncStatus.TYPE_TASK);
        for (SyncStatus goalStat : goalsNeedingDown) {

            GoalRecord goal;
            if (goalStat.isLocal) {
                goal = local.getGoalOrFail(goalStat.idLocal);
            }
            else {
                goal = local.createGoal();
                goal.setUniversalId(goalStat.universalId);
            }
            goal.setSynopsis(goalStat.nameRemote);
            goal.setState((int)goalStat.sizeRemote);
            goal.setAssigneeCommaSeparatedList(goalStat.assigneeRemote);
            goal.setPriority(goalStat.priorityRemote);
            goal.setDescription(goalStat.descRemote);

            //should figure out how to create a history record
            goal.setModifiedDate(goalStat.timeRemote);
            goal.setModifiedBy(goalStat.editorRemote);
        }


        local.saveFile(ar, "Synchronized notes and documents from upstream project");
    }


}
