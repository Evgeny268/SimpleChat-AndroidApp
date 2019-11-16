package internet;

import java.util.ArrayList;

public class NetMessager {
    private InetWorker inetWorker;
    private NewMessagesListener newMessagesListener;
    private Thread mWorkerThread;
    private String sendMessage = "";
    private boolean waitMessageIncome;

    public NetMessager(InetWorker inetWorker, NewMessagesListener newMessagesListener) {
        this.inetWorker = inetWorker;
        this.newMessagesListener = newMessagesListener;
        mWorkerThread = new Thread(new MWorker());
        mWorkerThread.start();
    }

    public void sendMessage(String message, boolean waitAnswer){
        try{
            inetWorker.send(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendMessage(String message, int waitAnswerTime){
        waitMessageIncome = false;
        Thread thread = new Thread(new Mwaiter(waitAnswerTime));
        thread.start();
        try{
            inetWorker.send(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop(){
        mWorkerThread.interrupt();
        sendMessage = "";
    }

    public interface NewMessagesListener{
        void newMessage(ArrayList<String> messages);
    }

    private class MWorker implements Runnable{

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                synchronized (InetWorker.lock){
                    try {
                        InetWorker.lock.wait();
                        waitMessageIncome = true;
                        newMessagesListener.newMessage(InetWorker.getData());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class  Mwaiter implements Runnable{

        private int sleepTime;

        public Mwaiter(int sleepTime) {
            this.sleepTime = sleepTime;
        }

        @Override
        public void run() {
            try{
                Thread.sleep(sleepTime);
                if (waitMessageIncome){
                    return;
                }else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("TIMEOUT");
                    newMessagesListener.newMessage(list);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
