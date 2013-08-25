package org.socialbiz.cog;

import java.util.TimerTask;

public class SendEmailThread extends TimerTask {

    public SendEmailThread() {
        //nothing to initialize?
    }

    public void run() {
        try {
            EmailSender emailSender = EmailSender.getInstance();
            EmailRecord eRec=null;
            NGPage possiblePage = null;

            //walk through the known pages and see if there are any email
            //messages there to send
            while ((possiblePage=NGPageIndex.getPageWithEmailToSend()) != null) {
                while ( (eRec=possiblePage.getEmailReadyToSend()) != null) {
                    emailSender.sendPreparedMessageImmediately(eRec);
                }
                possiblePage.save();
            }

            //This is the old, deprecated way to store email messages in the
            //for sending later in a single global store.
            while ( (eRec=EmailRecordMgr.getEmailReadyToSend()) != null) {
                emailSender.sendPreparedMessageImmediately(eRec);
                EmailRecordMgr.save();
            }

        }
        catch (Exception e) {

            //need to save the message that was marked as having failed to send
            //TODO: not sure that this is the right thing to do
            try {
                System.out.println("\nSendEmailThread ERROR------------------\n");
                e.printStackTrace();
                System.out.println("\n-----------------------------\n");
                EmailRecordMgr.save();
            }
            catch (Exception dead) {
                //what can we do here?
            }

        }
        finally {
            //only call this when you are sure you are not holding on to any containers
            NGPageIndex.clearLocksHeldByThisThread();
        }
    }

}
