package org.socialbiz.cog;

import java.io.Writer;
import java.io.IOException;

/**
* @see HTMLWriter
*
* Like HTMLWriter except that a newline character is converted to a BREAK tag
* Allows you write text to HTML, and for the line feed characters to insert
* break tags, which cause a new line to begin.
*
* This is useful in line oriented text, when placed into HTML, so that it is
* not all wrapped into a single paragraph.  Makes the linefeed characters (newline)
* significant over other kinds of white space.
*
*/
public class HTMLWriterLineFeed extends Writer
{
    private Writer wrapped;

    public HTMLWriterLineFeed(Writer _wrapped)
    {
        wrapped = _wrapped;
    }

    public void  write(int c)
        throws IOException
    {
        writeHtmlCharLF(wrapped, c);
    }

    public void write(char[] chs, int start, int len)
        throws IOException
    {
        if (start<0)
        {
            throw new RuntimeException("negative start position passed to HTMLWriter.write(char[], int, int)");
        }
        if (len<0)
        {
            throw new RuntimeException("negative len passed to HTMLWriter.write(char[], int, int)");
        }
        int last=start+len;
        if (last>chs.length)
        {
            throw new RuntimeException("start + len ("+last+") is longer than char array size ("+chs.length+") passed to HTMLWriter.write(char[], int, int)");
        }
        for (int i=start; i<last; i++)
        {
            writeHtmlCharLF(wrapped, chs[i]);
        }
    }

    public void  close()
        throws IOException
    {
        wrapped.close();
    }

    public void  flush()
        throws IOException
    {
        wrapped.flush();
    }


    private static void writeHtmlCharLF(Writer w, int ch)
        throws IOException
    {
        switch (ch)
        {
            case '&':
                w.write("&amp;");
                return;
            case '<':
                w.write("&lt;");
                return;
            case '>':
                w.write("&gt;");
                return;
            case '"':
                w.write("&quot;");
                return;
            case '\n':
                w.write("<br/>\n");
                return;
            default:
                w.write(ch);
                return;
        }
    }

}