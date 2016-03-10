package bubok.wordgame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.Profile;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.net.URL;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class main extends AppCompatActivity {
    public final static String EXTRA_MESSAGE_USED_ERROR = "bubok.wordgame.error";
    public final static String EXTRA_MESSAGE_USED_GAME = "bubok.wordgame.game";
    public final static String EXTRA_MESSAGE_USED_TOKEN = "bubok.wordgame.token";
    private String URL;
    private static String token;
    private String game;
    private Boolean isRegister;
    private ImageView profile_pic;
    public static Socket mSocket;
    private String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile_pic = (ImageView) findViewById(R.id.imageView2);
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
        if (intent.getExtras() != null ){
            token = intent.getStringExtra(Login.EXTRA_MESSAGE_TOKEN);
        }
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
                mSocket.emit("login", token);
            }
        }).on("not found", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("SOCKET", "not found");
                JSONObject sendUserInfo = new JSONObject();
                try {
                    Profile profile = Profile.getCurrentProfile();
                    sendUserInfo.put("NAME", profile.getName());
                    sendUserInfo.put("FB", token);
                    sendUserInfo.put("AVATAR", GetPathAvatar(profile.getId()));
                    mSocket.emit("login", sendUserInfo);
                } catch (Exception ex) {
                    Log.i("Error", ex.getMessage());
                }
                ;
            }
        }).on("info", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject answer = (JSONObject) args[0];
                try {
                    name = answer.getString("username");
                    String avatarUrl = answer.getString("avatar");
                    new DownloadImageTask((ImageView) findViewById(R.id.imageView2))
                            .execute(avatarUrl);

                } catch (Exception ex) {
                    Log.i("SOCKET", "ERROR: " + ex.getMessage());
                }

            }
        }).on("invite", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                final JSONObject answer = (JSONObject) args[0];
                try {
                    String room = answer.getString("title").toString();
                    Log.i("INVITE", room);

                } catch (Exception ex) {
                    Log.i("SOCKET", "ERROR: " + ex.getMessage());
                }

            }
        }).on("open chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final JSONObject answer = (JSONObject) args[0];
                try {
                    String game = answer.getString("game").toString();
                    Intent intent = new Intent(main.this, Chat.class);
                    intent.putExtra(EXTRA_MESSAGE_USED_TOKEN, token);
                    intent.putExtra(EXTRA_MESSAGE_USED_GAME, game);
                    Log.i("SOCKET", "Chat open");
                    startActivity(intent);
                } catch (Exception ex) {
                    Log.i("SOCKET", "ERROR: " + ex.getMessage());
                }

            }
        });
        //
    }

    @Override
    public void onResume(){
        super.onResume();
        mSocket.connect();
    }

    public void buttonCreateGame(View v){
        JSONObject game_Info = new JSONObject();
        try{
            game_Info.put("LEADER_ID", token);
            game_Info.put("WORD", "cat" );
        }
        catch (Exception ex){

        }
    }
    public void buttonNewGameClick(View v){
        Intent intent = new Intent(this, StartGame.class);
        startActivity(intent);
    }
    public void buttonInviteClick(View v){
        mSocket.emit("go chat", "image");
    }

    public void buttonChatClick(View v){
        Intent intent = new Intent(this, Chat.class);
        intent.putExtra(Login.EXTRA_MESSAGE_TOKEN, token);
        startActivity(intent);
    }

    private String GetPathAvatar(String id){
        return "https://graph.facebook.com/" + id + "/picture?type=large";
    }
    @Override
    public void onPause(){
        //mSocket.disconnect();
        super.onPause();
    }
    @Override
    public void onStop(){

        super.onStop();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                URL newurl = new URL(urldisplay);
                mIcon11 = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
