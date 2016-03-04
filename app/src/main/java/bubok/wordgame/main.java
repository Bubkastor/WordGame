package bubok.wordgame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class main extends AppCompatActivity {
    public final static String EXTRA_MESSAGE_USED_ACCOUNT = "bubok.wordgame.notregister";
    public final static String EXTRA_MESSAGE_USED_ERROR = "bubok.wordgame.error";
    public final static String EXTRA_MESSAGE_USED_SOCKET = "bubok.wordgame.socket";
    private String URL;
    public static String login;
    private Boolean isRegister;

    public static Socket mSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null){
                    Intent intent = new Intent(main.this, Login.class);
                    startActivity(intent);
                }
            }
        };
        Intent intent = getIntent();
        login = intent.getStringExtra(Login.EXTRA_MESSAGE_TOKEN);
        URL = getResources().getString(R.string.URLOnline);
        Log.i("URL", URL);
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
                Log.i("SOCKET", "connection");
                mSocket.emit("login", login);

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
        }).on("invite", new Emitter.Listener(){
            @Override
            public void call(Object... args) {

                final JSONObject answer = (JSONObject) args[0];
                try {
                    String room = answer.getString("title").toString();
                    Log.i("INVITE", room);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (!isFinishing()){
                                new AlertDialog.Builder(main.this)
                                        .setTitle("Invite")
                                        .setMessage("You invite play game")
                                        .setCancelable(false)
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.i("DIALOG", "a");
                                            }
                                        }).create().show();
                            }
                        }
                    });

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
        intent.putExtra(Login.EXTRA_MESSAGE_TOKEN, login);
        startActivity(intent);
    }
    public void buttonInviteUser(View v){
        mSocket.emit("invite", "room1");
    }

}
