package org.socialbiz.cog;

import java.io.File;
import java.util.List;
import org.w3c.dom.Document;

/**
* NGProj is a Container that represents a Project.
* This kind of project exists anywhere in a library hierarchy.
* The old project (NGPage) existed only in a single date folder, and all the attachments existed in the attachment folder.
* This project is represented by a folder anywhere on disk,
* and the attachments are just files within that folder.
* The project file itself has a reserved name "ProjectDetails.xml"
*/
public class NGProj extends NGPage
{
    /**
    * This project inhabits a folder on disk, and this is the path to the folder.
    */
    public File containingFolder;


    public NGProj(File theFile, Document newDoc) throws Exception {
        super(theFile, newDoc);

        containingFolder = theFile.getParentFile();
    }


    public static NGProj readProjAbsolutePath(File theFile) throws Exception {
        NGPage newPage = NGPage.readPageAbsolutePath(theFile);
        if (!(newPage instanceof NGProj)) {
            throw new Exception("Attempt to create an NGProj when there is already a NGPage at "+theFile+".  Are you trying to create a NGProj INSIDE the NGPage data folder?");
        }
        return (NGProj) newPage;
    }


    public List<AttachmentRecord> getAllAttachments() throws Exception {
        @SuppressWarnings("unchecked")
        List<AttachmentRecord> list = (List<AttachmentRecord>)(List<?>)
                attachParent.getChildren("attachment", ProjectAttachment.class);
        for (AttachmentRecord att : list) {
            att.setContainer(this);
            String atype = att.getType();
            boolean isDel = att.isDeleted();
            if (atype.equals("FILE") && !isDel)
            {
                File attPath = new File(containingFolder, att.getDisplayName());
                if (!attPath.exists()) {
                    //the file is missing, set to GONE, but should this be persistent?
                    att.setType("GONE");
                }
            }
            else if (atype.equals("GONE"))
            {
                File attPath = new File(containingFolder, att.getDisplayName());
                if (isDel || attPath.exists()) {
                    //either attachment deleted, or we found it again, so set it back to file
                    att.setType("FILE");
                }
            }
        }
        return list;
    }

    public AttachmentRecord createAttachment() throws Exception {
        AttachmentRecord attach = attachParent.createChild("attachment", ProjectAttachment.class);
        String newId = getUniqueOnPage();
        attach.setId(newId);
        attach.setContainer(this);
        attach.setUniversalId( getContainerUniversalId() + "@" + newId );
        return attach;
    }

    public void scanForNewFiles() throws Exception
    {
        File[] children = containingFolder.listFiles();
        List<AttachmentRecord> list = getAllAttachments();
        for (File child : children)
        {
            if (child.isDirectory()) {
                continue;
            }
            String fname = child.getName();
            if (fname.endsWith(".sp")) {
                continue;
            }
            //all others are possible documents at this point

            AttachmentRecord att = null;
            for (AttachmentRecord knownAtt : list) {
                if (fname.equals(knownAtt.getDisplayName())) {
                    att = knownAtt;
                }
            }
            if (att!=null) {
                continue;
            }
            att = createAttachment();
            att.setDisplayName(fname);
            att.setType("EXTRA");
            list.add(att);
        }
    }


    public void removeExtrasByName(String name) throws Exception
    {
        List<ProjectAttachment> list = attachParent.getChildren("attachment", ProjectAttachment.class);
        for (ProjectAttachment att : list) {
            if (att.getType().equals("EXTRA") && att.getDisplayName().equals(name)) {
                attachParent.removeChild(att);
                break;
            }
        }
    }
}