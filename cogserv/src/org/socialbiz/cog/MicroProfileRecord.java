package org.socialbiz.cog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MicroProfileRecord extends DOMFace {

    public MicroProfileRecord(Document doc, Element ele, DOMFace p) {
        super(doc, ele, p);
    }

    public String getId()
    {
        return getAttribute("id");
    }
    public void setId(String id)
    {
        setAttribute("id", id);
    }

    public String getDisplayName() {
        return getAttribute("displayName");
    }

    public void setDisplayName(String displayName) {
        setAttribute("displayName", displayName);
    }


    public void writeLink(AuthRequest ar) throws Exception {
        boolean makeItALink = ar.isLoggedIn() && !ar.isStaticSite();
        writeLinkInternal(ar, makeItALink);
    }

    private void writeLinkInternal(AuthRequest ar, boolean makeItALink) throws Exception {
        String cleanName = getDisplayName();

        if (makeItALink)
        {
            writeSpecificLink(ar, getDisplayName(), getId());
        }
        else
        {
            ar.writeHtml(cleanName);
        }

    }


    /**
    * Creates a link for a displayname and id.  If you don't have a display name
    * pass a nullstring in, and the id will be used instead.
    */
    public static void writeSpecificLink(AuthRequest ar, String displayName, String id)
        throws Exception
    {
        ar.write("<a href=\"javascript:\" onclick=\"javascript:editDetail(");
        ar.writeQuote4JS(id);
        ar.write(", ");
        ar.writeQuote4JS(displayName);
        ar.write(",this,");
        ar.writeQuote4JS(ar.getCompleteURL());
        ar.write(");\">");
        ar.write("<span class=\"red\">");

        if(displayName.length() > 0){
            ar.writeHtml(displayName);
        }else{
            ar.write(id);
        }
        ar.write("</span>");
        ar.write("</a>");
    }
}
