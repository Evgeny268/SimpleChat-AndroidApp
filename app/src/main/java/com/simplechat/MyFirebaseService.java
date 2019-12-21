package com.simplechat;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseService extends FirebaseMessagingService {
    public MyFirebaseService() {
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("inet", "From: " + remoteMessage.getFrom());
        if (remoteMessage.getNotification() != null) {
            Log.d("inet", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        if (remoteMessage.getData().size()>0){
            Log.d("inet", "Message Body: " + remoteMessage.getData());
            Map<String,String> data = remoteMessage.getData();
            String type = data.get("type");
            if (type!=null){
                if (type.equals("message")){
                    Resources resources = getResources();
                    Intent i = DialogActivity.newIntent(this);
                    PendingIntent pi = PendingIntent.getActivity(this,0,i,0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NotifWorker.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_logo)
                            .setContentTitle(data.get("who"))
                            .setContentText("text")
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentIntent(pi)
                            .setAutoCancel(true);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                    notificationManager.notify(2222,builder.build());
                }
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.e("inet", s);
        getSharedPreferences(AppUtils.APP_PREFERENCES,MODE_PRIVATE).edit().putString(AppUtils.APP_FIREBASE_TOKEN,s).apply();
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences(AppUtils.APP_PREFERENCES, MODE_PRIVATE).getString(AppUtils.APP_FIREBASE_TOKEN, "");
    }
}
