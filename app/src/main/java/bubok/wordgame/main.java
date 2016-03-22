package bubok.wordgame;

import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.content.Context;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.Profile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import bubok.wordgame.Service.SocketService;


public class main extends AppCompatActivity {

    public static final String EXTRA_MESSAGE_USED_GAME = "bubok.wordgame.game";
    public static final String EXTRA_MESSAGE_USED_ID_USER = "bubok.wordgame.id.user";
    private static final String TAG = "MAIN";
    private boolean mBound;
    private static Intent service;
    public static String idUSer;
    private Profile profile;
    private Context context;
    private static SocketService mService;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
        setContentView(R.layout.activity_main);
        context = main.this;

        service = new Intent(this, SocketService.class);
        service.putExtra("url", getResources().getString(R.string.URL));
        service.putExtra("chatNamespace", getResources().getString(R.string.ChatNamespace));

        startService(service);

        Log.i(TAG, "bindService");
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
            idUSer = intent.getStringExtra(Login.EXTRA_MESSAGE_ID_USER);
        }
        profile = Profile.getCurrentProfile();
        Log.i(TAG, "onCreate");

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
                }

                @Override
                public void onNotFound() {
                    Log.i(TAG, "not found");
                    JSONObject sendUserInfo = new JSONObject();
                    try {
                        sendUserInfo.put("NAME", profile.getName());
                        sendUserInfo.put("AVATAR", profile.getProfilePictureUri(800, 600).toString());
                        sendUserInfo.put("USER_ID_FB", profile.getId());
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
                        new AsyncTasks.DownloadImageTask((ImageView) findViewById(R.id.imageView2))
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
                        String gameId = jsonObject.getString("game");
                        String leader = jsonObject.getString("leader");
                        String leaderRaiting = jsonObject.getString("raiting");
                        String countInvite = jsonObject.getString("count");
                        inviteChat(leader, gameId, leaderRaiting, countInvite);
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onDisconnect() {

                }

                @Override
                public void onUserOnline(JSONArray jsonArray) {

                }

            });
            mService.mainSend("login", idUSer);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    public void buttonNewGameClick(View v){
        Intent intent = new Intent(this, StartGame.class);
        startActivity(intent);
    }


    private void inviteChat(String leader, final String gameId, String leaderRaiting, String countInvite) {

        String title = getString(R.string.invite_title);
        String message = getMessageDialog(leader, leaderRaiting, countInvite);
        String button1String = getString(R.string.accept);
        String button2String = getString(R.string.cancel);

        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openChat(gameId);
            }
        });
        builder.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });

    }

    private String getMessageDialog(String leader, String leaderRaiting, String countInvite) {
        StringBuilder result = new StringBuilder();
        result.append(leader + " " + getString(R.string.invite_message1) + " " +
                leaderRaiting + " " + getString(R.string.invite_message2) + " " +
                countInvite + " " + getString(R.string.invite_message3));
        return result.toString();
    }

    private void openChat(String gameId) {
        Intent intent = new Intent(main.this, Chat.class);
        intent.putExtra(EXTRA_MESSAGE_USED_ID_USER, profile.getId());
        intent.putExtra(EXTRA_MESSAGE_USED_GAME, gameId);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            Log.i(TAG, "unbindService");
            mBound = false;
        }
    }
    @Override
    public void onDestroy(){
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }
}
