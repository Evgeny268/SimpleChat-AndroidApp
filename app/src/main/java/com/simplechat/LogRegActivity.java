package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import internet.AuthWorker;

public class LogRegActivity extends AppCompatActivity {

    public String mode;
    public TextView tvLogin;
    public TextView tvPassword;
    public TextView tvMode;
    public Button btnRegLog;
    public CheckBox cbAcceptLicense;
    private static boolean lock = false;
    //TODO Добавить проверку логина и пароля
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_reg);
        tvLogin = findViewById(R.id.editTextLogin);
        tvPassword = findViewById(R.id.editTextPassword);
        tvMode = findViewById(R.id.textViewMode);
        btnRegLog = findViewById(R.id.buttonRegLog);
        cbAcceptLicense = findViewById(R.id.checkBoxLicense);
        mode = getIntent().getStringExtra("mode");
        if (mode.equals("signup")){
            tvMode.setText(R.string.registration);
            btnRegLog.setText(R.string.sign_up);
        }else {
            tvMode.setText(R.string.authorization);
            btnRegLog.setText(R.string.log_in);
        }
    }

    public void onlickAcceptLicense(View view) {
        if (cbAcceptLicense.isChecked()){
            btnRegLog.setEnabled(true);
        }else {
            btnRegLog.setEnabled(false);
        }
    }

    public void onClickSign(View view) {
        AuthWorker authWorker = new AuthWorker(this);
        lockAll();
        authWorker.execute();
    }

    public void goFriendActivity(){
        Intent intent = new Intent(LogRegActivity.this, FriendActivity.class);
        startActivity(intent);
    }

    public void lockAll(){
        lock = true;
        btnRegLog.setEnabled(false);
        cbAcceptLicense.setEnabled(false);
        tvLogin.setEnabled(false);
        tvPassword.setEnabled(false);
    }

    public void unlockAll(){
        lock = false;
        if (cbAcceptLicense.isChecked()){
            btnRegLog.setEnabled(true);
        }
        cbAcceptLicense.setEnabled(true);
        tvLogin.setEnabled(true);
        tvPassword.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if (!lock) {
            super.onBackPressed();
        }
    }
}
