package org.socialbiz.cog;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;
import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.exception.ProgramLogicError;

/**
* A project folder file versioning system just represents multiple files
* in a file system.  The primary version (the latest version) is kept directly
* in the project folder using the name of the attachment.  Previous version are
* stored in a subfolder named ".cog". and are named as the internal ID and
* appending the version number to that ID.
* For example the following might exist in the file system:
*
*     great-cars/CarStatistics.xls    (this is version 5)
*     great-cars/.cog/9987-1.xls
*     great-cars/.cog/9987-2.xls
*     great-cars/.cog/9987-3.xls
*     great-cars/.cog/9987-4.xls
*
* In this case, the project folder (container) is "great-cars"
* The attachment id is "9987" which is unique within the container.
* The hyphen number at the end is the version number of the attachment.
*
* The drawback of the project versioning system is obvious: all versions of all
* files are in the file system at the same time, taking up space.
* Also, the files are all stored local to one system.
* The advantage, however, is that it does not depend upon fancy code
* to unpack or otherwise manipulate things to access the versions.
* Simply find the version by name, and stream it out.  Very fast.
*
* Disks are cheap, and if your documents are mostly binary, then there is
* little need to use a differencing algorithm to store more efficiently.
*
* The current version file does not have a version number modifier.
*
* Date and time of the version is taken directly from the file system date.
*
* There is no record of who made the change, and other helpful metadata that
* you would get in a real versioning system.  Might think about adding another
* structure in the .cog folder to track that.
*/
public class AttachmentVersionProject implements AttachmentVersion
{
    private File      actualFile;
    private int       number;
    private boolean   readOnly;
    public  boolean   isLatest;    //NOT in the cog subfolder

    /**
    * This is the static method that will search the file system for all of the attachments
    * for a given container and attachment id.  Keeping this code here in the class
    * keeps all the Simple versioning system together in one maintainable place.
    * Other versioning system should have a static member like this as well to find the
    * the versions in their way.
    */
    public static List<AttachmentVersion> getProjectVersions(File projectfolder,  String attachName,
        String attachmentId) throws Exception
    {
        if (projectfolder==null)
        {
            throw new ProgramLogicError("null project folder sent to getProjectVersions");
        }
        if (attachName==null)
        {
            throw new ProgramLogicError("null attachment Name sent to getProjectVersions");
        }
        if (attachmentId==null)
        {
            throw new ProgramLogicError("null attachment Id sent to getProjectVersions");
        }
        if (!projectfolder.exists())
        {
            throw new ProgramLogicError("getProjectVersions needs to be passed a valid projectfolder.  This does not exist: "+projectfolder.toString());
        }
        List<AttachmentVersion> list = new ArrayList<AttachmentVersion>();


        File cogfolder = new File(projectfolder, ".cog");
        int highestVersionSeen = 0;

        if (cogfolder.exists())
        {
            // Here we make up a name to store the file on the server by combining the
            // page key, the attachment key, and then an integer that indicates how many
            // time the attachment has been modified.
            String storageNameBase = "att"+attachmentId+"-";
            int len = storageNameBase.length();

            for (File testFile : cogfolder.listFiles())
            {
                String testName = testFile.getName();
                if (testName.startsWith(storageNameBase))
                {
                    String tail = testName.substring(len);
                    //the version number is everything up to the dot
                    //if no dot, then it is the entire rest of the name
                    int dotPos = tail.indexOf(".");
                    int ver = highestVersionSeen+1;
                    if (dotPos>0)
                    {
                        ver = DOMFace.safeConvertInt(tail.substring(0, dotPos));
                    }
                    else
                    {
                        ver = DOMFace.safeConvertInt(tail);
                    }
                    if (ver==0) {
                        //what do we do if this is zero????
                        //ignore the file because it is not validly named, it does not have a
                        //version number, and so should not include in the list of files
                        continue;
                    }
                    if (ver>highestVersionSeen) {
                        highestVersionSeen = ver;
                    }
                    list.add(new AttachmentVersionProject(testFile, ver, true, false));
                }
            }
        }

        for (File testFile : projectfolder.listFiles())
        {
            String testName = testFile.getName();
            if (attachName.equalsIgnoreCase(testName))
            {
                list.add(new AttachmentVersionProject(testFile, highestVersionSeen+1, true, true));
                break;
            }
        }

        return list;
    }


    /**
    * This static method does the right thing for project versioning system to get a new
    * version file. Calculates the name of the new file, and it streams the entire contents
    * to the file in the attachment folder.  This method is synchronized so that only one
    * thread will be creating a new version at a time, and there is no confusion about
    * what version a file is.
    */
    public synchronized static AttachmentVersionProject getNewProjectVersion(File projectFolder,
        String attachName, String attachmentId, InputStream contents) throws Exception
    {
        File cogFolder = new File(projectFolder,".cog");
        File currentFile = new File(projectFolder, attachName);

        //First, create a temporary file in the cog directory, and copy the contents of the
        //current version to it
        int dotPos = attachName.lastIndexOf(".");
        String fileExtension = "";
        if (dotPos>0) {
            fileExtension = attachName.substring(dotPos);
        }

        File tempCogFile = null;
        if (currentFile.exists()) {
            if (!cogFolder.exists()) {
                cogFolder.mkdir();
            }

            tempCogFile = File.createTempFile("~newV_"+attachmentId, fileExtension, cogFolder);
            copyFileContents(currentFile, tempCogFile);
        }


        //Second, lets copy the new contents here, so that there is no blocking while in the synchronized
        //block.  Create a local file in the attachments folds, and copy the file there, so that
        //later the rename will be very fast.
        File tempFile = File.createTempFile("~newM_"+attachmentId, fileExtension, projectFolder);
        streamContentsToFile(contents, tempFile);

        //Next, search through the directory, find the version number that is next available
        //in the cog folder, and rename the file to that version number, and then rename the
        //temp current file to the current name.  Must be done in a synchronized block
        //to avoid the problem with two threads claiming the same version number.
        synchronized(AttachmentVersionProject.class)
        {
            //first, see what versions exist
            List<AttachmentVersion> list = getProjectVersions(projectFolder, attachName, attachmentId);

            int newSubVersion = 1;
            for (AttachmentVersion av : list)
            {
                if (((AttachmentVersionProject)av).isLatest) {
                    continue;   //skip the latest
                }
                int thisVer = av.getNumber();
                if (thisVer>=newSubVersion)
                {
                    newSubVersion = thisVer+1;
                }
            }

            if (tempCogFile!=null)
            {
                //if there had been a current file to back up, then this will be non-null
                String newSubFileName = "att"+attachmentId+"-"+newSubVersion+fileExtension;
                File newCogFile = new File(cogFolder, newSubFileName);
                if (!tempCogFile.renameTo(newCogFile))
                {
                    throw new NGException("nugen.exception.unable.to.rename.temp.file",new Object[]{tempCogFile,newCogFile});
                }
                newCogFile.setLastModified(currentFile.lastModified());
                currentFile.delete();
            }
            if (!tempFile.renameTo(currentFile))
            {
                throw new NGException("nugen.exception.unable.to.rename.temp.file",new Object[]{tempFile,currentFile});
            }
            return new AttachmentVersionProject(currentFile, newSubVersion+1, false, true);
        }
    }


    protected static void copyFileContents(File source, File dest)  throws Exception
    {
        if (!source.exists())
        {
            throw new Exception("copyFileContents - The source file for copying does not exist: ("+source.toString()+")");
        }
        FileInputStream fis = new FileInputStream(source);
        streamContentsToFile(fis, dest);
        fis.close();
    }

    protected static void streamContentsToFile(InputStream source, File dest)  throws Exception
    {
        FileOutputStream fos = new FileOutputStream(dest);
        byte[] buf = new byte[2048];
        int amtRead = source.read(buf);
        while (amtRead>0)
        {
            fos.write(buf, 0, amtRead);
            amtRead = source.read(buf);
        }
        fos.close();
    }

    /**
    * Use the public static methods above to construct the file.
    */
    public AttachmentVersionProject(File versionFile, int newNumber, boolean isReadOnly, boolean theLatest)
    {
        actualFile = versionFile;
        number = newNumber;
        readOnly = isReadOnly;
        isLatest = theLatest;
    }

    public int getNumber()
    {
        return number;
    }

    public long getCreatedDate()
    {
        return actualFile.lastModified();
    }

    /**
    * Generally an old, historical version is read only.
    * But when you ask for a new version, you get a writeable
    * version object.
    */
    public boolean isReadOnly()
    {
        return readOnly;
    }

    public File getLocalFile()
    {
        return actualFile;
    }

    public void commitLocalFile()
    {
        //for the file system implementation, nothing needs to be done because all
        //of the versions are simply files in the local folder.
    }

    public void releaseLocalFile()
    {
        //for the file system implementation, nothing needs to be done because all
        //of the versions are simply files in the local folder.
    }

}
