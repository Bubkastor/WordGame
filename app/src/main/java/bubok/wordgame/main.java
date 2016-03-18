package bubok.wordgame;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.content.Context;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.Profile;

import org.json.JSONObject;

import java.net.URL;

import bubok.wordgame.Service.SocketService;


public class main extends AppCompatActivity {

    public static final String EXTRA_MESSAGE_USED_GAME = "bubok.wordgame.game";
    public static final String EXTRA_MESSAGE_USED_TOKEN = "bubok.wordgame.token";
    private static final String TAG = "MAIN";
    private boolean mBound;
    private static Intent service;
    public static String token;

    private static SocketService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
        setContentView(R.layout.activity_main);

        service = new Intent(this, SocketService.class);
        service.putExtra("url", getResources().getString(R.string.URL));
        service.putExtra("chatNamespace", getResources().getString(R.string.ChatNamespace));

        startService(service);

        new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Intent intent = new Intent(main.this, Login.class);
                    startActivity(intent);
                }
            }
        };
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            token = intent.getStringExtra(Login.EXTRA_MESSAGE_TOKEN);
        }

        Log.i(TAG, "onCreate");

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        super.onNewIntent(intent);
    }
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            SocketService.SocketIOBinder binder = (SocketService.SocketIOBinder) service;
            mService = binder.getService();
            binder.setMainListener(new SocketService.SocketMainListener() {
                @Override
                public void onConnected() {
                    Log.i(TAG, "onConnected");
                    mService.mainSend("login", token);
                }
                @Override
                public void onNotFound(JSONObject jsonObject) {
                    Log.i(TAG, "not found");
                    JSONObject sendUserInfo = new JSONObject();
                    try {
                        Profile profile = Profile.getCurrentProfile();
                        sendUserInfo.put("NAME", profile.getName());
                        sendUserInfo.put("FB", token);
                        sendUserInfo.put("AVATAR", GetPathAvatar(profile.getId()));

                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                        return;
                    }
                    mService.mainSend("login", sendUserInfo);
                }
                @Override
                public void onInfo(JSONObject jsonObject) {
                    Log.i(TAG, "info");
                    try {
                        //String name = answer.getString("username");
                        String avatarUrl = jsonObject.getString("avatar");
                        new DownloadImageTask((ImageView) findViewById(R.id.imageView2))
                                .execute(avatarUrl);
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onOpenChat(JSONObject jsonObject) {
                }

                @Override
                public void onInviteChat(JSONObject jsonObject) {
                    Log.i(TAG, "invite chat");
                    try {
                        String game = jsonObject.getString("game");
                        Intent intent = new Intent(main.this, Chat.class);
                        intent.putExtra(EXTRA_MESSAGE_USED_TOKEN, token);
                        intent.putExtra(EXTRA_MESSAGE_USED_GAME, game);

                        startActivity(intent);
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onDisconnect() {

                }
            });
            mBound = true;
            mService.mainSocketConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        if (!mBound) {
            bindService(service, mConnection, Context.BIND_AUTO_CREATE);
        }

    }

    public void buttonNewGameClick(View v){
        Intent intent = new Intent(this, StartGame.class);
        startActivity(intent);
    }

    private String GetPathAvatar(String id){
        return "https://graph.facebook.com/" + id + "/picture?type=large";
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mIcon11 = null;
            try {
                URL newUrl = new URL(url);
                mIcon11 = BitmapFactory.decodeStream(newUrl.openConnection().getInputStream());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    @Override
    public void onDestroy(){
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }
}
