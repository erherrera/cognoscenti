/*
 * Thumbnail.java (requires Java 1.2+)
 */
package org.socialbiz.cog;

import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.exception.ProgramLogicError;
import org.socialbiz.cog.util.CVSUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.w3c.dom.Document;

/**
 * An account is a collection of pages. This allows a collection of pages to
 * share a single set of members, and a particular look and feel. For archaic
 * reasons called NGBook, should be NGAccount
 */
public class NGBook extends ContainerCommon implements NGContainer {
    public String key;
    public ReminderMgr reminderMgr;
    // The following are the indices which are used by book finding and
    // reading. Initialized by scanAllBooks() method.
    private static Hashtable<String, NGBook> keyToBook = null;
    private static Vector<NGBook> allAccounts = null;
    private static NGBook defaultBook = null;
    // private Vector sectionElements = null;
    private String address;
    private Vector<String> existingIds = null;
    private String[] displayNames;
    // there will be a book with a special name: mainbook.book
    public static final String DEFAULT_BOOK_KEY = "mainbook";
    BookInfoRecord bookInfoRecord;
    NGRole memberRole;
    NGRole ownerRole;

    public NGBook(File path, Document newDoc, String nKey) throws Exception {
        super(path, newDoc);
        address = path.getPath().replace('\\', '/');
        bookInfoRecord = requireChild("bookInfo", BookInfoRecord.class);
        displayNames = bookInfoRecord.getPageNames();

        key = nKey;
        requireChild("notes", DOMFace.class);
        requireChild("attachments", DOMFace.class);
        requireChild("process", DOMFace.class);
        requireChild("history", DOMFace.class);

        memberRole = getRequiredRole("Executives");
        ownerRole = getRequiredRole("Owners");

        // just in case this is an old account object, we need to look for and
        // copy members from the members tag into the role itself
        moveOldMembersToRole();

        // upgrade all the note records
        cleanUpNoteAndDocUniversalId();
    }

    /**
     * SCHEMA MIGRATION CODE - old schema required members to be children of a
     * tag 'members' and also prospective memebers in a tag 'pmembers' This code
     * migrates these to the standard Role object storage format, to a role
     * called 'Executives' The tag 'members' and 'pmembers' are removed from the
     * file.
     * 
     * the old format did not distinguish between executives and owners so these
     * members are migrated to both executives and owners, presumably the real
     * owner will remove the others.
     * 
     * But this code, like other migration code, must be left in in case there
     * are olld book files around with the old format. until 2 years after April
     * 2011 and there are no books older than this.
     */
    private void moveOldMembersToRole() throws Exception {
        // in case there is a pmembers tag around, get rid of that.
        // these are just discarded, and they have to request again
        DOMFace pmembers = getChild("pmembers", DOMFace.class);
        if (pmembers != null) {
            removeChild(pmembers);
        }

        DOMFace members = getChild("members", DOMFace.class);
        if (members == null) {
            return;
        }
        for (String id : members.getVector("member")) {
            AddressListEntry user = AddressListEntry.newEntryFromStorage(id);
            memberRole.addPlayer(user);
            ownerRole.addPlayer(user);
        }
        // now get rid of it so it never is heard from again.
        removeChild(members);

    }

    public static NGBook readBookByKey(String key) throws Exception {
        if (keyToBook == null) {
            // this should never happen, but if it does....
            throw new ProgramLogicError(
                    "in readBookByKey called before the account index initialzed.");
        }
        if (key == null) {
            // pass a null in to get the default book
            return defaultBook;
        }

        NGBook retVal = keyToBook.get(key);
        if (retVal == null) {
            throw new NGException("nugen.exception.book.not.found", new Object[] { key });
        }
        return retVal;
    }

    private static NGBook readBookAbsolutePath(String key, File theFile) throws Exception {
        if (!theFile.exists()) {
            throw new NGException("nugen.exception.file.not.exist", new Object[] { theFile });
        }
        try {
            Document newDoc = readOrCreateFile(theFile, "book");
            NGBook newBook = new NGBook(theFile, newDoc, key);
            return newBook;
        }
        catch (Exception e) {
            throw new NGException("nugen.exception.unable.to.read.file", new Object[] { theFile },
                    e);
        }
    }

    public static Vector<NGBook> getAllAccounts() {
        // might do a copy here if we fear that the receiver will corrupt this
        // vector
        return allAccounts;
    }

    /**
     * Creates a book with the specified name. Generates the key automatically.
     */
    public static NGBook createNewBook(String name) throws Exception {
        String key = IdGenerator.generateKey();
        NGBook ngb = createBookByKey(key, name);
        allAccounts.add(ngb);
        keyToBook.put(key, ngb);
        return ngb;
    }

    /**
     * Creates the sepcial default book This should be kept in sync with above
     * routine.
     */
    private static NGBook createBookByKey(String key, String name) throws Exception {
        File theFile = NGPage.getRealPath(key + ".book");
        if (theFile.exists()) {
            throw new NGException("nugen.exception.cant.crete.new.book", new Object[] { key });
        }
        Document newDoc = readOrCreateFile(theFile, "book");

        NGBook newBook = new NGBook(theFile, newDoc, key);

        // set default values
        newBook.setName(name);
        newBook.setStyleSheet("PageViewer.css");
        newBook.setLogo("logo.gif");
        newBook.setDescription("");

        return newBook;
    }

    public void saveBookAs(String newKey, UserProfile user, String comment) throws Exception {
        try {
            reformatXML();

            File theFile = NGPage.getRealPath(newKey + ".book");
            if (!theFile.exists()) {
                File theParent = theFile.getParentFile();
                theParent.mkdirs();
            }
            saveAs(theFile);
            key = newKey;

            // Add & commit the new file the CVS.
            CVSUtil.add(associatedFile.getAbsolutePath(), user.getName(), comment);
        }
        catch (Exception e) {
            throw new NGException("nugen.exception.unable.to.write.file.for.key",
                    new Object[] { newKey }, e);
        }
    }

    public String getKey() {
        return key;
    }

    public boolean isDefaultBook() {
        return (key.equals(DEFAULT_BOOK_KEY));
    }

    public String getName() {
        String fullName = getFullName();
        if (fullName != null) {
            return fullName;
        }

        return getScalar("name");
    }

    public void setName(String newName) {
        setScalar("name", newName.trim());
    }

    public String[] getAccountNames() {
        if (displayNames == null || displayNames.length < 0) {
            String name = getFullName();
            return new String[] { name };
        }
        return displayNames;
    }

    public void setAccountNames(String[] newNames) {
        bookInfoRecord.setPageNames(newNames);
        displayNames = bookInfoRecord.getPageNames();
    }

    public String getStyleSheet() {
        String ss = getScalar("styleSheet");
        if (ss == null) {
            return "PageViewer.css";
        }
        return ss;
    }

    public void setStyleSheet(String newName) {
        setScalar("styleSheet", newName.trim());
    }

    public String getLogo() {
        String ss = getScalar("logo");
        if (ss == null) {
            return "logo.gif";
        }
        return ss;
    }

    public void setLogo(String newName) {
        setScalar("logo", newName.trim());
    }

    public String getDescription() {
        String ss = getScalar("description");
        if (ss == null) {
            return "";
        }
        return ss;
    }

    public void setDescription(String newDescr) {
        setScalar("description", newDescr.trim());
    }

    /**
     * Set all static values back to their initial states, so that garbage
     * collection can be done, and subsequently, the class will be
     * reinitialized.
     */
    public synchronized static void clearAllStaticVars() {
        keyToBook = null;
        allAccounts = null;
        defaultBook = null;
    }

    /**
     * The method scanAllBooks is used to initialize the indices which are used
     * to quickly find and manipulate books. Book records are read and held in
     * memory since they are used so frequently, and there are relatively few of
     * them.
     * 
     * This method need to consider carefully the "reinitialize" situation.
     * There is an Admin page which allows for clearing memory, garbage
     * collecting, and then reinitializing all the static variables. This is a
     * "soft restart" of the nugen system, reinitializing all internal data
     * structures. How can this be accomplished when we are not sure that all
     * other requests have been stopped? Simply clearing the data structures has
     * the problem that some code tests and automatically regenerates certain
     * structures, and you might have the problem that multiple threads might be
     * rebuilding the index at the same time, and this would be bad.
     * 
     * This method was once written to create all the structures in temporary
     * variables, and then update the globals all at once, but this is not
     * entirely satisfactory because one of the methods used during the
     * initialization might use the older existing indices, which might have the
     * effect of causing a link reference from the new tables to the old tables,
     * and cause garbage collection to fail to reclaim that memory.
     * 
     * The cleanest method would be to clear all global variables, garbage
     * collect with nothing in memory, and then rebuild completely from scratch.
     * That requires a special mode of the server, which prevents any access
     * during the time that the internal memory is being rebuilt. At this point
     * in time such an admin mode is not present, so there is no way to prevent
     * access during the time that the memory is being constructed.
     * 
     * Currently, the globals are cleaned out (nulled), and then reconstructed.
     * If there are accesses during the time of rebuilding, there are two
     * possibilities. First, there could be an exception terminating the
     * processing of the request. This is annoying to users, but a necessary
     * side effect of doing maintenance while the server is online. Second,
     * there might be an operation that causes the regeneration of such data.
     * This is more problematic since that constructed data might have a mixture
     * of old and new links. So this method checks to see if anything has
     * created cached values in the mean time, and throws it's own self-descruct
     * exception.
     */
    public synchronized static void scanAllBooks(String rootDirectory) throws Exception {
        // clear the statics first of all to make sure they are not
        // holding any old values that need to be cleared, also to make
        // sure that they are not set as a side effect of this code,
        // or code on another thread that may be running.
        keyToBook = null;
        allAccounts = null;
        defaultBook = null;

        Hashtable<String, NGBook> tKeyToBook = new Hashtable<String, NGBook>();
        Vector<NGBook> tAllBooks = new Vector<NGBook>();
        NGBook tDefaultBook = null;

        File root = ConfigFile.getFolderOrFail(rootDirectory);

        File[] children = root.listFiles();
        for (int i = 0; i < children.length; i++) {
            File child = children[i];
            String fileName = child.getName();
            if (!fileName.endsWith(".book")) {
                // ignore all files except those that end in .book
                continue;
            }

            String key = fileName.substring(0, fileName.length() - 5);

            NGBook ngb = readBookAbsolutePath(key, child);

            if (key.equals(DEFAULT_BOOK_KEY)) {
                tDefaultBook = ngb;
            }

            tAllBooks.add(ngb);
            tKeyToBook.put(key, ngb);
        }

        if (keyToBook != null || allAccounts != null) {
            // this is the 'self-descruct' message. Either something in
            // the logic above, or something on a different thread
            // has manipulated the static variables during execution.
            // A self-descruct does not solve the problem, but it does
            // alert the programmer / admin that the code logic is
            // somehow incorrect.
            throw new ProgramLogicError("somewhere inside "
                    + "the logic to initialize the key data structures of the "
                    + "server, something incorrectly created some values in "
                    + "'keyToBook' or 'allAccounts'.  This should not happen, and "
                    + "the fact that it happened means that the internal memory "
                    + "structures are in an unknown state.  Restarting the server "
                    + "is recommended in this case.");
        }

        // now make them live
        keyToBook = tKeyToBook;
        allAccounts = tAllBooks;

        // set up the default book. Done after the exposure of the indices
        // above just in case the methods to create a book use the indices.
        if (tDefaultBook == null) {
            tDefaultBook = createBookByKey(DEFAULT_BOOK_KEY, "Main Book");
            tAllBooks.add(tDefaultBook);
            tKeyToBook.put(DEFAULT_BOOK_KEY, tDefaultBook);
        }
        defaultBook = tDefaultBook;
    }

    public static NGBook createNewAccount(String key, String name) throws Exception {
        File theFile = NGPage.getRealPath(key + ".book");
        if (theFile.exists()) {
            throw new Exception(
                    "Unable to create new account, an account with that ID already exists.");
        }

        Document newDoc = readOrCreateFile(theFile, "book");
        NGBook newBook = new NGBook(theFile, newDoc, key);

        // set default values
        newBook.setName(name);
        newBook.setStyleSheet("PageViewer.css");
        newBook.setLogo("logo.gif");

        // where is the account going to go?
        String[] libFolders = ConfigFile.getArrayProperty("libFolder");
        if (libFolders.length > 0) {
            File domFolder = new File(libFolders[0]);
            if (!domFolder.exists()) {
                throw new Exception(
                        "Config setting 'libFolder' is not correct, first value is not existing folder: ("
                                + domFolder + ")");
            }
            File newAccountFolder = new File(domFolder, key);
            if (newAccountFolder.exists()) {
                throw new Exception("Can't create account because folder alread exists: ("
                        + newAccountFolder + ")");
            }
            newAccountFolder.mkdirs();
            newBook.setPreferredProjectLocation(newAccountFolder.toString());
        }

        allAccounts.add(newBook);
        keyToBook.put(key, newBook);
        return newBook;
    }

    public void setKey(String key) {
        setScalar("key", key.trim());
    }

    /**
     * getPermaLink returns the best name to use for linking to this page that
     * is guaranteed not to change. This will be the name of the directory that
     * the file is stored in, which might be randomly generated. This will not
     * necessarily be a descriptive name of the page. But it will be one that
     * does not change.
     */
    public String getPermaLink(String pageResource) {
        return "p/" + getKey() + "/" + pageResource;
    }

    /**
     * Walk through whatever elements this owns and put all the four digit IDs
     * into the vector so that we can generate another ID and assure it does not
     * duplication any id found here.
     */
    public void findIDs(Vector<String> v) throws Exception {
        List<AttachmentRecord> attachments = getAllAttachments();
        for (AttachmentRecord att : attachments) {
            v.add(att.getId());
        }
    }

    public String getUniqueOnPage() throws Exception {
        if (existingIds == null) {
            existingIds = new Vector<String>();
            findIDs(existingIds);
        }
        return IdGenerator.generateFourDigit(existingIds);
    }

    public String getFullName() {
        if (displayNames == null) {
            return getScalar("name");
        }
        if (displayNames.length == 0) {
            return getScalar("name");
        }
        return displayNames[0];
    }

    // /////////////// Role Requests/////////////////////

    public RoleRequestRecord createRoleRequest(String roleName, String requestedBy,
            long modifiedDate, String modifiedBy, String requestDescription) throws Exception {
        DOMFace rolelist = bookInfoRecord.requireChild("Role-Requests", DOMFace.class);
        RoleRequestRecord newRoleRequest = rolelist
                .createChild("requests", RoleRequestRecord.class);
        newRoleRequest.setRequestId(IdGenerator.generateKey());
        newRoleRequest.setModifiedDate(Long.toString(modifiedDate));
        newRoleRequest.setModifiedBy(modifiedBy);
        newRoleRequest.setState("Requested");
        newRoleRequest.setCompleted(false);
        newRoleRequest.setRoleName(roleName);
        newRoleRequest.setRequestedBy(requestedBy);
        newRoleRequest.setRequestDescription(requestDescription);
        newRoleRequest.setResponseDescription("");

        return newRoleRequest;
    }

    public List<RoleRequestRecord> getAllRoleRequest() throws Exception {

        List<RoleRequestRecord> requestList = new ArrayList<RoleRequestRecord>();
        DOMFace rolelist = bookInfoRecord.requireChild("Role-Requests", DOMFace.class);
        Vector<RoleRequestRecord> children = rolelist.getChildren("requests",
                RoleRequestRecord.class);
        for (RoleRequestRecord rrr : children) {
            requestList.add(rrr);
        }
        return requestList;
    }

    // ////////////////// ROLES /////////////////////////

    public NGRole getPrimaryRole() {
        return memberRole;
    }

    public NGRole getSecondaryRole() {
        return ownerRole;
    }

    protected DOMFace getAttachmentParent() throws Exception {
        return requireChild("attachments", DOMFace.class);
    }

    protected DOMFace getNoteParent() throws Exception {
        return requireChild("notes", DOMFace.class);
    }

    protected DOMFace getRoleParent() throws Exception {
        return requireChild("roleList", DOMFace.class);
    }

    protected DOMFace getHistoryParent() throws Exception {
        return requireChild("history", DOMFace.class);
    }

    // ////////////////// NOTES /////////////////////////

    public License getLicense(String id) throws Exception {
        throw new Exception("getLicense is not supported on account containers");
    }

    public void setLastModify(AuthRequest ar) throws Exception {
        ar.assertLoggedIn("Must be logged in in order to modify account.");
        bookInfoRecord.setModTime(ar.nowTime);
        bookInfoRecord.setModUser(ar.getBestUserId());
    }

    public void saveFile(AuthRequest ar, String comment) throws Exception {
        try {
            setLastModify(ar);
            save();
            // commit the modified files to the CVS.
            CVSUtil.commit(address, ar.getBestUserId(), comment);
        }
        catch (Exception e) {
            throw new NGException("nugen.exception.unable.to.write.account.file",
                    new Object[] { address }, e);
        }
    }

    public void saveContent(AuthRequest ar, String comment) throws Exception {
        saveFile(ar, comment);
    }

    public String getAddress() throws Exception {
        return address;
    }

    public String[] getContainerNames() {
        return getAccountNames();
    }

    public void setContainerNames(String[] nameSet) {
        setAccountNames(nameSet);
    }

    public long getLastModifyTime() throws Exception {
        return 0;
    }

    public boolean isDeleted() {
        return false;
    }

    public ReminderMgr getReminderMgr() throws Exception {
        if (reminderMgr == null) {
            reminderMgr = requireChild("reminders", ReminderMgr.class);
        }
        return reminderMgr;
    }

    public void changeVisibility(String oid, AuthRequest ar) throws Exception {
        int visibility = safeConvertInt(ar.reqParam("visibility"));
        NoteRecord note = getNoteOrFail(oid);
        note.setVisibility(visibility);
        note.setEffectiveDate(SectionUtil.niceParseDate(ar.defParam("effDate", "")));
    }

    public List<HistoryRecord> getAllHistory() throws Exception {
        DOMFace historyContainer = requireChild("history", DOMFace.class);
        Vector<HistoryRecord> vect = historyContainer.getChildren("event", HistoryRecord.class);
        HistoryRecord.sortByTimeStamp(vect);
        return vect;
    }

    public HistoryRecord createNewHistory() throws Exception {
        DOMFace historyContainer = requireChild("history", DOMFace.class);
        HistoryRecord newHist = historyContainer.createChild("event", HistoryRecord.class);
        newHist.setId(getUniqueOnPage());
        return newHist;
    }

    public void writeContainerLink(AuthRequest ar, String documentId, int len) throws Exception {
        writeAccountUrl(ar);
        ar.write("/public.htm\">");
        ar.writeHtml(getFullName());
        ar.write("</a>");
    }

    public void writeDocumentLink(AuthRequest ar, String documentId, int len) throws Exception {

        AttachmentRecord att = findAttachmentByID(documentId);
        if (att == null) {
            ar.write("(Document ");
            ar.write(documentId);
            ar.write(")");
            return;
        }
        String nameOfLink = trimName(att.getDisplayName(), len);
        writeAccountUrl(ar);
        ar.write("/docinfo");
        ar.writeURLData(documentId);
        ar.write(".htm\">");
        ar.writeHtml(nameOfLink);
        ar.write("</a>");
    }

    public void writeReminderLink(AuthRequest ar, String reminderId, int len) throws Exception {
        ReminderRecord att = getReminderMgr().findReminderByID(reminderId);
        if (att == null) {
            ar.write("(Reminder ");
            ar.write(reminderId);
            ar.write(")");
            return;
        }
        String nameOfLink = trimName(att.getFileDesc(), len);
        writeAccountUrl(ar);
        ar.write("/sendemailReminder.htm?rid=");
        ar.writeURLData(reminderId);
        ar.write("\">");
        ar.writeHtml(nameOfLink);
        ar.write("</a>");
    }

    public void writeTaskLink(AuthRequest ar, String taskId, int len) throws Exception {
        throw new ProgramLogicError("This account does not have a task '" + taskId
                + "' or any other task.  Accounts don't have tasks.");
    }

    public void writeNoteLink(AuthRequest ar, String noteId, int len) throws Exception {
        NoteRecord note = getNote(noteId);
        if (note == null) {
            if ("x".equals(noteId)) {
                ar.write("(attached documents only)");
            }
            else {
                ar.write("(Note ");
                ar.write(noteId);
                ar.write(")");
            }
            return;
        }

        String nameOfLink = trimName(note.getSubject(), len);
        writeAccountUrl(ar);
        ar.write("/leaflet");
        ar.writeURLData(note.getId());
        ar.write(".htm\">");
        ar.writeHtml(nameOfLink);
        ar.write("</a>");
    }

    private void writeAccountUrl(AuthRequest ar) throws Exception {
        ar.write("<a href=\"");
        ar.writeHtml(ar.baseURL);
        ar.write("t/");
        ar.writeHtml(getKey());
    }

    /**
     * Different accounts can have different style sheets (themes)
     */
    public String getThemePath() {
        String val = bookInfoRecord.getThemePath();
        if (val == null || val.length() == 0) {
            return "theme/blue/";
        }
        return val;
    }

    public void setThemePath(String newName) {
        bookInfoRecord.setThemePath(newName);
    }

    /**
     * This is the path to a folder (on disk) that new projects should be
     * created in for this account. Not all projects will actually be there
     * because older ones may have been created elsewhere, or moved, but new
     * ones created there. If this has a value, then a new folder is created
     * inside this one for the project.
     */
    public String getPreferredProjectLocation() {
        return bookInfoRecord.getScalar("preferredLocation");
    }

    public void setPreferredProjectLocation(String newLoc) {
        bookInfoRecord.setScalar("preferredLocation", newLoc);
    }

    /**
     * Given a new project with a key 'p', this will return the File for the new
     * project file (which does not exist yet). There are two methods:
     * 
     * 1) if a preferred location has been set, then a new folder in that will
     * be created, and the project NGProj placed within that. 2) if no preferred
     * location, then a regular NGPage will be created in datapath folder.
     * 
     * Note: p is NOT the name of the file, but the sanitized key. The returned
     * name should have the .sp suffix on it.
     */
    public File getNewProjectPath(String p) throws Exception {
        String prefLocStr = getPreferredProjectLocation();
        if (prefLocStr != null && prefLocStr.length() > 0) {
            File prefLoc = new File(prefLocStr);
            if (prefLoc.exists()) {
                return newProjFileInPreferredLocation(prefLoc, p);
            }
        }

        if (NGPage.dataPath == null) {
            throw new NGException("nugen.exception.datapath.not.initialized", null);
        }
        if (p.indexOf('/') >= 0) {
            throw new NGException("nugen.exception.path.have.slash", new Object[] { p });
        }
        File theFile = new File(NGPage.dataPath, p + ".sp");

        // this is a security check:
        // The result of combining the path in this way, must result in a path
        // that is still within the data folder, so check that the cannonical
        // path starts with the data folder path.
        if (!NGPage.fileIsInDataPath(theFile)) {
            throw new ProgramLogicError(
                    "Somehow the NGPage file is supposed to be in the datapath, but did not turn out to be: "
                            + theFile);
        }

        return theFile;
    }

    /**
     * Will create a new folder to put the project into based on the key
     */
    private File newProjFileInPreferredLocation(File prefLoc, String key) throws Exception {

        File newFolder = new File(prefLoc, key);

        int count = 0;
        while (newFolder.exists()) {
            count++;
            newFolder = new File(prefLoc, key + "-" + count);
        }

        newFolder.mkdirs();
        File newProjFile = new File(newFolder, key + ".sp");
        return newProjFile;
    }

    public boolean isFrozen() throws Exception {
        return false;
    }

    // //////////////////// DEPRECATED METHODS//////////////////

    public String getAllowPublic() throws Exception {
        return bookInfoRecord.getAllowPublic();
    }

    public void setAllowPublic(String allowPublic) throws Exception {
        bookInfoRecord.setAllowPublic(allowPublic);
    }

    public void save(String modUser, long modTime, String comment) throws Exception {
        try {
            bookInfoRecord.setModTime(modTime);
            bookInfoRecord.setModUser(modUser);
            save();
            // commit the modified files to the CVS.
            CVSUtil.commit(address, modUser, comment);
        }
        catch (Exception e) {
            throw new NGException("nugen.exception.unable.to.write.account.file",
                    new Object[] { address }, e);
        }

    }

}
