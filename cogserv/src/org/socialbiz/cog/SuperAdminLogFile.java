/*
 * Copyright 2013 Keith D Swenson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors Include: Shamim Quader, Sameer Pradhan, Kumar Raja, Jim Farris,
 * Sandia Yang, CY Chen, Rajiv Onat, Neal Wang, Dennis Tam, Shikha Srivastava,
 * Anamika Chaudhari, Ajay Kakkar, Rajeev Rastogi
 */

package org.socialbiz.cog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.socialbiz.cog.exception.NGException;
import org.socialbiz.cog.exception.ProgramLogicError;
import org.w3c.dom.Document;

/**
 * SuperAdminHelper manages a file called 'SuperAdminInfo.xml' in the user folder
 * That file holds information relevant to the running of the whole server
 *
 * 1. automated scheduling of email messages
 * 2. list of new users who joined recently
 * 3. list of sites accepted/denied
 */
public class SuperAdminLogFile extends DOMFile {

    public SuperAdminLogFile(File path, Document doc) throws Exception {
        super(path, doc);
        requireChild("events", DOMFace.class);
    }

    public static SuperAdminLogFile getInstance() throws Exception {
        File superAdminFile = new File( ConfigFile.getUserFolderOrFail(), "SuperAdminInfo.xml");
        if (!superAdminFile.exists()) {
            //migration from old file in the data folder, to new file in user folder
            //if there, copy contents, and delete old.
            //migration started March 2014
            File otherFile = new File( ConfigFile.getDataFolderOrFail(), "superadmin.logs");
            if (otherFile.exists()) {
                UtilityMethods.copyFileContents(otherFile, superAdminFile);
                otherFile.delete();
            }
        }
        Document newDoc = readOrCreateFile(superAdminFile, "super-admin");
        return new SuperAdminLogFile(superAdminFile, newDoc);
    }

    /**
     * This method returns a list of ALL sites created.
     * TODO: either fix this
     * to return sites created in a particular timespan OR: implement a
     * mechanism that removes the old events from the file, so that only the new
     * ones are left.
     */
    public List<NGBook> getAllNewSites() throws Exception {
        Vector<AdminEvent> allEvents = getEventsParent().getChildren("event",
                AdminEvent.class);
        List<NGBook> newSites = new ArrayList<NGBook>();
        for (AdminEvent event : allEvents) {
            if (event.getContext().equals(AdminEvent.SITE_CREATED)) {
                NGBook site = (NGBook) NGPageIndex.getContainerByKey(event
                        .getObjectId());
                if (site!=null) {
                    //TODO: is this a bad error situation if null??
                    newSites.add(site);
                }
            }
        }
        return newSites;
    }

    /**
     * This method returns a list of ALL users registered
     * TODO: either fix this
     * to return registrations created in a particular timespan OR: implement a
     * mechanism that removes the old registrations from the file, so that only
     * the new ones are left.
     */
    public List<UserProfile> getAllNewRegisteredUsers() throws Exception {
        Vector<AdminEvent> allEvents = getEventsParent().getChildren("event",
                AdminEvent.class);
        List<UserProfile> newUsers = new ArrayList<UserProfile>();
        for (AdminEvent event : allEvents) {
            if (event.getContext().equals(AdminEvent.NEW_USER_REGISTRATION)) {
                UserProfile profile = UserManager.getUserProfileByKey(event
                        .getObjectId());
                if (profile != null) {
                    //TODO: is this a bad error situation if null??
                    newUsers.add(profile);
                }
            }
        }
        return newUsers;
    }

    public static void createAdminEvent(String objectId, long modTime,
            String modUser, String context) throws Exception {
        SuperAdminLogFile superAdminHelper = getInstance();

        if (objectId == null || modUser == null || context == null
                || context.equals("")) {
            throw new RuntimeException(
                    "parameter is required to log an event for Super Admin");
        }

        AdminEvent newEvent = superAdminHelper.getEventsParent().createChild(
                "event", AdminEvent.class);
        newEvent.setObjectId(objectId);
        newEvent.setModified(modUser, modTime);
        newEvent.setContext(context);
        superAdminHelper.save();
    }

    public static void setLastNotificationSentTime(long time, String logTrace)
            throws Exception {
        SuperAdminLogFile superAdminHelper = getInstance();
        superAdminHelper.setScalar("lastnotificationsenttime",
                Long.toString(time));
        superAdminHelper.setScalar("previousSendLog",
                superAdminHelper.getScalar("lastSendLog"));
        superAdminHelper.setScalar("lastSendLog", logTrace);
        superAdminHelper.save();
    }

    public static long getLastNotificationSentTime() throws Exception {
        SuperAdminLogFile superAdminHelper = getInstance();
        String timeString = superAdminHelper
                .getScalar("lastnotificationsenttime");
        return safeConvertLong(timeString);
    }

    public String getSendLog() throws Exception {
        return getScalar("lastSendLog");
    }

    /**
     * Get a four digit numeric id which is unique on the page.
     */
    public String getUniqueOnPage() throws Exception {
        // getUniqueOnPage is not implemented. Do we need this???
        throw new ProgramLogicError("getUniqueOnPage is not implemented.");
    }

    protected DOMFace getEventsParent() throws Exception {
        return requireChild("events", DOMFace.class);
    }

    public static void setLastExceptionNo(long exceptionNO) throws Exception {
        SuperAdminLogFile superAdminHelper = getInstance();
        superAdminHelper.setScalar("exceptionNumber",
                String.valueOf(exceptionNO));
        superAdminHelper.save();
    }

    public static long getNextExceptionNo() throws Exception {
        SuperAdminLogFile superAdminHelper = getInstance();
        String exceptionNo = superAdminHelper.getScalar("exceptionNumber");
        long exceptionNO = safeConvertLong(exceptionNo) + 1;
        return exceptionNO;
    }

    public static void setEmailListenerPropertiesFlag(boolean flag)
            throws Exception {
        SuperAdminLogFile superAdminHelper = getInstance();
        superAdminHelper.setScalar("emailListenerPropertiesFlag",
                String.valueOf(flag));
        superAdminHelper.save();
    }

    public static void setEmailListenerProblem(Throwable ex) throws Exception {
        SuperAdminLogFile superAdminHelper = getInstance();
        superAdminHelper.setScalar("emailListenerProblem",
                NGException.getFullMessage(ex, Locale.getDefault()));
        superAdminHelper.save();
    }

    public static boolean getEmailListenerPropertiesFlag() throws Exception {
        boolean emailListenerPropertiesFlag = false;
        SuperAdminLogFile superAdminHelper = getInstance();
        String flag = superAdminHelper.getScalar("emailListenerPropertiesFlag");
        if (flag != null && flag.length() > 0 && "true".equals(flag)) {
            emailListenerPropertiesFlag = true;
        }
        return emailListenerPropertiesFlag;
    }

    public static String getEmailListenerProblem() throws Exception {
        SuperAdminLogFile superAdminHelper = getInstance();
        return superAdminHelper.getScalar("emailListenerProblem");
    }
}
