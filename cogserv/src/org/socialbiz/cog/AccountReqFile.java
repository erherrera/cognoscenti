/**
 *
 */
package org.socialbiz.cog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;

import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.exception.ProgramLogicError;
import org.socialbiz.cog.spring.AccountRequest;

/**
 * Actually holds New Account Requests
 *
 */
public class AccountReqFile extends DOMFile {

    private static List<AccountRequest> allRequests = null;
    private static AccountReqFile accountRequestFile = null;

    public synchronized static void clearAllStaticVars()
    {
        accountRequestFile = null;
        allRequests = null;
    }


    public AccountReqFile(File path, Document newDoc) throws Exception {
        super(path, newDoc);
    }

    private synchronized static String generateKey()
    {
        return IdGenerator.generateKey();
    }


    /**
    * Save the one file holding all the account requests
    */
    public static void saveAll()
        throws Exception
    {
        if (accountRequestFile==null)
        {
            throw new ProgramLogicError("Program logic Error: attempting to save account request records "
            +"when they have not been read yet.");
        }
        accountRequestFile.save();
    }


    /**
    * Get ALL requests in the file
    */
    public static List<AccountRequest> getAccountsStatus() throws Exception
    {
        if (allRequests==null) {
            initializeAccountlist();
        }
        return allRequests;
    }



    /**
    * Get requests more than 48 hours old
    */
    public static List<AccountRequest> scanAllDelayedAccountRequests()
    throws Exception {
        if (allRequests==null) {
            initializeAccountlist();
        }
        List<AccountRequest> delayedList = new ArrayList<AccountRequest>();

        long timeSpan = 0;
        long accountModTime = 0;
        for (AccountRequest accountDetails : allRequests)
        {
            if ((accountDetails.getStatus().equalsIgnoreCase("requested")))
            {
                accountModTime = accountDetails.getModTime();
                timeSpan = System.currentTimeMillis() - accountModTime;
                if(timeSpan >= 172800000) // 172800000 = 48 hours
                {
                    delayedList.add(accountDetails);
                }
            }
        }
        return delayedList;
    }

    /**
     * Create new request for new account with the specified name and description.
     * BAnd save the file.
     */
    public static AccountRequest requestForNewAccount(String displayName,
            String description, AuthRequest ar) throws Exception
    {
        if (allRequests==null) {
            initializeAccountlist();
        }

        if(displayName.length()<4){
            throw new NGException("nugen.exception.account.name.length",null);
        }
        String accountId = displayName.substring(0, 3).toUpperCase() + "_"
                + generateKey();
        String status = "requested";
        String universalId = ar.getUserProfile().getUniversalId();
        String modUser = ar.getUserProfile().getKey();

        AccountRequest accountReq = accountRequestFile.createAccount(accountId, displayName,
                description, status, universalId, ar.nowTime, modUser);

        saveAll();
        return accountReq;
    }

    public static AccountRequest getRequestByKey(String key) throws Exception
    {
        if (allRequests==null) {
            initializeAccountlist();
        }
        for (AccountRequest accountDetails : allRequests)
        {
            if (key.equals(accountDetails.getRequestId()))
            {
                return accountDetails;
            }
        }
        return null;
    }


    public static void removeRequest(String reqId) throws Exception
    {
        if (allRequests==null) {
            initializeAccountlist();
        }
        for (AccountRequest accountDetails : allRequests)
        {
            if (reqId.equals(accountDetails.getRequestId()))
            {
                accountRequestFile.removeChild(accountDetails);
                allRequests.remove(accountDetails);
            }
        }
    }

    private AccountRequest createAccount(String accountId, String displayName,
            String description, String status, String universalId,
            long modTime, String modUser) throws Exception
    {
        if (accountId == null) {
            throw new RuntimeException(
                    "createAccount was passed a null accountId parameter");
        }
        if (displayName == null || displayName.equals("")) {
            throw new RuntimeException(
                    "createAccount was passed a null displayName parameter");
        }
        String requestedId = generateKey();

        AccountRequest newRequest = (AccountRequest) createChild("request",
                AccountRequest.class);

        newRequest.setRequestId(requestedId);
        newRequest.setStatus(status);
        newRequest.setModified(modUser, modTime);

        newRequest.setName(displayName);
        newRequest.setDescription(description);
        newRequest.setAccountId(accountId);
        newRequest.setUniversalId(universalId);
        allRequests.add(newRequest);
        return newRequest;
    }




    /**
    * Read the account request file, and automatically remove old requests
    * from the in-memory version.
    */
    private static synchronized void initializeAccountlist() throws Exception {
        if (accountRequestFile == null) {
            accountRequestFile = readAccountAbsolutePath();
        }

        long tenDaysAgo = System.currentTimeMillis() - 864000000;
        Vector<AccountRequest> requests = accountRequestFile.getChildren("request",
                AccountRequest.class);
        ArrayList<AccountRequest> outOfDate = new ArrayList<AccountRequest>();
        allRequests = new ArrayList<AccountRequest>();
        for (AccountRequest accountDetails : requests) {
            long time = accountDetails.getModTime();
            if (time < tenDaysAgo) {
                // collect all the old requests
                outOfDate.add(accountDetails);
            }
            else {
                allRequests.add(accountDetails);
            }
        }

        // now actually get rid of the out of date children in case this is
        // saved back to file
        for (AccountRequest accountDetails : outOfDate) {
            accountRequestFile.removeChild(accountDetails);
        }
    }


    private static AccountReqFile readAccountAbsolutePath() throws Exception
    {
        // the index is not initialzed, so read file if exists
        File theFile = NGPage.getRealPath("requeted.account");
        try {
            Document newDoc = readOrCreateFile(theFile, "accounts-request");
            AccountReqFile account = new AccountReqFile(theFile, newDoc);
            return account;
        } catch (Exception e) {
            throw new NGException("nugen.exception.unable.load.account.request.file",new Object[]{theFile},e);
        }
    }

    public synchronized List<AccountRequest> scanAllPendingAccounts()
            throws Exception {
        List<AccountRequest> pendingAccount = new ArrayList<AccountRequest>();
        for (AccountRequest accountDetails : allRequests) {
            if ((accountDetails.getStatus().equalsIgnoreCase("requested"))
                    || (accountDetails.getStatus().equalsIgnoreCase("Denied"))) {
                pendingAccount.add(accountDetails);
            }
        }
        return pendingAccount;
    }

    public static List<AccountRequest> scanAllDeniedAccountRequests() throws Exception {
        if (allRequests == null) {
            initializeAccountlist();
        }
        List<AccountRequest> deniedAccounts = new ArrayList<AccountRequest>();
        for (AccountRequest accountDetails : allRequests) {
            if ((accountDetails.getStatus().equalsIgnoreCase("Denied"))) {
                deniedAccounts.add(accountDetails);
            }
        }
        return deniedAccounts;
    }

}
