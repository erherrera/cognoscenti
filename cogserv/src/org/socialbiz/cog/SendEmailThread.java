package org.socialbiz.cog;

public class SendEmailThread extends Thread {

    SendEmailThread() {
        start();
    }

    public void run() {

        try {
            //don't send email immediately on startup ... wait 30 seconds
            Thread.sleep(30000);
            EmailSender emailSender = EmailSender.getInstance();
            EmailRecord eRec = null;
            while (true) {
                try {
                    while ( (eRec = EmailRecordMgr.getEmailReadyToSend()) != null ) {
                        emailSender.sendPreparedMessageImmediately(eRec);
                        EmailRecordMgr.save();
                    }

                    EmailRecordMgr.blockUntilNextMessage();
                }
                catch (Exception e) {

                    //need to save the message that was marked as having failed to send
                    EmailRecordMgr.save();

                    System.out.println("\nEMAIL ERROR------------------\n");
                    e.printStackTrace();
                    System.out.println("\n-----------------------------\n");

                    //on every exception, wait 10 seconds, just in case there is a traffic jam, or incase
                    //there is a problem causing every message to fail, there will not be a
                    //constant endless loop trying to send.
                    Thread.sleep(10000);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
