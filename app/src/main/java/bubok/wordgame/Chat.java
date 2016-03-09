package bubok.wordgame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.util.ArrayList;

import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Chat extends AppCompatActivity {

    private String token = "";
    private ListView listViewCheat;
    private Button buttonSend;
    private EditText editTextMessage;
    private MessageAdapter messageAdapter;
    private ImageView imageView;
    private String URL;
    private String namespaceSocket;
    private String mGame;
    public static Socket mSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        URL = getResources().getString(R.string.URLOnline);
        namespaceSocket = getResources().getString(R.string.URLNamespace);

        Log.i("STRING", "URL + namespaceSocket: " + URL + namespaceSocket);
        {
            try {
                Manager manager = new Manager(new URI(URL));
                mSocket = manager.socket(namespaceSocket);
            } catch (URISyntaxException e) {
                Log.i("SOCKET", "ERROR: " + e.getMessage());
            }
        }
        Intent intent = getIntent();
        token = intent.getStringExtra(main.EXTRA_MESSAGE_USED_TOKEN);
        mGame = intent.getStringExtra(main.EXTRA_MESSAGE_USED_GAME);

        listViewCheat = (ListView) findViewById(R.id.listViewCheat);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        imageView = (ImageView) findViewById(R.id.imageView);

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject answer = new JSONObject();
                try{
                    answer.put("token", token);
                    answer.put("game", mGame);
                }catch (Exception ex){}
                mSocket.emit("add user", answer);
            }
        }).on("joined", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHEAT", "USER JOINED");
                JSONObject answer = (JSONObject) args[0];
                try {
                    //AddMessageInCheat(answer.getString("avatar"), answer.getString("username"), "Connected");
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
                    AddMessageInCheat(
                            answer.getString("avatar"),
                            answer.getString("username"),
                            answer.getString("message"),
                            answer.getString("idMessage"),
                            answer.getString("status"));
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
                    //AddMessageInCheat(answer.getString("avatar"), answer.getString("username"), "Left");
                } catch (Exception ex) {
                    Log.i("CHEAT", "ERROR: " + ex.getMessage());
                }
            }
        }).on("close game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHEAT", "close chat");
                JSONObject answer = (JSONObject) args[0];
                try {
                    String userWin = answer.getString("win");
                    String word = answer.getString("word");
                    String text = "Пользователь " + userWin + " победил загаданое слово " + word;
                    Log.i("CHEAT", text);
                    //AddMessageInCheat("", "Bot", text);
                    CloseCheat(userWin, word);
                } catch (Exception ex) {
                    Log.i("CHEAT", ex.getMessage());

                }
            }
        }).on("get info game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHEAT", "info");
                JSONObject answer = (JSONObject) args[0];
                try {
                    String typeMedia = answer.getString("typeMedia");
                    String contentType = answer.getString("contentType");
                    byte[] decodedBytes = (byte[]) answer.get("data");
                    Boolean isAdmin = answer.getBoolean("admin");
                    messageAdapter.setOptionPanel(isAdmin);
                    Bitmap bMap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    imageView.setImageBitmap(bMap);
                } catch (Exception ex) {                }
            }
        }).on("change status message",new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHAT", "change status message");
                JSONObject answer = (JSONObject) args[0];
                try {
                    String id = answer.getString("idMessage");
                    String status = answer.getString("status");
                    ChangeStatus(id, status);

                } catch (Exception ex) {
                    Log.i("CHAT", ex.getMessage());
                }
            }
        });
        mSocket.connect();

        mSocket.emit("get info game", mGame);

        imageView.setImageResource(R.drawable.cat);

        ArrayList<Message> arrayList = new ArrayList<Message>();
        messageAdapter = new MessageAdapter(this, arrayList);
        listViewCheat.setAdapter(messageAdapter);
    }
    private synchronized  void ChangeStatus(String id, String status){
        messageAdapter.ChangeStatus(id, status);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
            }
        });
    }
    private void CloseCheat(String userWin, String word) {
        //ToDo close chat an show winer
    }

    private synchronized void AddMessageInCheat(
            String urlAvatar,
            String login,
            String message,
            String idMessage,
            String status){
        Message message1 = new Message(urlAvatar, login, message, idMessage, status);
        messageAdapter.add(message1);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    public void buttonSendClick(View v) {
        Log.i("CHEAT", "send");
        String message = editTextMessage.getText().toString();
        editTextMessage.setText("");
        JSONObject sendMessage = new JSONObject();
        try{
            sendMessage.put("message", message);
            sendMessage.put("game", mGame);
            mSocket.emit("message", sendMessage);

        } catch (Exception ex){
            Log.i("CHEAT", "ERROR: " + ex.getMessage());
        }
    }
}
