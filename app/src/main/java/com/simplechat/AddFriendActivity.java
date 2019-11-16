package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import transfers.TransferRequestAnswer;
import transfers.TypeRequestAnswer;

public class AddFriendActivity extends AppCompatActivity {

    private EditText etFriendLogin;
    private Button bAddFriend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        etFriendLogin = findViewById(R.id.editTextAddFriendLogin);
        bAddFriend = findViewById(R.id.buttonAddFriend);
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
            FriendAdderTask friendAdderTask = new FriendAdderTask();
            friendAdderTask.execute(login);
        }
    }

    private class FriendAdderTask extends AsyncTask<String, String, Void > implements TypeRequestAnswer {

        private boolean request_send = false;

        @Override
        protected void onPreExecute() {
            blockInterface();
        }


        @Override
        protected Void doInBackground(String... strings) {
            request_send = false;
            String login = strings[0];

            if (!AppUtils.isAlreadyConnect()){
                if (!AppUtils.startConnect()){
                    publishProgress(getResources().getString(R.string.serverNotRespond));
                    return null;
                }
            }

            TransferRequestAnswer out = new TransferRequestAnswer(ADD_FRIEND,AppUtils.getLogin(),AppUtils.getPassword(),login);
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter stringWriter = new StringWriter();
            try {
                objectMapper.writeValue(stringWriter,out);
                Log.d("SCinet","Готов высылать");
                AppUtils.send(stringWriter.toString());
                Log.d("SCinet","выслал");
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
                if (node.get("type").asText().equals("."+TransferRequestAnswer.class.getSimpleName())){
                    try{
                        TransferRequestAnswer in = (TransferRequestAnswer)objectMapper.readValue(data.get(data.size()-1),TransferRequestAnswer.class);
                        if (in.request==null){
                            publishProgress(getResources().getString(R.string.error));
                            return null;
                        }
                        if (in.request.equals(REQUEST_SENT)){
                            publishProgress(getResources().getString(R.string.applicationSent));
                            request_send = true;
                            return null;
                        }else if (in.request.equals(USER_NOT_EXIST)){
                            publishProgress(getResources().getString(R.string.userNotExist));
                            return null;
                        }else {
                            publishProgress(getResources().getString(R.string.error));
                            return null;
                        }
                    }catch (IOException e) {
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
        }

        @Override
        protected void onProgressUpdate(String... values) {
            for (int i = 0; i < values.length; i++) {
                Toast.makeText(getApplicationContext(), values[i], Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            unblockInterface();
            if (request_send){
                startActivity(new Intent(AddFriendActivity.this, FriendActivity.class));
            }
        }


        @Override
        protected void onCancelled() {
            unblockInterface();
        }
    }

}
