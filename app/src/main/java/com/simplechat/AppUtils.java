package com.simplechat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import internet.InetWorker;

public class AppUtils {
    private static InetWorker inetWorker;
    private static boolean alreadyConnect = false;
    private static String login = "";
    private static String password = "";
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_LOGIN = "Nickname";
    public static final String APP_PREFERENCES_PASSWORD = "Password";
    public static final String APP_FIREBASE_TOKEN = "ftoken";
    public static boolean startConnect(){
        URL url = null;
        try {
            url = new URL("http://192.168.1.35:4444");
            //url = new URL("http://127.0.0.1:4444");
            //url = new URL("http://ecombine.ddns.net:4444");
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

    public static void saveTokenLogAndPass(Context context, String tokenLog, String tokenPass){
        SharedPreferences mSetting = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSetting.edit();
        editor.putString(APP_PREFERENCES_LOGIN,tokenLog);
        editor.putString(APP_PREFERENCES_PASSWORD,tokenPass);
        editor.apply();
    }

    public static void loadTokenLogAndPass(Context context){
        SharedPreferences mSetting = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        login = "";
        password = "";
        if (mSetting.contains(APP_PREFERENCES_LOGIN)){
            login = mSetting.getString(APP_PREFERENCES_LOGIN,"");
        }
        if (mSetting.contains(APP_PREFERENCES_PASSWORD)){
            password = mSetting.getString(APP_PREFERENCES_PASSWORD,"");
        }
    }

    public static void resetAll(Context context){
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        }catch (Exception e){
            Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
            return;
        }
        inetWorker.close();
        saveTokenLogAndPass(context,"","");
        context.startActivity(new Intent(context,StartActivity.class));

    }

    public static String objToJson(Object c) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter,c);
        return stringWriter.toString();
    }
}
