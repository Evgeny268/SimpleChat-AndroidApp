package internet;


import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class InetWorker extends WebSocketClient {
    public static Object lock = new Object();
    public static boolean newData = false;
    public static ArrayList<String> messages = new ArrayList<>();
    private MessageNotify messageNotify = null;
    public InetWorker(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onMessage(String message) {
        Log.d("Inet","ОЖИДАНИЕ ОТКРЫТИЯ ОБЪЕКТА");
        synchronized (lock) {
            Log.d("Inet","ОТКРЫТИЕ ОБЪЕКТА");
            System.out.println(message);
            messages.add(message);
            Log.d("Inet","ДОБАВЛЕНО НОВОЕ ПИСЬМО");
            newData = true;
            lock.notifyAll();
            if (messageNotify != null){
                messageNotify.messageIncome();
            }
        }
    }

    public void setMessageNotify(MessageNotify messageNotify) {
        this.messageNotify = messageNotify;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d("Inet","ЗАКРЫТИЕ СОЕДИНЕНИЯ");
    }

    @Override
    public void onError(Exception ex) {

    }

    public static ArrayList<String> getData(){
        ArrayList<String> data;
        synchronized (lock){
            data = new ArrayList<>(messages);
            messages.clear();
            newData = false;
        }
        return data;
    }

    public interface MessageNotify{
        void messageIncome();
    }

}
