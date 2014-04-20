package org.socialbiz.cog;

import java.io.StringWriter;

import org.socialbiz.cog.spring.NGWebUtils;
import org.socialbiz.cog.spring.SiteRequest;

public class HistoricActions {

	private AuthRequest ar;
	
	/**
	 * Actions that create history and/or send
	 * email messages, are consolidated into this layer.
	 */
	public HistoricActions(AuthRequest _ar) {
		ar = _ar;
	}
	
	public SiteRequest createNewSiteRequest(String accountID, String accountName, 
			String accountDesc) throws Exception {
		SiteRequest accountDetails = SiteReqFile.createNewSiteRequest(accountID,
            accountName, accountDesc, ar);

        sendSiteRequestEmail( ar,  accountDetails);
        return accountDetails;
	}
	
    private static void sendSiteRequestEmail(AuthRequest ar,
            SiteRequest accountDetails) throws Exception {
        StringWriter bodyWriter = new StringWriter();
        AuthRequest clone = new AuthDummy(ar.getUserProfile(), bodyWriter);
        clone.setNewUI(true);
        clone.retPath = ar.baseURL;
        clone.write("<html><body>\n");
        clone.write("<table>\n<tr><td>Purpose: &nbsp;</td><td>New Site Request</td></tr>");
        clone.write("\n<tr><td>Site Name: &nbsp;</td><td>");
        clone.writeHtml(accountDetails.getName());
        clone.write("</td></tr>");
        clone.write("\n<tr><td>Description: &nbsp;</td><td>");
        clone.writeHtml(accountDetails.getDescription());
        clone.write("</td></tr>");
        clone.write("\n<tr><td>Requested By: &nbsp;</td><td>");
        ar.getUserProfile().writeLink(clone);
        clone.write("</td></tr>");
        clone.write("\n<tr><td>Action: &nbsp;</td><td>");
        clone.write("<a href=\"");
        clone.write(ar.baseURL);
        clone.write("v/approveAccountThroughMail.htm?requestId=");
        clone.write(accountDetails.getRequestId());

        UserProfile up = UserManager.getSuperAdmin(ar);
        if (up != null) {
            clone.write("&userId=");
            clone.write(up.getKey());

            clone.write("&");
            clone.write(AccessControl.getAccessSiteRequestParams(
                    up.getKey(), accountDetails));
        }

        clone.write("\">Click here to Accept/Deny</a>");
        clone.write("</td></tr>");
        clone.write("</table>\n");
        clone.write("<p>Being a <b>Super Admin</b> of the Cognoscenti console, you have rights to accept or deny this request.</p>");
        clone.write("</body></html>");

        EmailSender.simpleEmail(NGWebUtils.getSuperAdminMailList(ar), null,
                "Site Approval for " + ar.getBestUserId(),
                bodyWriter.toString());
    }
	
}
