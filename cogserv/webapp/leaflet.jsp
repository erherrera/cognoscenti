<%@page errorPage="error.jsp"
%><%@page contentType="text/html;charset=UTF-8" pageEncoding="ISO-8859-1"
%><%@page import="org.socialbiz.cog.AuthRequest"
%><%@page import="org.socialbiz.cog.HistoryRecord"
%><%@page import="org.socialbiz.cog.NoteRecord"
%><%@page import="org.socialbiz.cog.LeafletResponseRecord"
%><%@page import="org.socialbiz.cog.NGBook"
%><%@page import="org.socialbiz.cog.NGPage"
%><%@page import="org.socialbiz.cog.NGPageIndex"
%><%@page import="org.socialbiz.cog.NGSection"
%><%@page import="org.socialbiz.cog.SectionComments"
%><%@page import="org.socialbiz.cog.SectionDef"
%><%@page import="org.socialbiz.cog.SectionFormat"
%><%@page import="org.socialbiz.cog.SectionUtil"
%><%@page import="org.socialbiz.cog.UserProfile"
%><%@page import="org.socialbiz.cog.WikiConverter"
%><%@page import="org.socialbiz.cog.UtilityMethods"
%><%@page import="java.io.Writer"
%><%@page import="java.net.URLEncoder"
%><%@page import="java.util.Enumeration"
%><%@page import="java.util.Hashtable"
%><%@page import="java.util.Vector"
%><%@page import="org.w3c.dom.Element"
%><%ar = AuthRequest.getOrCreate(request, response, out);

    /* if the parameter is not found in the parameters list, then find it out in the attributes list */
    String p = ar.reqParam("p");
    String lid = ar.reqParam("lid");

    ngp = NGPageIndex.getProjectByKeyOrFail(p);
    ar.setPageAccessLevels(ngp);
    boolean isMember = isMember(ar, ngp);
    boolean isAdmin = isAdmin(ar, ngp);

    ngb = ngp.getAccount();
    specialTab = "Note";

    NoteRecord leaflet = ngp.getNoteOrFail(lid);

    pageTitle = ngp.getFullName()+": "+leaflet.getSubject();

    int allowedLevel = leaflet.getVisibility();
    boolean canAccess = (allowedLevel<=1 || isMember);%>

<%@ include file="Header.jsp"%>
<%
    headlinePath(ar, leaflet.getSubject());

    if (!canAccess)
    {
        if (!ar.isLoggedIn())
        {
            mustBeLoggedInMessage(ar);
        }
        else
        {
            mustBeMemberMessage(ar);
        }
    }
    else
    {
        writeOneLeaflet(ngp, ar, allowedLevel, leaflet);
%>
    <div class="pagenavigation">
        <div class="pagenav">
            <div class="left">
<%
            if (ar.isLoggedIn())
            {
                UserProfile up = ar.getUserProfile();
                LeafletResponseRecord llr = leaflet.getOrCreateUserResponse(up);
                String choices = leaflet.getChoices();
                String[] choiceArray = splitOnDelimiter(choices, ',');
                String data = llr.getData();
            %>
            <h3><a name="Response"></a>Your Response</h3>

            <form method="post" action="<%= ar.retPath %>LeafletResponseAction.jsp">
            <input type="hidden" name="p" value="<% ar.writeHtml(p); %>">
            <input type="hidden" name="lid" value="<% ar.writeHtml(lid); %>">
            <input type="hidden" name="uid" value="<% ar.writeHtml(up.getKey()); %>">
            <input type="hidden" name="go" value="<% ar.writeHtml(ar.getResourceURL(ngp,leaflet)); %>">

            <table>
            <tr><td>Choice</td><td>
<%
                for (String ach : choiceArray) {
                    String isChecked = "";
                    if (ach.equals(llr.getChoice())) {
                        isChecked = " checked=\"checked\"";
                    }
                %>
                <input type="radio" name="choice"<%=isChecked%> value="<% ar.writeHtml(ach);%>"> <% ar.writeHtml(ach);%> &nbsp;
                <%
                }
            %>

                </td></tr>
            <tr><td>Response</td><td><textarea name="data"><% ar.writeHtml(data); %></textarea></td></tr>
            <tr><td></td><td><input type="submit" name="action" value="Update"></td></tr>
            </table>
            <%
            } else {
            %>
                Please log in to create a response, and to see the other responses.
            <%
            }
            %>
            </div>
            <div class="right"></div>
            <div class="clearer">&nbsp;</div>
        </div>
        <div class="pagenav_bottom"></div>
    </div>

            </form>
            <h3>Responses</h3>
            <table>
            <col width="150">
            <col width="550">
            <%

            Vector<AddressListEntry> recs = leaflet.getResponses();
            Hashtable choiceTotals= new Hashtable();

            for (LeafletResponseRecord llr : recs)
            {
                AddressListEntry ale = new AddressListEntry(llr.getUser());
                String choice = llr.getChoice();
                Integer tot = (Integer) choiceTotals.get(choice);
                if (tot==null)
                {
                    tot = new Integer(1);
                }
                else
                {
                    tot = new Integer( tot.intValue()+1 );
                }
                choiceTotals.put(choice, tot);

                %><tr><td><%
                ale.writeLink(ar);
                %>:</td><td><b><%
                ar.writeHtml(choice);
                %></b> - <%
                SectionUtil.nicePrintTime(ar, llr.getLastEdited(), ar.nowTime);
                %></td></tr>
                <tr><td></td><td><%
                WikiConverter.writeWikiAsHtml(ar,llr.getData());
                %></td></tr>
                <%
            }

            %>
            </table>
            <br/>
            <h3>Totals</h3>
            <ul>
            <%

            e = choiceTotals.keys();
            while (e.hasMoreElements())
            {
                String  choice = (String)e.nextElement();
                Integer tot = (Integer) choiceTotals.get(choice);
                %><li><%ar.writeHtml(choice);%>: <%=tot.intValue()%></li><%
            }
            %>
            </ul>

            <br/>



            <h3>History</h3>
            <ul>
            <%
                List<HistoryRecord> histRecs = ngp.getAllHistory();
                for (HistoryRecord hist : histRecs)
                {
                    if (hist.getContextType()==HistoryRecord.CONTEXT_TYPE_LEAFLET
                        && lid.equals(hist.getContext()))
                    {
                        AddressListEntry ale = new AddressListEntry(hist.getResponsible());

                        %>
                        <li><%
                        ar.writeHtml(HistoryRecord.convertEventTypeToString(hist.getEventType()));
                        ar.write(" by ");
                        ale.writeLink(ar);
                        ar.write(" ");
                        SectionUtil.nicePrintTime(ar, hist.getTimeStamp(), ar.nowTime);
                        if (hist.getEventType()==HistoryRecord.EVENT_EMAIL_SENT)
                        {
                            ar.write(" to ");
                            String[] addressees = UtilityMethods.splitOnDelimiter(hist.getComments(),',');
                            for (String adr : addressees)
                            {
                                AddressListEntry ale2 = new AddressListEntry(adr);
                                ale2.writeLink(ar);
                            }
                        }
                        else
                        {
                            ar.write(" - ");
                            ar.writeHtml(hist.getComments());
                        }

                        %></li><%
                    }
                }
            %>
            </ul>
            <%
        }

        out.flush();

%>

<%@ include file="Footer.jsp"%>
<%@ include file="functions.jsp"%>
