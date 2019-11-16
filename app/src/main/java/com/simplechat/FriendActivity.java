package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import transfers.Friends;
import transfers.TransferRequestAnswer;
import transfers.TypeRequestAnswer;
import transfers.User;

public class FriendActivity extends AppCompatActivity implements FriendsRVAdapter.OnFriendsListListener, FriendsRVAdapter.OnFriendListLClickListener {

    private static ArrayList<User> friends = new ArrayList<>();
    private RecyclerView friendList;
    private FriendsRVAdapter friendsRVAdapter;
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
        FriendInetWorker fWorker = new FriendInetWorker();
        fWorker.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO обновить данные с сервера
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
        showPopupMenu(v);
    }

    private void showPopupMenu(View v){
        PopupMenu popupMenu = new PopupMenu(this,v);
        popupMenu.inflate(R.menu.friendlist_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
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

    private class FriendInetWorker extends AsyncTask<Void,String,Void> implements TypeRequestAnswer {

        @Override
        protected Void doInBackground(Void... voids) {
            if (!AppUtils.isAlreadyConnect()){
                if (!AppUtils.startConnect()){
                    publishProgress(getResources().getString(R.string.serverNotRespond));
                    return null;
                }
            }

            TransferRequestAnswer out = new TransferRequestAnswer(GET_FRIENDS,AppUtils.getLogin(),AppUtils.getPassword());
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter stringWriter = new StringWriter();

            try {
                objectMapper.writeValue(stringWriter,out);
                AppUtils.send(stringWriter.toString());
            } catch (Exception e) {
                publishProgress(getResources().getString(R.string.error));
                return null;
            }

            ArrayList<String> data;
            synchronized (InetWorker.lock){
                if (!InetWorker.newData){
                    try {
                        InetWorker.lock.wait(5000);
                    } catch (InterruptedException e) {
                        publishProgress(getResources().getString(R.string.error));
                        return null;
                    }
                }
            }
            data = InetWorker.getData();

            if (data.size()==0){
                publishProgress(getResources().getString(R.string.error));
                return null;
            }
            ObjectNode node = null;
            try {
                node = new ObjectMapper().readValue(data.get(data.size()-1),ObjectNode.class);
            } catch (IOException e) {
                publishProgress(getResources().getString(R.string.error));
                return null;
            }

            if (node.has("type")){
                if (node.get("type").asText().equals("."+ Friends.class.getSimpleName())){
                    try {
                        Friends friendIn = (Friends)objectMapper.readValue(data.get(data.size()-1),Friends.class);
                        friends = friendIn.friends;
                    } catch (IOException e) {
                        e.printStackTrace();
                        publishProgress(getResources().getString(R.string.error));
                        return null;
                    }
                }else {
                    publishProgress(getResources().getString(R.string.error));
                    return null;
                }
            }else {
                publishProgress(getResources().getString(R.string.error));
                return null;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            for (int i = 0; i < values.length; i++){
                Toast.makeText(getApplicationContext(), values[i], Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            friendsRVAdapter = new FriendsRVAdapter(friends,getFriendActivity(),getFriendActivity());
            friendList.setAdapter(friendsRVAdapter);
        }
    }
}
