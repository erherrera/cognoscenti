package org.socialbiz.cog;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Document;
import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.exception.ProgramLogicError;

public class MicroProfileMgr {

    private static Hashtable<String, MicroProfileRecord> microProfiles = new Hashtable<String, MicroProfileRecord>();
    private static DOMFile  profileFile;
    private static Vector<AddressListEntry> allProfileIds = new Vector<AddressListEntry>();

    public synchronized static void loadMicroProfilesInMemory() throws Exception
    {
        File theFile = NGPage.getRealPath("microprofiles.profile");
        Document newDoc = DOMFile.readOrCreateFile(theFile, "micro-profiles");
        profileFile = new DOMFile(theFile, newDoc);

        refreshMicroProfilesHashTable();
    }

    public static void refreshMicroProfilesHashTable() throws Exception
    {
        microProfiles = new Hashtable<String, MicroProfileRecord>();
        allProfileIds = new Vector<AddressListEntry>();

        for (MicroProfileRecord profileRecord : getAllMicroProfileRecords()){
            String lowerCase = profileRecord.getId().toLowerCase();
            microProfiles.put(lowerCase, profileRecord);
            allProfileIds.add(new AddressListEntry(profileRecord.getId()));
        }
    }

    public static Vector<MicroProfileRecord> getAllMicroProfileRecords() throws Exception
    {
        if (profileFile==null)
        {
            throw new ProgramLogicError("profileFile is null when it shoudl not be.  May not have been initialized correctly.");
        }
        Vector<MicroProfileRecord> vc = profileFile.getChildren("microprofile", MicroProfileRecord.class);
        return vc;
    }

    public synchronized static void save() throws Exception{
        if(profileFile == null){
            throw new NGException("nugen.exception.microprofile.name.not.set",null);
        }
        profileFile.save();
    }

    /**
    * find a MicroProfileRecord, or create one
    */
    public static MicroProfileRecord findOrCreateMicroProfile(String emailId, String displayName) throws Exception
    {
        if (emailId == null) {
            throw new ProgramLogicError("createMicroProfileRecord was passed a null emailId parameter");
        }
        if (profileFile==null) {
            throw new ProgramLogicError("profileFile is null when it should not be.  May not have been initialized correctly.");
        }

        MicroProfileRecord profileRecord = findMicroProfileById(emailId);

        if (profileRecord!=null) {
            return profileRecord;
        }

        profileRecord = profileFile.createChild("microprofile", MicroProfileRecord.class);
        profileRecord.setId(emailId);
        profileRecord.setDisplayName(displayName);

        String lowerCase = emailId.toLowerCase();
        microProfiles.put(lowerCase, profileRecord);
        allProfileIds.add(new AddressListEntry(emailId));
        return profileRecord;
    }

    public synchronized static boolean removeMicroProfileRecord(String id) throws Exception {
        if (id == null) {
            throw new ProgramLogicError("removeMicroProfileRecord was passed a null emailId parameter");
        }
        if (profileFile==null) {
            throw new ProgramLogicError("profileFile is null when it shoudl not be.  May not have been initialized correctly.");
        }
        Vector<MicroProfileRecord> vc = profileFile.getChildren("microprofile", MicroProfileRecord.class);
        Enumeration<MicroProfileRecord> e = vc.elements();
        while (e.hasMoreElements()) {
            MicroProfileRecord child = e.nextElement();
            if (id.equals(child.getAttribute("id"))) {
                profileFile.removeChild(child);
                refreshMicroProfilesHashTable();
                return true;
            }
        }
        return false;
    }

    public synchronized static void setDisplayName(String id, String displayName) throws Exception
    {
        MicroProfileRecord child = findOrCreateMicroProfile(id, displayName);
        child.setDisplayName(displayName);
    }

    public static Vector<AddressListEntry> getAllProfileIds() throws Exception
    {
        return allProfileIds;
    }

    public static MicroProfileRecord findMicroProfileById(String id)
    {
        if (id == null) {
            throw new ProgramLogicError("findMicroProfileById was passed a null id parameter");
        }
        if (microProfiles != null){
            String lowerCase = id.toLowerCase();
            return microProfiles.get(lowerCase);
        }
        return null;
    }
}
