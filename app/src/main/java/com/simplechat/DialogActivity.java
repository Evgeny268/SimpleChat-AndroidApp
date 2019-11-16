package com.simplechat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import transfers.User;

public class DialogActivity extends AppCompatActivity {

    private User friend;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        textView = findViewById(R.id.textViewTest);
        friend = (User)getIntent().getSerializableExtra("friend");
        textView.setText(friend.iduser+" "+friend.login);
    }
}
