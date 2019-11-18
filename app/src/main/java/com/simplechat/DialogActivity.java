package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import internet.NetMessager;
import transfers.Messages;
import transfers.TransferRequestAnswer;
import transfers.TypeRequestAnswer;
import transfers.User;

public class DialogActivity extends AppCompatActivity implements NetMessager.NewMessagesListener, TypeRequestAnswer {

    private User friend;
    private ArrayList<transfers.Message> messagesList = new ArrayList<>();
    private NetMessager netMessager;
    private Handler mHandler;
    private RecyclerView rvMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        friend = (User)getIntent().getSerializableExtra("friend");
        rvMessages = findViewById(R.id.recyclerViewMessages);
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==222){
                    Object o = msg.obj;
                    if (o instanceof Messages){
                        Messages messages = (Messages) o ;
                        addNewMessages(messages.messages);
                        //TODO Обновить адаптер
                    }else if (o instanceof TransferRequestAnswer){
                        TransferRequestAnswer tra = (TransferRequestAnswer)o;
                        if (tra.request.equals(NEW_MESSAGE)){
                            TransferRequestAnswer out = new TransferRequestAnswer(GET_MESSAGES,AppUtils.getLogin(), AppUtils.getPassword(),friend.iduser+","+20);
                            ObjectMapper objectMapper = new ObjectMapper();
                            StringWriter stringWriter = new StringWriter();
                            try {
                                objectMapper.writeValue(stringWriter,out);
                                netMessager.sendMessage(stringWriter.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        netMessager = new NetMessager(AppUtils.getInetWorker(),this);
        TransferRequestAnswer out = new TransferRequestAnswer(GET_MESSAGES,AppUtils.getLogin(), AppUtils.getPassword(),friend.iduser+","+20);
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        try {
            objectMapper.writeValue(stringWriter,out);
            netMessager.sendMessage(stringWriter.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        netMessager.stop();
    }

    @Override
    public void newMessage(ArrayList<String> messages) {
        if (messages.size()>0){
            Message message = mHandler.obtainMessage(222,messages.get(0));
        }
    }

    private void addNewMessages(ArrayList<transfers.Message> list){
        for (int i = 0; i < list.size(); i++) {
            if (!messagesList.contains(list.get(i))){
                messagesList.add(list.get(i));
            }
        }
        Collections.sort(messagesList);
    }
}
