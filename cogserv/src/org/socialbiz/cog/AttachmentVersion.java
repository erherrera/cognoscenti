package org.socialbiz.cog;

import java.io.File;

/**
* An attachment can have many different versions, and this object
* represents a specific version.  The AttachmentRecord object will
* return a List of these when UI needs to display all the versions
* of a particular attachment.
*
* This object is not persisted, but instead is information collected
* by the versioning system when requested.
*
* There will be an implementation of this class for each kind of
* versioning system supported.  The AttachmentRecord object will have
* to determine the right implementation class for the server configuration.
*/
public interface AttachmentVersion
{

    /**
    * Returns the integer version number.  There is no minor and major version
    * but just a simple number that represent the sequence of versions.
    */
    public int getNumber();

    /**
    * Returns the date of the version, usually the date that the version was
    * checked into the system.
    */
    public long getCreatedDate();

    /**
    * Generally an old, historical versions are read only.
    * But when you ask for a new version, you get a writeable
    * version object.
    */
    public boolean isReadOnly();

    /**
    * Retrieves the version (if necessary) and returns a File object that points to
    * the (temporary) file that contains the contents.
    *
    * Retrieved versions are usually read only.
    */
    public File getLocalFile();

    /**
    * If you had a writeable version, after writing the contents to the file, you
    * must call commit in order to actually save the contents to the versioning system.
    * This will also release and clean up any unnecessary temporary files or resources.
    */
    public void commitLocalFile();

    /**
    * If you called "getLocalFile" then it is possible that a temporary file has been created
    * or other resources help.  Calling release will eithre delete that file, or otherwise
    * free up the resources help to access the old version.
    */
    public void releaseLocalFile();

}
