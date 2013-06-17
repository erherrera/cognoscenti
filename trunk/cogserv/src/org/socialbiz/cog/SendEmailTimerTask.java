package org.socialbiz.cog;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

public class SendEmailTimerTask extends TimerTask {

    private final static long EVERY_TWO_HOURS = 1000*60*60*2;

    private static SendEmailTimerTask sendEmailSingleton = null;
    public static Exception threadLastCheckException = null;

    private SendEmailTimerTask() throws Exception{
        EmailRecordMgr.initializeEmailRecordMgr();
    }

    public static void initEmailSender() throws Exception
    {
        if(sendEmailSingleton != null){
            return;
            //throw new Exception("Try to create duplicate instance of SendEmail Singleton class.");
        }
        sendEmailSingleton = new SendEmailTimerTask();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(sendEmailSingleton, 60000, EVERY_TWO_HOURS);
    }

    @Override
    public void run() {
        try
        {
            //I am not sure why we have to do this, and why we need this class in the first place
            //since there is another class 'SendEmailThread' that does the same thing.
            EmailRecordMgr.triggerNextMessageSend();
        }
        catch(Exception e)
        {
            Exception failure = new Exception("Failure in the SendEmail thread run method. Thread died.", e);
            failure.printStackTrace();
            threadLastCheckException = failure;
        }
    }



    public static ArrayBlockingQueue<EmailRecord> blq = new ArrayBlockingQueue<EmailRecord>(1000);

}
