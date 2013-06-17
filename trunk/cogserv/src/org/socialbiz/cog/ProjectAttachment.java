package org.socialbiz.cog;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* Project Attachments (attachments on NGProj) work differently than attachments on
* NGPage objects.  They are stored with real file names (not just IDs) in the same
* folder that the project is stored in.
*
* Former versions are stored in a subfolder named ".cog"
*
* This allows projects to exist in folders that already contain documents, and those
* documents automatically become part of the project.  This record implements the
* special attachment behavior that projects need.
*/
public class ProjectAttachment extends AttachmentRecord
{

    public ProjectAttachment(Document doc, Element definingElement, DOMFace attachmentContainer) {
        super (doc, definingElement, attachmentContainer);
    }

    public void setContainer(NGContainer newCon) throws Exception
    {
        if (!(newCon instanceof NGProj)) {
            throw new Exception("Problem: ProjectAttachment should only belong to NGProject, but somehow got a different kind of container.");
        }
        container = newCon;
    }

    public void updateActualFile(String oldName, String newName) throws Exception
    {
        if (container==null) {
            throw new Exception("ProjectAttachment record has not be innitialized correctly, there is no container setting.");
        }
        File folder = ((NGProj)container).containingFolder;
        File docFile = new File(folder, oldName);
        File newFile = new File(folder, newName);
        if (docFile.exists()) {
            //this will fail if the file already exists.
            docFile.renameTo(newFile);
        }
        else {
            //it is possible that user is 'fixing' the project by changing the name of an attachment
            //record to the name of an existing file.  IF this is the case, there may have been a
            //record of an "extra" file.  This will eliminate that.
            ((NGProj)container).removeExtrasByName(newName);
        }
    }

    /**
    * Get a list of all the versions of this attachment that exist.
    * The container is needed so that each attachment can caluculate
    * its own name properly.
    */
    public List<AttachmentVersion> getVersions(NGContainer ngc)
        throws Exception {
        if (!(ngc instanceof NGProj)) {
            throw new Exception("Problem: ProjectAttachment should only belong to NGProject, but somehow got a different kind of container.");
        }

        File projectFolder = ((NGProj)ngc).containingFolder;
        if (projectFolder==null) {
            throw new Exception("NGProject container has no containing folder????");
        }

        List<AttachmentVersion> list =
            AttachmentVersionProject.getProjectVersions(projectFolder, getNiceName(), getId());

        sortVersions(list);

        return list;
    }

    /**
    * Provide an input stream to the contents of the new version, and this method will
    * copy the contents into here, and then create a new version for that file, and
    * return the AttachmentVersion object that represents that new version.
    */
    public AttachmentVersion streamNewVersion(AuthRequest ar, NGContainer ngc, InputStream contents)
        throws Exception {

        if (!(ngc instanceof NGProj)) {
            throw new Exception("Problem: ProjectAttachment should only belong to NGProject, but somehow got a different kind of container.");
        }
        File projectFolder = ((NGProj)ngc).containingFolder;
        if (projectFolder==null) {
            throw new Exception("NGProject container has no containing folder????");
        }

        String displayName = getNiceName();
        AttachmentVersion av = AttachmentVersionProject.getNewProjectVersion(projectFolder, displayName,
                getId(), contents);

        //update the record
        setVersion(av.getNumber());
        setStorageFileName(av.getLocalFile().getName());
        setModifiedDate(ar.nowTime);
        setModifiedBy(ar.getBestUserId());

        return av;
    }

}
