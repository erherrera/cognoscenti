// Support collection of files for file uploader.

package org.socialbiz.cog.util;

import java.io.IOException;
import java.util.Vector;

/**
 * @publish extension
 */
public class UploadFiles {

    UploadFiles()
    {
        m_files = new Vector<UploadFile>();
    }

    protected void addFile(UploadFile newFile) throws Exception
    {
        if (newFile == null)
        {
            throw new IllegalArgumentException("Null file passed to addFile.  File must not be null.");
        }
        m_files.add(newFile);
    }

    public UploadFile getFile(int index)
    {
        if (index < 0)
        {
            throw new IllegalArgumentException("File's index " + index
                    + " cannot be a negative value.");
        }
        int last = getCount();
        if (index >= last)
        {
            throw new IllegalArgumentException("File's index " + index
                    + " is greater than the number of files being held.");
        }
        UploadFile retval = m_files.elementAt(index);
        if (retval == null)
        {
            throw new IllegalArgumentException(
                    "Something is wrong with the collection of files.  Index '"+index+"' returned a null value.");
        }
        return retval;
    }

    public int getCount()
    {
        return m_files.size();
    }

    public long getSize() throws IOException
    {
        long tmp = 0L;
        int last = m_files.size();
        for (int i = 0; i < last; i++)
        {
            tmp += getFile(i).getSize();
        }
        return tmp;
    }

    private Vector<UploadFile> m_files;
}