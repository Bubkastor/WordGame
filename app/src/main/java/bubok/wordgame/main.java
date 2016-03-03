package bubok.wordgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class main extends AppCompatActivity {
    public final static String EXTRA_MESSAGE_USED_ACCOUNT = "bubok.wodgame.notregister";
    public final static String EXTRA_MESSAGE_USED_ERROR = "bubok.wodgame.error";

    private String URL;
    private String login;
    Boolean isRegister;
    private Socket mSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        login = intent.getStringExtra(Login.EXTRA_MESSAGE_LOGIN);
        isRegister = intent.getExtras().getBoolean(Login.EXTRA_MESSAGE_REGISTER);
        URL = getResources().getString(R.string.URLOnline);
        {
            try {
                mSocket = IO.socket(URL);
            } catch (URISyntaxException e) {
                Log.i("SOCKET", "ERROR: " + e.getMessage());
            }
        }

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (isRegister) {
                    mSocket.emit("register", login);
                } else {
                    mSocket.emit("login", login);
                }

            }
        }).on("message", new Emitter.Listener(){
            @Override
            public void call(Object... args) {
                JSONObject answer = (JSONObject) args[0];
                try {
                    Log.i("SOCKET","User: " + answer.getString("username") + " " + answer.getString("data") );
                    if (!answer.getBoolean("enter")) ReturnPage(answer.getString("data"));

                } catch (Exception ex) {
                    Log.i("SOCKET", "ERROR: " + ex.getMessage());
                }

            }
        }).on("all user", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject answer = (JSONObject) args[0];
                try {
                    Log.i("SOCKET","User: " + answer.getString("users") );
                } catch (Exception ex) {
                    Log.i("SOCKET", "ERROR: " + ex.getMessage());
                }

            }
        });
        mSocket.connect();
    }
    private void ReturnPage(String str){
        Intent intent = new Intent(main.this, Login.class);
        intent.putExtra(EXTRA_MESSAGE_USED_ACCOUNT, true);
        intent.putExtra(EXTRA_MESSAGE_USED_ERROR, str);
        startActivity(intent);
    }

    public void buttonGetAllUsersClick(View v){
        mSocket.emit("get all user");
    }
    public void buttonGetOnlineUsersClick(View v){
        mSocket.emit("get user online");
    }
    public void buttonNewGameClick(View v){
        Intent intent = new Intent(this, StartGame.class);
        startActivity(intent);
    }
    public void buttonCheatClick(View v){
        Intent intent = new Intent(this, Cheat.class);
        startActivity(intent);
    }

}
