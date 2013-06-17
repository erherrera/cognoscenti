package org.socialbiz.cog;

/**
* supports comparing a local and remote project
*/
public class SyncStatus
{
    public static final int TYPE_DOCUMENT = 1;
    public static final int TYPE_NOTE     = 2;
    public static final int TYPE_TASK     = 3;

    ProjectSync    sync;
    public String  universalId;
    public int     type;

    //whether they exist or not, and non-global id
    public boolean isLocal;
    public boolean isRemote;
    public String  idLocal;
    public String  idRemote;

    //name like field: document name, task synopsis, note subject
    public String  nameLocal;
    public String  nameRemote;
    public String  descLocal;
    public String  descRemote;

    //timestamp information
    public long    timeLocal;
    public long    timeRemote;
    public String  editorLocal;
    public String  editorRemote;

    //documents have size, tasks put state here
    public long    sizeLocal;
    public long    sizeRemote;

    //documents have URL, notes use the URL for the note content
    public String  urlLocal;
    public String  urlRemote;

    //these are for tasks/goals only
    public String  assigneeLocal;
    public String  assigneeRemote;
    public int     priorityLocal;
    public int     priorityRemote;


    public SyncStatus(ProjectSync _sync, int _type, String _uid) throws Exception {

        sync = _sync;
        type = _type;
        universalId = _uid;

    }

}
