package com.example.chatandroidclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.provider.Telephony.Carriers.SERVER;

public class chatPage extends AppCompatActivity {

    static WebSocket socket;
    static String roomName;
    static String username;
    Handler messageHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        roomName = intent.getStringExtra(MainActivity.roomName);
        username = intent.getStringExtra(MainActivity.userName);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView4);
        textView.setText(roomName);

        Thread newThread = new Thread( () -> {

                socket = createAndConnect();

        });
        newThread.start();


    }



    public synchronized void sendMessage(View view){
        EditText messageField = findViewById(R.id.messageWriteField);
        String message = messageField.getText().toString();
        socket.sendText(username + " " + message);
        messageField.setText(""); // Clear the field after you send the message
    }

    public WebSocket createAndConnect(){
        socket = null;
        try {
            socket = new WebSocketFactory().createSocket("ws://10.0.2.2:8080");
            socket.connectAsynchronously();
            System.out.println("socket created");
            socket.addListener(new WebSocketAdapter() {
                public synchronized void onTextMessage(WebSocket socket, String jsonMessage) throws JSONException {
                    messageHandler.post(new Runnable() {
                        @Override
                                public void run(){
                            try{
                                appendTextView(jsonMessage);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    });

                }
                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    super.onConnected(websocket, headers);
                    socket.sendText("join " + roomName + " " + username);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }

    private void appendTextView(String message) throws JSONException {
        TextView text = new TextView(this);
        JSONObject jsonMessage = new JSONObject(message);
        String userParsed = jsonMessage.getString("user");
        String messageParsed = jsonMessage.getString("message");
        String completeMessage;
        if(messageParsed.equals(" has joined the chat")){
            completeMessage = userParsed + messageParsed;
        }else {
            completeMessage = userParsed + ":" + messageParsed;
        }
        text.setText(completeMessage);
        if(messageParsed.equals(" has joined the chat")){
            text.setGravity(Gravity.CENTER);
        }
        else if(userParsed.equals(username)) {
            text.setBackgroundColor(getResources().getColor(R.color.lightcoral));
            text.setGravity(Gravity.RIGHT);
        }else{
            text.setBackgroundColor(getResources().getColor(R.color.lightgreen));
            text.setGravity(Gravity.LEFT);
        }
        ((LinearLayout) findViewById(R.id.messageBoard)).addView(text);
    }
}
