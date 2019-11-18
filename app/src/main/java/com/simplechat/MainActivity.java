package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView textVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textVersion = findViewById(R.id.textViewVersion);
        textVersion.setText(getResources().getString(R.string.textVersion)+" "+BuildConfig.VERSION_NAME);
        new Thread(){
            @Override
            public void run() {
                if (!AppUtils.isAlreadyConnect()){
                    if (!AppUtils.startConnect()){
                        System.out.println("No connection");
                    }
                }
            }
        }.start();
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
