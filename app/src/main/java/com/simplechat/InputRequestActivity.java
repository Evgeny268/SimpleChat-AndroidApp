package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import internet.InetWorker;
import transfers.RequestIn;
import transfers.TransferRequestAnswer;
import transfers.TypeRequestAnswer;
import transfers.User;

public class InputRequestActivity extends AppCompatActivity implements InFriendsAdapter.RequestInControl {

    private static ArrayList<User> requests = new ArrayList<>();
    private RecyclerView requestList;
    private InFriendsAdapter inFriendsAdapter;
    private LinearLayoutManager layoutManager;
    private DividerItemDecoration dividerItemDecoration;

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
        RequestInWorker requestInWorker = new RequestInWorker();
        requestInWorker.execute();
    }

    public InputRequestActivity getInputRequestActivity(){
        return this;
    }
    @Override
    public void onClickAccept(User user) {
        Toast.makeText(this, "Кнопочка принять "+user.login, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClickReject(User user) {
        Toast.makeText(this, "Кнопочка отказать "+user.login, Toast.LENGTH_SHORT).show();
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
