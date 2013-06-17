/**
 *
 */
package org.socialbiz.cog.spring;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.socialbiz.cog.DOMFace;

/**
 *
 */
public class AccountRequest extends DOMFace {

    public AccountRequest(Document doc, Element ele, DOMFace p) {
         super(doc, ele, p);
    }

    public String getName() {
        return getScalar("displayName");
    }

    public void setName(String displayName) {

        setScalar("displayName", displayName.trim());
    }

    public String getDescription() {
        return getScalar("description");
    }

    public void setDescription(String descr) {
        setScalar("description", descr.trim());
    }

    public String getAccountId() {
        return getScalar("accountId");
    }

    public void setAccountId(String accountId) {

        setScalar("accountId", accountId.trim());
    }

    public void setUniversalId(String universalId) {
        setScalar("universalId", universalId.trim());
    }

    /*
     * This returns email id of the user who has requested account.
     */
    public String getUniversalId() {
        return getScalar("universalId");
    }

    public void setRequestId(String Id) {
        setAttribute("Id", Id.trim());
    }

    public void setStatus(String status) {
        setAttribute("status", status.trim());
    }

    public void setModified(String userId, long time) {
        setAttribute("modUser", userId);
        setAttribute("modTime", Long.toString(time));
    }

    public String getStatus() {
        return getAttribute("status");
    }

    public String getRequestId() {
        return getAttribute("Id");
    }

    public long getModTime() {
        return safeConvertLong(getAttribute("modTime"));
    }
    public String getModUser() {
        return getAttribute("modUser");
    }



}
