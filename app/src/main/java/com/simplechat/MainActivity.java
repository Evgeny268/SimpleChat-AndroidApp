package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import internet.NetMessager;

import static com.simplechat.AppUtils.APP_PREFERENCES;

public class MainActivity extends AppCompatActivity {

    private TextView textVersion;
    private NetMessager netMessager = null;
    SharedPreferences mSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textVersion = findViewById(R.id.textViewVersion);
        textVersion.setText(getResources().getString(R.string.textVersion)+" "+BuildConfig.VERSION_NAME);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(AppUtils.APP_PREFERENCES_LOGIN) && mSettings.contains(AppUtils.APP_PREFERENCES_PASSWORD)){
            AppUtils.setLogin(mSettings.getString(AppUtils.APP_PREFERENCES_LOGIN,""));
            AppUtils.setPassword(mSettings.getString(AppUtils.APP_PREFERENCES_PASSWORD,""));
        }
        new Thread(){
            @Override
            public void run() {
                if (!AppUtils.isAlreadyConnect()){
                    if (!AppUtils.startConnect()){
                        System.out.println("No connection");
                    }else {
                        if (mSettings.contains(AppUtils.APP_PREFERENCES_LOGIN) && mSettings.contains(AppUtils.APP_PREFERENCES_PASSWORD)){
                            AppUtils.setLogin(mSettings.getString(AppUtils.APP_PREFERENCES_LOGIN,""));
                            AppUtils.setPassword(mSettings.getString(AppUtils.APP_PREFERENCES_PASSWORD,""));

                        }
                    }
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickLogIn(View view) {
        Intent intent = new Intent(MainActivity.this, LogRegActivity.class);
        intent.putExtra("mode","login");
        startActivity(intent);
    }

    public void onClickSignUp(View view) {
        Intent intent = new Intent(MainActivity.this, LogRegActivity.class);
        intent.putExtra("mode","signup");
        startActivity(intent);
    }
}
