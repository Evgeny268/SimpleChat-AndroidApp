package internet;

import android.util.Log;

import com.simplechat.AppUtils;

import java.util.ArrayList;

public class NetMessager implements InetWorker.MessageNotify {
    private InetWorker inetWorker;
    private NewMessagesListener newMessagesListener = null;
    private boolean waitMessageIncome = false;
    private static Object waitLock = new Object();

    public NetMessager(InetWorker inetWorker, NewMessagesListener newMessagesListener) {
        this.inetWorker = inetWorker;
        this.newMessagesListener = newMessagesListener;
        AppUtils.getInetWorker().setMessageNotify(this);
    }

    @Override
    public void messageIncome() {
        synchronized (waitLock){
            waitMessageIncome = false;
            waitLock.notify();
        }
        if (newMessagesListener!=null) {
            newMessagesListener.newMessage(InetWorker.getData());
        }
    }

    public void sendMessage(String message){
        try{
            inetWorker.send(message);
            Log.d("Inet","I SEND: "+message);
        }catch (Exception e){
            e.printStackTrace();
            ArrayList<String> list = new ArrayList<>();
            list.add("ERROR");
            if (newMessagesListener!=null) {
                newMessagesListener.newMessage(list);
            }
        }
    }

    public void stop(){
        newMessagesListener = null;
        inetWorker = null;
    }


    public void sendMessage(String message, int waitAnswerTime){
        if (waitMessageIncome){
            Log.d("Inet","НЕ ОТПРАВЛЯЕМ СООБЩЕНИЕ");
            return;
        }
        waitMessageIncome = true;
        Thread thread = new Thread(new Mwaiter(waitAnswerTime));
        thread.start();
        try{
            inetWorker.send(message);
            Log.d("Inet","I SEND: "+message);
        }catch (Exception e){
            e.printStackTrace();
            ArrayList<String> list = new ArrayList<>();
            list.add("ERROR");
            if (newMessagesListener!=null) {
                newMessagesListener.newMessage(list);
            }
        }
    }


    public interface NewMessagesListener{
        void newMessage(ArrayList<String> messages);
    }


    private class  Mwaiter implements Runnable{

        private int sleepTime;

        public Mwaiter(int sleepTime) {
            this.sleepTime = sleepTime;
        }

        @Override
        public void run() {
            try{
                synchronized (waitLock){
                    waitLock.wait(sleepTime);
                    Log.d("Inet","WAIT LOCK");
                }
                if (!waitMessageIncome){
                    return;
                }else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("TIMEOUT");
                    Log.d("Inet","TIMEOUT");
                    waitMessageIncome = false;
                    if (newMessagesListener!=null) {
                        newMessagesListener.newMessage(list);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            waitMessageIncome = false;
            Log.d("Inet","waitMessageIncome = false");
        }
    }
}
