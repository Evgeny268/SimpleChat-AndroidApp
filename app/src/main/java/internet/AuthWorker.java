package internet;

import android.os.AsyncTask;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.simplechat.AppUtils;
import com.simplechat.LogRegActivity;
import com.simplechat.R;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import transfers.TransferRequestAnswer;
import transfers.TypeRequestAnswer;

public class AuthWorker extends AsyncTask<Void,String,Void> implements TypeRequestAnswer {

    private LogRegActivity logRegActivity;
    private boolean authDone = false;

    public AuthWorker(LogRegActivity logRegActivity) {
        this.logRegActivity = logRegActivity;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (!AppUtils.isAlreadyConnect()){
            if (!AppUtils.startConnect()){
                publishProgress(logRegActivity.getResources().getString(R.string.serverNotRespond));
                return null;
            }
        }
        if (logRegActivity.mode.equals("signup")){

        }else {
            String login = logRegActivity.tvLogin.getText().toString();
            String password = logRegActivity.tvPassword.getText().toString();
            TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION,login,password);
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter stringWriter = new StringWriter();
            try {
                objectMapper.writeValue(stringWriter,out);
                AppUtils.send(stringWriter.toString());
            } catch (Exception e) {
                publishProgress(logRegActivity.getResources().getString(R.string.error));
                return null;
            }
            ArrayList<String> data;
            synchronized (InetWorker.lock){
                if (!InetWorker.newData){
                    try {
                        InetWorker.lock.wait(10000);
                    } catch (InterruptedException e) {
                        publishProgress(logRegActivity.getResources().getString(R.string.error));
                        return null;
                    }
                }
            }

            data = InetWorker.getData();
            if (data.size()==0){
                publishProgress(logRegActivity.getResources().getString(R.string.error));
                return null;
            }
            ObjectNode node = null;
            try {
                node = new ObjectMapper().readValue(data.get(data.size()-1),ObjectNode.class);
            } catch (IOException e) {
                publishProgress(logRegActivity.getResources().getString(R.string.error));
                return null;
            }
            if (node.has("type")){
                if (node.get("type").asText().equals("."+TransferRequestAnswer.class.getSimpleName())){
                    try {
                        TransferRequestAnswer in = (TransferRequestAnswer)objectMapper.readValue(data.get(data.size()-1),TransferRequestAnswer.class);
                        if (in.request.equals(AUTHORIZATION_DONE)){
                            authDone = true;
                            return null;
                        }else if (in.request.equals(USER_NOT_EXIST)){
                            publishProgress(logRegActivity.getResources().getString(R.string.userNotExist));
                            return null;
                        }else if(in.request.equals(WRONG_PASSWORD)){
                            publishProgress(logRegActivity.getResources().getString(R.string.wrongPassword));
                            return null;
                        }else {
                            publishProgress(logRegActivity.getResources().getString(R.string.error));
                            return null;
                        }
                    } catch (IOException e) {
                        publishProgress(logRegActivity.getResources().getString(R.string.error));
                        return null;
                    }
                }else {
                    publishProgress(logRegActivity.getResources().getString(R.string.error));
                    return null;
                }
            }else {
                publishProgress(logRegActivity.getResources().getString(R.string.error));
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        for (int i = 0; i < values.length; i++) {
            Toast.makeText(logRegActivity, values[i], Toast.LENGTH_LONG).show();
            if (logRegActivity.cbAcceptLicense.isChecked()) {
                logRegActivity.btnRegLog.setEnabled(true);
                logRegActivity.cbAcceptLicense.setEnabled(true);
            }
        }
    }

    @Override
    protected void onCancelled() {
        if (logRegActivity.cbAcceptLicense.isChecked()) logRegActivity.btnRegLog.setEnabled(true);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (authDone) {
            authDone = false;
            logRegActivity.goFriendActivity();
        }
        logRegActivity.btnRegLog.setEnabled(true);
        logRegActivity.cbAcceptLicense.setEnabled(true);
    }
}


