package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import internet.InetWorker;
import internet.NetMessager;
import transfers.Friends;
import transfers.TransferRequestAnswer;
import transfers.Transfers;
import transfers.TransfersFactory;
import transfers.TypeRequestAnswer;
import transfers.User;

public class FriendActivity extends AppCompatActivity implements FriendsRVAdapter.OnFriendsListListener, FriendsRVAdapter.OnFriendListLClickListener, NetMessager.NewMessagesListener, TypeRequestAnswer {

    private static ArrayList<User> friends = new ArrayList<>();
    private RecyclerView friendList;
    private FriendsRVAdapter friendsRVAdapter;
    private NetMessager netMessager;
    private Handler mHandler;
    LinearLayoutManager layoutManager;
    DividerItemDecoration dividerItemDecoration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        Toolbar toolbar = findViewById(R.id.toolbar_friend_activity);
        setSupportActionBar(toolbar);
        friendList = findViewById(R.id.rv_friends);
        layoutManager = new LinearLayoutManager(this);
        friendList.setLayoutManager(layoutManager);
        friendList.setHasFixedSize(false);
        dividerItemDecoration = new DividerItemDecoration(friendList.getContext(),layoutManager.getOrientation());
        friendList.addItemDecoration(dividerItemDecoration);
        friendsRVAdapter = new FriendsRVAdapter(friends,FriendActivity.this,FriendActivity.this);
        friendList.setAdapter(friendsRVAdapter);
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==0){
                    Toast.makeText(FriendActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                }else if (msg.what==1){
                    friendsRVAdapter.setFriends(friends);
                    friendsRVAdapter.notifyDataSetChanged();
                }else if (msg.what == 2){
                    TransferRequestAnswer out = new TransferRequestAnswer(GET_FRIENDS,AppUtils.getLogin(),AppUtils.getPassword());
                    ObjectMapper objectMapper = new ObjectMapper();
                    StringWriter stringWriter = new StringWriter();
                    try {
                        objectMapper.writeValue(stringWriter,out);
                        netMessager.sendMessage(stringWriter.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        netMessager = new NetMessager(AppUtils.getInetWorker(),this);
        TransferRequestAnswer out = new TransferRequestAnswer(GET_FRIENDS,AppUtils.getLogin(),AppUtils.getPassword());
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        try {
            objectMapper.writeValue(stringWriter,out);
            netMessager.sendMessage(stringWriter.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        netMessager.stop();
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
            if (transfers!=null){
                if (transfers instanceof Friends){
                   Friends inputFriends = (Friends) transfers;
                   friends = inputFriends.friends;
                   message = mHandler.obtainMessage(1);
                   message.sendToTarget();
                }else if (transfers instanceof TransferRequestAnswer){
                    TransferRequestAnswer tra = (TransferRequestAnswer) transfers;
                    if (tra.request.equals(ERROR)){
                        message = mHandler.obtainMessage(0);
                        message.sendToTarget();
                    }else if (tra.request.equals(REMOVE_FRIEND)){
                        message = mHandler.obtainMessage(2);
                        message.sendToTarget();
                    }else if (tra.request.equals(REQUEST_SENT)){
                        message = mHandler.obtainMessage(2);
                        message.sendToTarget();
                    }else if (tra.request.equals(NEW_REQUEST)){
                        message = mHandler.obtainMessage(2);
                        message.sendToTarget();
                    }
                }
            }
        }else {
            message = mHandler.obtainMessage(0);
            message.sendToTarget();
        }
    }

    public FriendActivity getFriendActivity(){
        return this;
    }

    @Override
    public void onFriendClick(int position, View v) {
        User friend = friends.get(position);
        Intent intent = new Intent(FriendActivity.this, DialogActivity.class);
        intent.putExtra("friend", friend);
        startActivity(intent);
    }

    @Override
    public void onFriendClickLong(int position, View v) {
        showPopupMenu(v, position);
    }

    private void showPopupMenu(View v, final int position){
        PopupMenu popupMenu = new PopupMenu(this,v);
        popupMenu.inflate(R.menu.friendlist_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.deleteFriend:
                        User deleteFriend = friends.get(position);
                        TransferRequestAnswer out = new TransferRequestAnswer(REMOVE_FRIEND,AppUtils.getLogin(),AppUtils.getPassword(),deleteFriend.login);
                        ObjectMapper objectMapper = new ObjectMapper();
                        StringWriter stringWriter = new StringWriter();
                        try {
                            objectMapper.writeValue(stringWriter,out);
                            netMessager.sendMessage(stringWriter.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                }
                return false;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {

            }
        });
        popupMenu.show();
    }

    public void onClickAddFriend(View view) {
        startActivity(new Intent(FriendActivity.this, AddFriendActivity.class));
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friend_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.friend_request:
                startActivity(new Intent(FriendActivity.this, InputRequestActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
