package com.example.chatandroidclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static final String roomName = "com.example.chatandroidclient.MESSAGE";
    public static final String userName = "com.example.chatandroiddlient.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getChatRoomName(View view){
        Intent intent = new Intent(this, chatPage.class);
        EditText editText = (EditText) findViewById(R.id.editText2);
        String message = editText.getText().toString();
        intent.putExtra(roomName, message);
        EditText usernameText = (EditText) findViewById(R.id.editText);
        String name = usernameText.getText().toString();
        intent.putExtra(userName,name);
        startActivity(intent);
    }

}
