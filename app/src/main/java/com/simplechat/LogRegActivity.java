package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class LogRegActivity extends AppCompatActivity {

    private String mode;
    private TextView tvLogin;
    private TextView tvPassword;
    private TextView tvMode;
    private Button btnRegLog;
    private CheckBox cbAcceptLicense;
    //TODO Добавить проверку логина и пароля
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_reg);
        tvLogin = findViewById(R.id.editTextLogin);
        tvPassword = findViewById(R.id.editTextPassword);
        tvMode = findViewById(R.id.textViewMode);
        btnRegLog = findViewById(R.id.buttonRegLog);
        cbAcceptLicense = findViewById(R.id.checkBoxLicense);
        mode = getIntent().getStringExtra("mode");
        if (mode.equals("signup")){
            tvMode.setText(R.string.registration);
            btnRegLog.setText(R.string.sign_up);
        }else {
            tvMode.setText(R.string.authorization);
            btnRegLog.setText(R.string.log_in);
        }
    }

    public void onlickAcceptLicense(View view) {
        if (cbAcceptLicense.isChecked()){
            btnRegLog.setEnabled(true);
        }else {
            btnRegLog.setEnabled(false);
        }
    }
}
