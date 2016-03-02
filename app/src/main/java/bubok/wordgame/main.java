package bubok.wordgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class main extends AppCompatActivity {
    public final static String EXTRA_MESSAGE_USED_ACCOUNT = "bubok.wodgame.notregister";
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
                    if (!answer.getBoolean("enter")) ReturnPage();

                } catch (Exception ex) {
                    Log.i("SOCKET", "ERROR: " + ex.getMessage());
                }

            }
        });
        mSocket.connect();

    }
    private void ReturnPage(){
        Intent intent = new Intent(main.this, Login.class);
        intent.putExtra(EXTRA_MESSAGE_USED_ACCOUNT, true);
        startActivity(intent);
    }
    @Override
    public void onDestroy(){
        mSocket.disconnect();
        super.onDestroy();
    }

}
