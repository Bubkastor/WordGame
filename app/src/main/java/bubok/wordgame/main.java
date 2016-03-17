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

import java.net.URISyntaxException;
import java.net.URL;


import bubok.wordgame.Service.SocketService;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class main extends AppCompatActivity {
    public static final String EXTRA_MESSAGE_USED_GAME = "bubok.wordgame.game";
    public static final String EXTRA_MESSAGE_USED_TOKEN = "bubok.wordgame.token";
    private static final String TAG_WORKER = "TAG_WORKER";
    private static final String TAG = "MAIN";
    private static String token;

    public static Socket mSocket;


    private static SocketService mService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "MainActivity onServiceConnected");
            SocketService.SocketIOBinder binder = (SocketService.SocketIOBinder) service;
            mService = binder.getService();
            binder.setListener(new SocketService.SocketIOListener() {
                @Override
                public void onConnected() {
                    Log.i(TAG, "Connect");
                    mService.send("login", token);
                }

                @Override
                public void onMessage(JSONObject jsonObject) {

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
                    mService.send("login", sendUserInfo);
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
                        Log.i(TAG, "ERROR: " + ex.getMessage());
                    }
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "MainActivity onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
        setContentView(R.layout.activity_main);
        Intent service = new Intent(this, SocketService.class);

        startService(service);

        bindService(service, mConnection, Context.BIND_AUTO_CREATE);


        new AccessTokenTracker() {
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
        /*
        final WorkerFragment retainedWorkerFragment = (WorkerFragment) getFragmentManager().findFragmentByTag(TAG_WORKER);

        if (retainedWorkerFragment != null){
            Log.i(TAG, "found fragment worker");
            socketModel = retainedWorkerFragment.getSocketModel();
        } else{
            Log.i(TAG, "not found fragment worker");
            String url = getResources().getString(R.string.URLOnline);
            String chat = getResources().getString(R.string.URLNamespace);
            Bundle bundle = new Bundle();
            bundle.putString("url", url);
            bundle.putString("chat", chat);
            final WorkerFragment workerFragment = new WorkerFragment(bundle);
            getFragmentManager().beginTransaction().
                    add(workerFragment, TAG_WORKER)
                    .commit();
            socketModel = workerFragment.getSocketModel();
            initBehaviorSocket();
        }
        Log.i(TAG, "onCreate");
        */
    }

    private void initBehaviorSocket(){
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "Connect");
                mSocket.emit("login", token);
            }
        }).on("not found", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "not found");
                JSONObject sendUserInfo = new JSONObject();
                try {
                    Profile profile = Profile.getCurrentProfile();
                    sendUserInfo.put("NAME", profile.getName());
                    sendUserInfo.put("FB", token);
                    sendUserInfo.put("AVATAR", GetPathAvatar(profile.getId()));
                    mSocket.emit("login", sendUserInfo);
                } catch (Exception ex) {
                    Log.i(TAG, ex.getMessage());
                }
            }
        }).on("info", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "info");
                JSONObject answer = (JSONObject) args[0];
                try {
                    //String name = answer.getString("username");
                    String avatarUrl = answer.getString("avatar");
                    new DownloadImageTask((ImageView) findViewById(R.id.imageView2))
                            .execute(avatarUrl);

                } catch (Exception ex) {
                    Log.i(TAG, "ERROR: " + ex.getMessage());
                }

            }
        }).on("invite", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                final JSONObject answer = (JSONObject) args[0];
                try {
                    String room = answer.getString("title");
                    Log.i(TAG, room);

                } catch (Exception ex) {
                    Log.i(TAG, "ERROR: " + ex.getMessage());
                }

            }
        }).on("open chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final JSONObject answer = (JSONObject) args[0];
                try {
                    String game = answer.getString("game");
                    Intent intent = new Intent(main.this, Chat.class);
                    intent.putExtra(EXTRA_MESSAGE_USED_TOKEN, token);
                    intent.putExtra(EXTRA_MESSAGE_USED_GAME, game);
                    Log.i(TAG, "Chat open");
                    startActivity(intent);
                } catch (Exception ex) {
                    Log.i("SOCKET", "ERROR: " + ex.getMessage());
                }

            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "Disconnected");
            }
        });
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume");
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

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }
}
