package org.socialbiz.cog;

import java.util.Vector;
import java.util.Enumeration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.socialbiz.cog.exception.NGException;

/**
* ReminderMgr manages the collection of reminders for a page
*/
public class ReminderMgr extends DOMFace
{

    public ReminderMgr(Document doc, Element definingElement, DOMFace p)
    {
        super (doc, definingElement, p);
    }


    public Vector<ReminderRecord> getAllReminders()
        throws Exception
    {
        Vector<ReminderRecord> vc = getChildren("reminder", ReminderRecord.class);
        return vc;
    }

    public Vector<ReminderRecord> getOpenReminders() throws Exception {
        Vector<ReminderRecord> vc = getChildren("reminder", ReminderRecord.class);
        Vector<ReminderRecord> v = new Vector<ReminderRecord>();
        for (ReminderRecord rRec : vc) {
            if (rRec.isOpen()) {
                v.add(rRec);
            }
        }
        return v;
    }

    public Vector<ReminderRecord> getUserReminders(UserProfile up)throws Exception
    {
        Vector<ReminderRecord> result = new Vector<ReminderRecord>();
        for (ReminderRecord reminderRecord : getAllReminders()) {
            if(reminderRecord.isOpen() && up != null && up.hasAnyId(reminderRecord.getAssignee())){
                result.add(reminderRecord);
            }
        }
        return result;
    }
    public ReminderRecord findReminderByID(String id)
        throws Exception
    {
        Vector<ReminderRecord> v = getAllReminders();
        Enumeration<ReminderRecord> e = v.elements();
        while (e.hasMoreElements())
        {
            ReminderRecord rRec = e.nextElement();
            if (id.equals(rRec.getId()))
            {
                return rRec;
            }
        }
        return null;
    }
    
    public ReminderRecord findReminderByIDOrFail(String id) throws Exception {

        ReminderRecord ret =  findReminderByID( id );        
        if (ret==null)
        {
            throw new NGException("nugen.exception.reminder.not.found",new Object[]{id});
        }
        return ret;
    }

    public ReminderRecord createReminder(String id)
        throws Exception
    {
        return (ReminderRecord) createChildWithID("reminder",
            ReminderRecord.class, "id", id);
    }

    public boolean removeReminder(String id) throws Exception {
        Vector<ReminderRecord> vc = getChildren("reminder", ReminderRecord.class);
        for (ReminderRecord child : vc) {
            if (id.equals(child.getAttribute("id"))) {
                removeChild(child);
                return true;
            }
        }
        // maybe this should throw an exception?
        return false;
    }

}
