package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import internet.AuthWorker;
import internet.NetMessager;
import transfers.TransferRequestAnswer;
import transfers.Transfers;
import transfers.TransfersFactory;
import transfers.TypeRequestAnswer;

public class LogRegActivity extends AppCompatActivity implements NetMessager.NewMessagesListener, TypeRequestAnswer {

    public String mode;
    public TextView tvLogin;
    public TextView tvPassword;
    public TextView tvMode;
    public Button btnRegLog;
    public CheckBox cbAcceptLicense;
    private static boolean lock = false;
    private NetMessager netMessager;
    private Handler mHandler;
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
        unlockAll();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                if (message.obj!=null){
                    String text = (String)message.obj;
                    Toast.makeText(LogRegActivity.this, text, Toast.LENGTH_SHORT).show();
                }
                unlockAll();
            }
        };
    }

    @Override
    public void newMessage(ArrayList<String> messages) {
        String out = "";
        if (messages.size()>0){
            String text = messages.get(0);
            if (text.equals("TIMEOUT") || text.equals("ERROR")){
                out = getString(R.string.serverNotRespond);
            }else {
                Transfers transfers = TransfersFactory.createTransfers(text);
                if (transfers==null){
                    out = getString(R.string.error);
                }else {
                    if (transfers instanceof TransferRequestAnswer){
                        TransferRequestAnswer tra = (TransferRequestAnswer) transfers;
                        if (tra.request.equals(AUTHORIZATION_DONE) || tra.request.equals(REGISTRATION_DONE)){
                            startActivity(new Intent(LogRegActivity.this, FriendActivity.class));
                        }else if (tra.request.equals(BAD_LOGIN)){
                            out = getString(R.string.badLogin);
                        }else if (tra.request.equals(BAD_PASSWORD)){
                            out = getString(R.string.badPassword);
                        }else if (tra.request.equals(USER_ALREADY_EXIST)){
                            out = getString(R.string.userAlreadyExist);
                        }else if (tra.request.equals(USER_NOT_EXIST)){
                            out = getString(R.string.userNotExist);
                        }else if (tra.request.equals(WRONG_PASSWORD)){
                            out = getString(R.string.wrongPassword);
                        }
                    }else {
                        out = getString(R.string.error);
                    }
                }
            }
        }else {
            out = getString(R.string.error);
        }
        Message message;
        if (!out.equals("")){
            message = mHandler.obtainMessage(0,out);
        }else {
            message = mHandler.obtainMessage(1);
        }
        message.sendToTarget();
    }

    public void onlickAcceptLicense(View view) {
        if (cbAcceptLicense.isChecked()){
            btnRegLog.setEnabled(true);
        }else {
            btnRegLog.setEnabled(false);
        }
    }

    public void onClickSign(View view) {
        lockAll();
        AppUtils.setLogin(tvLogin.getText().toString());
        AppUtils.setPassword(tvPassword.getText().toString());
        TransferRequestAnswer out;
        if (mode.equals("signup")){
            out = new TransferRequestAnswer(REGISTRATION,AppUtils.getLogin(),AppUtils.getPassword());
        }else {
            out = new TransferRequestAnswer(AUTHORIZATION,AppUtils.getLogin(), AppUtils.getPassword());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        try {
            objectMapper.writeValue(stringWriter, out);
        }catch (IOException e){
            e.printStackTrace();
            unlockAll();
            return;
        }
        netMessager.sendMessage(stringWriter.toString(),7000);
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

    @Override
    protected void onStart() {
        super.onStart();
        netMessager = new NetMessager(AppUtils.getInetWorker(),this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        netMessager.stop();
    }
}
