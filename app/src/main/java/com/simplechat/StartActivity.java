package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {

    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        retryButton = findViewById(R.id.buttonRetryConnect);
        AppUtils.loadTokenLogAndPass(this);
        NotifWorker.createNotificationChannel(this); //Init notification channel
        StartConnect startConnect = new StartConnect();
        startConnect.execute();
    }

    public void onClickRetryConnect(View view) {
        retryButton.setEnabled(false);
        retryButton.setVisibility(View.INVISIBLE);
        StartConnect startConnect = new StartConnect();
        startConnect.execute();
    }

    private class StartConnect extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            if (!AppUtils.isAlreadyConnect()){
                AppUtils.startConnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!AppUtils.isAlreadyConnect()){
                Toast.makeText(StartActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                retryButton.setVisibility(View.VISIBLE);
                retryButton.setEnabled(true);
            }else {
                if (AppUtils.getLogin().equals("") || AppUtils.getPassword().equals("")){
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                }else {
                    startActivity(new Intent(StartActivity.this, FriendActivity.class));
                }
            }
        }
    }
}
