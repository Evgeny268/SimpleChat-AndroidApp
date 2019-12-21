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
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;

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
    public TextView licenseText;
    public Button btnRegLog;
    public CheckBox cbAcceptLicense;
    private static boolean lock = false;
    private NetMessager netMessager;
    private Handler mHandler;
    private volatile String log;
    private volatile String pas;
    private String tokenLog;
    private String tokenPas;
    //TODO Добавить проверку логина и пароля
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_reg);
        tvLogin = findViewById(R.id.editTextLogin);
        tvPassword = findViewById(R.id.editTextPassword);
        tvMode = findViewById(R.id.textViewMode);
        btnRegLog = findViewById(R.id.buttonRegLog);
        licenseText = findViewById(R.id.textViewAcceptLicense);
        cbAcceptLicense = findViewById(R.id.checkBoxLicense);
        mode = getIntent().getStringExtra("mode");
        if (mode.equals("signup")){
            tvMode.setText(R.string.registration);
            btnRegLog.setText(R.string.sign_up);
        }else {
            tvMode.setText(R.string.authorization);
            btnRegLog.setText(R.string.log_in);
            cbAcceptLicense.setEnabled(false);
            cbAcceptLicense.setVisibility(View.INVISIBLE);
            licenseText.setVisibility(View.INVISIBLE);
        }
        unlockAll();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                unlockAll();
                if (message.obj!=null){
                    String text = (String)message.obj;
                    Transfers transfers = TransfersFactory.createTransfers(text);
                    if (transfers != null){
                        if (transfers instanceof TransferRequestAnswer){
                            if (((TransferRequestAnswer) transfers).request.equals(AUTHORIZATION_DONE)){
                                if (((TransferRequestAnswer) transfers).extra!=null){
                                    AppUtils.saveTokenLogAndPass(LogRegActivity.this,((TransferRequestAnswer) transfers).extra,tokenPas);
                                    log = "";
                                    pas = "";
                                    startActivity(new Intent(LogRegActivity.this,FriendActivity.class));
                                }
                            }else if (((TransferRequestAnswer) transfers).request.equals(REGISTRATION_DONE)){
                                lockAll();
                                log = tvLogin.getText().toString();
                                pas = tvPassword.getText().toString();
                                tokenPas = generateSafeToken(129);
                                TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION,log, pas,tokenPas);
                                try {
                                    netMessager.sendMessage(AppUtils.objToJson(out),7000);
                                } catch (IOException e) {
                                    unlockAll();
                                }
                            }else if (((TransferRequestAnswer) transfers).request.equals(BAD_LOGIN)){
                                Toast.makeText(LogRegActivity.this, getString(R.string.badLogin), Toast.LENGTH_SHORT).show();
                            }else if (((TransferRequestAnswer) transfers).request.equals(BAD_PASSWORD)){
                                Toast.makeText(LogRegActivity.this, getString(R.string.badPassword), Toast.LENGTH_SHORT).show();
                            }else if (((TransferRequestAnswer) transfers).request.equals(USER_ALREADY_EXIST)){
                                Toast.makeText(LogRegActivity.this, getString(R.string.userAlreadyExist), Toast.LENGTH_SHORT).show();
                            }else if (((TransferRequestAnswer) transfers).request.equals(USER_NOT_EXIST)){
                                Toast.makeText(LogRegActivity.this, getString(R.string.userNotExist), Toast.LENGTH_SHORT).show();
                            }else if (((TransferRequestAnswer) transfers).request.equals(WRONG_PASSWORD)){
                                Toast.makeText(LogRegActivity.this, getString(R.string.wrongPassword), Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(LogRegActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }else {
                        Toast.makeText(LogRegActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(LogRegActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
                unlockAll();
            }
        };
    }

    @Override
    public void newMessage(ArrayList<String> messages) {
        String out = "";
        if (messages.size()>0) {
            Message message = mHandler.obtainMessage(0, messages.get(0));
            message.sendToTarget();
//            String text = messages.get(0);
//            if (text.equals("TIMEOUT") || text.equals("ERROR")){
//                out = getString(R.string.serverNotRespond);
//            }else {
//                Transfers transfers = TransfersFactory.createTransfers(text);
//                if (transfers==null){
//                    out = getString(R.string.error);
//                }else {
//                    if (transfers instanceof TransferRequestAnswer){
//                        TransferRequestAnswer tra = (TransferRequestAnswer) transfers;
//                        if (tra.request.equals(AUTHORIZATION_DONE) || tra.request.equals(REGISTRATION_DONE)){
//                            startActivity(new Intent(LogRegActivity.this, FriendActivity.class));
//                        }else if (tra.request.equals(BAD_LOGIN)){
//                            out = getString(R.string.badLogin);
//                        }else if (tra.request.equals(BAD_PASSWORD)){
//                            out = getString(R.string.badPassword);
//                        }else if (tra.request.equals(USER_ALREADY_EXIST)){
//                            out = getString(R.string.userAlreadyExist);
//                        }else if (tra.request.equals(USER_NOT_EXIST)){
//                            out = getString(R.string.userNotExist);
//                        }else if (tra.request.equals(WRONG_PASSWORD)){
//                            out = getString(R.string.wrongPassword);
//                        }
//                    }else {
//                        out = getString(R.string.error);
//                    }
//                }
//            }
//        }else {
//            out = getString(R.string.error);
//        }
//        Message message;
//        if (!out.equals("")){
//            message = mHandler.obtainMessage(0,out);
//        }else {
//            message = mHandler.obtainMessage(1);
//        }
//        message.sendToTarget();
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
        lockAll();
        log = tvLogin.getText().toString();
        pas = tvPassword.getText().toString();
        TransferRequestAnswer out;
        if (mode.equals("signup")){
            out = new TransferRequestAnswer(REGISTRATION,log,pas);
        }else {
            tokenPas = generateSafeToken(129);
            out = new TransferRequestAnswer(AUTHORIZATION,log, pas,tokenPas);
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
        tvLogin.setEnabled(false);
        tvPassword.setEnabled(false);
        if (mode.equals("signup")){
            cbAcceptLicense.setEnabled(false);
        }
    }

    public void unlockAll(){
        lock = false;
        tvLogin.setEnabled(true);
        tvPassword.setEnabled(true);
        if (mode.equals("signup")) {
            if (cbAcceptLicense.isChecked()) {
                btnRegLog.setEnabled(true);
            }
            cbAcceptLicense.setEnabled(true);
        }else {
            btnRegLog.setEnabled(true);
        }
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

    public static String generateSafeToken(int size) {
        SecureRandom random = new SecureRandom();
        StringBuilder returnValue = new StringBuilder(size);
        final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < size; i++) {
            returnValue.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return new String(returnValue);
    }
}
