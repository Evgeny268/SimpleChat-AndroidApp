package com.simplechat;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import internet.InetWorker;

public class AppUtils {
    private static InetWorker inetWorker;
    private static boolean alreadyConnect = false;
    public static boolean startConnect(){
        URL url = null;
        try {
            url = new URL("http://192.168.1.35:4444");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        URI uri = null;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        inetWorker = new InetWorker(uri);
        try {
            inetWorker.connect();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        long millis = System.currentTimeMillis();
        while (!inetWorker.isOpen()){
            if (System.currentTimeMillis()-millis>5000) {
                if (!inetWorker.isOpen()) return false;
                else return true;
            }
        }
        alreadyConnect = true;
        return true;
    }

    public static InetWorker getInetWorker() {
        return inetWorker;
    }

    public static boolean isAlreadyConnect() {
        return alreadyConnect;
    }

    public static void send(String text){
        if (!alreadyConnect) return;
        try {
            inetWorker.send(text);
        }catch (Exception e){
            if (startConnect()){
                alreadyConnect = true;
                try {
                    inetWorker.send(text);
                }catch (Exception ex){
                    throw ex;
                }
            }
        }
    }
}
