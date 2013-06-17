package org.socialbiz.cog;

import java.util.List;
import java.util.Vector;
import java.io.InputStream;
import java.net.URL;

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
            String attName = att.getUniversalId();
            docNames.add(attName);
        }
        Vector<DOMFace> att2s = remote.getAttachments();
        for (DOMFace att2 : att2s) {
            String attName = att2.getScalar("universalid");
            if (!docNames.contains(attName)) {
                docNames.add(attName);
            }
        }
        for (String docName : docNames) {
            statii.add( findDocStatus( docName, allAtts, att2s ) );
        }
    }

    private SyncStatus findDocStatus(String docName, List<AttachmentRecord> atts, Vector<DOMFace> att2s) throws Exception {
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
        for (DOMFace att2 : att2s) {
            String attName = att2.getScalar("universalid");
            if (docName.equals(attName)) {
                retval.isRemote = true;
                retval.nameRemote = att2.getScalar("name");
                retval.timeRemote = UtilityMethods.getDateTimeFromXML(att2.getScalar("modifiedtime"));
                retval.urlRemote = att2.getScalar("address");
                retval.idRemote = att2.getAttribute("id");
                retval.sizeRemote = DOMFace.safeConvertLong(att2.getScalar("size"));
                retval.editorRemote = att2.getAttribute("modifieduser");
                retval.descRemote = att2.getAttribute("remark");
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
        Vector<DOMFace> notes2 = remote.getNotes();
        for (DOMFace noteRef : notes2) {
            String noteName = noteRef.getScalar("universalid");
            if (!noteIds.contains(noteName)) {
                noteIds.add(noteName);
            }
        }
        for (String noteId : noteIds) {
            statii.add( findNoteStatus( noteId, allNotes, notes2 ) );
        }
    }

    private SyncStatus findNoteStatus(String noteId, List<NoteRecord> noteList,
            Vector<DOMFace> att2s) throws Exception {
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
        for (DOMFace noteRef : att2s) {
            String uid = noteRef.getScalar("universalid");
            if (noteId.equals(uid)) {
                retval.isRemote = true;
                retval.timeRemote = UtilityMethods.getDateTimeFromXML(noteRef.getScalar("time"));
                retval.urlRemote = noteRef.getScalar("content");
                retval.idRemote = noteRef.getAttribute("id");
                retval.nameRemote = noteRef.getScalar("subject");
                retval.editorRemote = noteRef.getAttribute("modifieduser");
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
        Vector<DOMFace> goals2 = remote.getTasks();
        for (DOMFace goal2 : goals2) {
            String goalName = goal2.getScalar("universalid");
            if (!goalIds.contains(goalName)) {
                goalIds.add(goalName);
            }
        }
        for (String goalId : goalIds) {
            statii.add( findGoalStatus( goalId, allGoals, goals2 ) );
        }
    }


    private SyncStatus findGoalStatus(String goalId, List<GoalRecord> atts, Vector<DOMFace> att2s) throws Exception {
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
        for (DOMFace att2 : att2s) {
            String uid = att2.getScalar("universalid");
            if (goalId.equals(uid)) {
                retval.isRemote = true;
                retval.timeRemote = UtilityMethods.getDateTimeFromXML(att2.getScalar("modifiedtime"));
                retval.idRemote = att2.getAttribute("id");
                retval.nameRemote = att2.getScalar("synopsis");
                retval.editorRemote = att2.getScalar("modifieduser");
                retval.assigneeRemote = att2.getScalar("assignee");
                retval.sizeRemote = DOMFace.safeConvertInt(att2.getScalar("state"));
                retval.priorityRemote = DOMFace.safeConvertInt(att2.getScalar("priority"));
                retval.descRemote = att2.getAttribute("description");
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
            AttachmentRecord newAtt;
            if (docStat.isLocal) {
                newAtt = local.findAttachmentByID(docStat.idLocal);
            }
            else {
                newAtt = local.createAttachment();
                newAtt.setUniversalId(docStat.universalId);
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


        local.savePage(ar, "Synchronized notes and documents from upstream project");
    }


}
