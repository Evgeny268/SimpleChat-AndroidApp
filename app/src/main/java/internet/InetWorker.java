package internet;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class InetWorker extends WebSocketClient {
    public static Object lock = new Object();
    public static boolean newData = false;
    public static ArrayList<String> messages = new ArrayList<>();
    public InetWorker(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onMessage(String message) {
        synchronized (lock) {
            messages.add(message);
            newData = true;
            lock.notifyAll();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

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

}
