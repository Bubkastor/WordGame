package bubok.wordgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Cheat extends AppCompatActivity {

    private String login = "";
    private ListView listViewCheat;
    private Button buttonSend;
    private EditText editTextMessage;
    private MessageAdapter messageAdapter;
    private String URL;
    private Socket mSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat);
        URL = getResources().getString(R.string.URLCheatTest);
        Log.i("STRING", "URL: " + URL);
        {
            try {
                mSocket = IO.socket(URL);
            } catch (URISyntaxException e) {
                Log.i("SOCKET", "ERROR: " + e.getMessage());
            }
        }


        Intent intent = getIntent();
        login = intent.getStringExtra(Login.EXTRA_MESSAGE);

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                mSocket.emit("add user", login);
            }
        }).on("joined", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHEAT", "USER JOINED");
                JSONObject answer = (JSONObject) args[0];
                try {
                    AddMessageInCheat(answer.getString("username"), "Connected");
                } catch (Exception ex) {
                    Log.i("CHEAT", "ERROR: " + ex.getMessage());
                }
            }
        }).on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHEAT", "MESSAGE");
                JSONObject answer = (JSONObject) args[0];
                try {
                    AddMessageInCheat(answer.getString("username"), answer.getString("message"));
                } catch (Exception ex) {
                    Log.i("CHEAT", "ERROR: " + ex.getMessage());
                }
            }
        }).on("user left", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHEAT", "USER LEFT");
                JSONObject answer = (JSONObject) args[0];
                try {
                    AddMessageInCheat(answer.getString("username"), "Left");
                } catch (Exception ex) {
                    Log.i("CHEAT", "ERROR: " + ex.getMessage());
                }
            }
        });
        mSocket.connect();

        listViewCheat = (ListView) findViewById(R.id.listViewCheat);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);

        ArrayList<Message> arrayList = new ArrayList<Message>();
        messageAdapter = new MessageAdapter(this, arrayList);
        listViewCheat.setAdapter(messageAdapter);
    }
    private void AddMessageInCheat(String login, String message){
        Message message1 = new Message(login, message);
        messageAdapter.add(message1);
        messageAdapter.notifyDataSetChanged();
    }

    public void buttonSendClick(View v){
        Log.i("CHEAT", "send");
        String message = editTextMessage.getText().toString();
        editTextMessage.setText("");
        try{
            mSocket.emit("message", message);
        } catch (Exception ex){
            Log.i("CHEAT", "ERROR: " + ex.getMessage());
        }
    }
}
