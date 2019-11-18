package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import internet.InetWorker;
import internet.NetMessager;
import transfers.TransferRequestAnswer;
import transfers.Transfers;
import transfers.TransfersFactory;
import transfers.TypeRequestAnswer;

public class AddFriendActivity extends AppCompatActivity implements NetMessager.NewMessagesListener,TypeRequestAnswer {

    private EditText etFriendLogin;
    private Button bAddFriend;
    private Handler mHandler;
    private NetMessager netMessager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        etFriendLogin = findViewById(R.id.editTextAddFriendLogin);
        bAddFriend = findViewById(R.id.buttonAddFriend);
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==0){
                    Toast.makeText(AddFriendActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                    unblockInterface();
                }else if (msg.what==1){
                    unblockInterface();
                    Toast.makeText(AddFriendActivity.this, getString(R.string.applicationSent), Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }else if (msg.what==2){
                    unblockInterface();
                    Toast.makeText(AddFriendActivity.this, getString(R.string.userNotExist), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }



    @Override
    public void newMessage(ArrayList<String> messages) {
        Message message = mHandler.obtainMessage(0);
        if (messages.size()>0){
            String data = messages.get(0);
            if (data.equals(ERROR)){
                message.sendToTarget();
                return;
            }
            Transfers transfers = TransfersFactory.createTransfers(data);
            if (transfers != null){
                if (transfers instanceof TransferRequestAnswer){
                    TransferRequestAnswer tra = (TransferRequestAnswer) transfers;
                    if (tra.request.equals(ERROR)){
                        message = mHandler.obtainMessage(0);
                    }else if(tra.request.equals(REQUEST_SENT)){
                        message = mHandler.obtainMessage(1);
                    }else if (tra.request.equals(USER_NOT_EXIST)){
                        message = mHandler.obtainMessage(2);
                    }
                }
            }else {
                message = mHandler.obtainMessage(0);
            }
        }else{
            message = mHandler.obtainMessage(0);
        }
        message.sendToTarget();
    }

    protected void blockInterface(){
        bAddFriend.setEnabled(false);
        etFriendLogin.setEnabled(false);
    }

    protected void unblockInterface(){
        bAddFriend.setEnabled(true);
        etFriendLogin.setEnabled(true);
    }

    public void onClickAddFriend(View view) {
        String login = etFriendLogin.getText().toString();
        if (login.length()==0){
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.enterFriendName),Toast.LENGTH_SHORT).show();
        }else {
            blockInterface();
            TransferRequestAnswer out = new TransferRequestAnswer(ADD_FRIEND,AppUtils.getLogin(),AppUtils.getPassword(),login);
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter stringWriter = new StringWriter();
            try {
                objectMapper.writeValue(stringWriter,out);
                netMessager.sendMessage(stringWriter.toString(),4000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        netMessager = new NetMessager(AppUtils.getInetWorker(), this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        netMessager.stop();
    }


}
