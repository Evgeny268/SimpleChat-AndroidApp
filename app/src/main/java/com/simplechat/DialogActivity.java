package com.simplechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import internet.NetMessager;
import transfers.Messages;
import transfers.TransferRequestAnswer;
import transfers.TransfersFactory;
import transfers.TypeRequestAnswer;
import transfers.User;

public class DialogActivity extends AppCompatActivity implements NetMessager.NewMessagesListener, TypeRequestAnswer {

    private User friend;
    private ArrayList<transfers.Message> messagesList = new ArrayList<>();
    private NetMessager netMessager;
    private Handler mHandler;
    private RecyclerView rvMessages;
    private MessageListAdapter messageListAdapter;
    private LinearLayoutManager layoutManager;
    private EditText editText;
    private ImageButton sendButton;
    private boolean newMessage = true;
    private int insertedNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        friend = (User)getIntent().getSerializableExtra("friend");
        editText = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSendMessage);
        rvMessages = findViewById(R.id.recyclerViewMessages);
        layoutManager = new LinearLayoutManager(this);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setHasFixedSize(false);
        messageListAdapter = new MessageListAdapter(this,messagesList,friend.iduser);
        rvMessages.setAdapter(messageListAdapter);
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==222){
                    Object o = msg.obj;
                    if (o instanceof Messages){
                        Messages messages = (Messages) o ;
                        addNewMessages(messages.messages);
                        messageListAdapter.setMessagesList(messagesList);
                        if (newMessage) {
                            messageListAdapter.notifyDataSetChanged();
                            rvMessages.scrollToPosition(messagesList.size() - 1);
                        }else {
                            messageListAdapter.notifyItemRangeInserted(0,insertedNum);
                        }
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
        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!rvMessages.canScrollVertically(-1)){
                    if (messagesList.size()>0){
                        Date lastDate = messagesList.get(0).date;
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String datestr = format.format(lastDate);
                        TransferRequestAnswer out = new TransferRequestAnswer(GET_MESSAGES,AppUtils.getLogin(), AppUtils.getPassword(),friend.iduser+","+datestr+","+20);
                        ObjectMapper objectMapper = new ObjectMapper();
                        StringWriter stringWriter = new StringWriter();
                        try {
                            objectMapper.writeValue(stringWriter,out);
                            netMessager.sendMessage(stringWriter.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                            //TODO не могу обновить сообщения
                        }
                    }
                }
            }
        });
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
            Message message = mHandler.obtainMessage(222, TransfersFactory.createTransfers(messages.get(0)));
            message.sendToTarget();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            rvMessages.scrollToPosition(messagesList.size() - 1);
        }
    }

    private void addNewMessages(ArrayList<transfers.Message> list){
        transfers.Message message = null;
        insertedNum = 0;
        if (messagesList.size()>0){
            message = messagesList.get(messagesList.size()-1);
        }
        for (int i = 0; i < list.size(); i++) {
            if (!messagesList.contains(list.get(i))){
                messagesList.add(list.get(i));
                insertedNum++;
            }
        }
        Collections.sort(messagesList);
        if (message != null){
            transfers.Message lastMessage = messagesList.get(messagesList.size()-1);
            if (message.compareTo(lastMessage)==-1){
                newMessage = true;
            }else {
                newMessage = false;
            }
        }else {
            newMessage = true;
        }
    }

    public void onClickSendButton(View view) {
        String text = editText.getText().toString();
        transfers.Message out = new transfers.Message(AppUtils.getLogin(),AppUtils.getPassword(),friend.iduser,text);
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        try {
            objectMapper.writeValue(stringWriter,out);
            netMessager.sendMessage(stringWriter.toString());
            editText.setText("");
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
