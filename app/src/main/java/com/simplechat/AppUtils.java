package com.simplechat;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import internet.InetWorker;

public class AppUtils {
    private static InetWorker inetWorker;
    private static boolean alreadyConnect = false;
    private static String login = null;
    private static String password = null;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_LOGIN = "Nickname";
    public static final String APP_PREFERENCES_PASSWORD = "Password";
    public static boolean startConnect(){
        URL url = null;
        try {
            //url = new URL("http://192.168.1.35:4444");
            //url = new URL("http://127.0.0.1:4444");
            url = new URL("http://ecombine.ddns.net:4444");
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
            Log.d("SCinet","Выслал данные");
        }catch (Exception e){
            Log.d("SCinet","Что-то пошло не так");
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

    public static String getLogin() {
        return login;
    }

    public static void setLogin(String login) {
        AppUtils.login = login;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        AppUtils.password = password;
    }
}
