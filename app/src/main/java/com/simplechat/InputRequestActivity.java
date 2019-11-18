package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import internet.InetWorker;
import internet.NetMessager;
import transfers.RequestIn;
import transfers.TransferRequestAnswer;
import transfers.Transfers;
import transfers.TransfersFactory;
import transfers.TypeRequestAnswer;
import transfers.User;

public class InputRequestActivity extends AppCompatActivity implements InFriendsAdapter.RequestInControl, NetMessager.NewMessagesListener, TypeRequestAnswer {

    private static ArrayList<User> requests = new ArrayList<>();
    private RecyclerView requestList;
    private InFriendsAdapter inFriendsAdapter;
    private LinearLayoutManager layoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private NetMessager netMessager;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_request);
        requestList = findViewById(R.id.requestList);
        layoutManager = new LinearLayoutManager(this);
        requestList.setLayoutManager(layoutManager);
        requestList.setHasFixedSize(false);
        dividerItemDecoration = new DividerItemDecoration(requestList.getContext(),layoutManager.getOrientation());
        requestList.addItemDecoration(dividerItemDecoration);
        inFriendsAdapter = new InFriendsAdapter(requests,getInputRequestActivity());
        requestList.setAdapter(inFriendsAdapter);
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==0){
                    Toast.makeText(InputRequestActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                }else if (msg.what==1){
                    inFriendsAdapter.setRequests(requests);
                    inFriendsAdapter.notifyDataSetChanged();
                }else if (msg.what==2){
                    TransferRequestAnswer out = new TransferRequestAnswer(GET_REQUEST_IN,AppUtils.getLogin(),AppUtils.getPassword());
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
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        netMessager = new NetMessager(AppUtils.getInetWorker(), this);
        TransferRequestAnswer out = new TransferRequestAnswer(GET_REQUEST_IN,AppUtils.getLogin(),AppUtils.getPassword());
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
    protected void onStop() {
        super.onStop();
        netMessager.stop();
    }

    @Override
    public void newMessage(ArrayList<String> messages) {
        Message message = mHandler.obtainMessage(0);
        if (messages.size()>0){
            String data = messages.get(0);
            Transfers transfers = TransfersFactory.createTransfers(data);
            if (transfers !=null){
                if (transfers instanceof RequestIn){
                    RequestIn requestIn = (RequestIn) transfers;
                    requests = requestIn.users;
                    message = mHandler.obtainMessage(1);
                    message.sendToTarget();
                }else if (transfers instanceof TransferRequestAnswer){
                    TransferRequestAnswer transferRequestAnswer = (TransferRequestAnswer) transfers;
                    if (transferRequestAnswer.request.equals(REQUEST_SENT)){
                        message = mHandler.obtainMessage(2);
                        message.sendToTarget();
                    }else if (transferRequestAnswer.request.equals(REMOVE_FRIEND)){
                        message = mHandler.obtainMessage(2);
                        message.sendToTarget();
                    }else if (transferRequestAnswer.request.equals(ERROR)){
                        message = mHandler.obtainMessage(0);
                        message.sendToTarget();
                    }
                }
            }else {
                message.sendToTarget();
            }
        }else {
            message.sendToTarget();
        }
    }

    public InputRequestActivity getInputRequestActivity(){
        return this;
    }
    @Override
    public void onClickAccept(User user) {
        TransferRequestAnswer out = new TransferRequestAnswer(ADD_FRIEND,AppUtils.getLogin(), AppUtils.getPassword(),user.login);
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
    public void onClickReject(User user) {
        TransferRequestAnswer out = new TransferRequestAnswer(REMOVE_FRIEND,AppUtils.getLogin(), AppUtils.getPassword(), user.login);
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        try {
            objectMapper.writeValue(stringWriter,out);
            netMessager.sendMessage(stringWriter.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class RequestInWorker extends AsyncTask<Void,String,Void> implements TypeRequestAnswer {

        @Override
        protected Void doInBackground(Void... voids) {
            if (!AppUtils.isAlreadyConnect()){
                if (!AppUtils.startConnect()){
                    publishProgress(getResources().getString(R.string.serverNotRespond));
                    return null;
                }
            }

            TransferRequestAnswer out = new TransferRequestAnswer(GET_REQUEST_IN,AppUtils.getLogin(),AppUtils.getPassword());
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
                if (node.get("type").asText().equals("."+ RequestIn.class.getSimpleName())){
                    try {
                        RequestIn requestIn = (RequestIn)objectMapper.readValue(data.get(data.size()-1),RequestIn.class);
                        if (requestIn.users!=null){
                            requests = requestIn.users;
                        }
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
            inFriendsAdapter = new InFriendsAdapter(requests,getInputRequestActivity());
            requestList.setAdapter(inFriendsAdapter);
        }
    }
}
