package bubok.wordgame.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.Profile;


import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;


import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bubok.wordgame.AsyncTasks.DownloadImageTask;
import bubok.wordgame.Class.SingleUploadBroadcastReceiver;
import bubok.wordgame.R;
import bubok.wordgame.Service.SocketService;


public class Main extends AppCompatActivity {

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
        context = Main.this;

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
                    Intent intent = new Intent(Main.this, Login.class);
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

        initButton();

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
                        sendUserInfo.put("AVATAR", profile.getProfilePictureUri(500, 500).toString());
                        sendUserInfo.put("USER_ID_FB", profile.getId());
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
                        setName(jsonObject.getString("username"));
                        String avatarUrl = jsonObject.getString("avatar");
                        new DownloadImageTask((ImageView) findViewById(R.id.imageViewAvatar))
                                .execute(avatarUrl);
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onOpenChat(JSONObject jsonObject) {
                    Log.i(TAG, "onOpenChat");
                    try {
                        String gameId = jsonObject.getString("gameId");
                        openChat(gameId);
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
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
                public void onCancelGame() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.toast_game_cancel), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }

                @Override
                public void onGameIsStarted() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.toast_game_started), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }

                @Override
                public void onDisconnect() {

                }


            });
            mService.send("login", idUSer);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    private void initButton() {
        findViewById(R.id.buttonStartGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame(v);
            }
        });
        findViewById(R.id.buttonStatistics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statistics(v);
            }
        });
        findViewById(R.id.buttonLogo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about(v);
            }
        });
        findViewById(R.id.buttonFriends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socialFriends(v);
            }
        });

    }

    private void socialFriends(View v) {
        Intent intent = new Intent(this, SocialFriends.class);
        startActivity(intent);
    }

    private void statistics(View v) {
        Intent intent = new Intent(this, Statistics.class);
        startActivity(intent);
    }

    private void about(View v) {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }

    private void setName(final String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textViewName = (TextView) findViewById(R.id.textViewName);
                textViewName.setText(name);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
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

    public void newGame(View v) {
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
                Log.i(TAG, "Accept invite");
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("gameId", gameId);
                    mService.send("accept invitation", jsonObject);
                    showProgress(true);
                } catch (Exception ex) {
                    Log.i(TAG, ex.getMessage());
                }

            }
        });
        builder.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Log.i(TAG, "Cancel invite");
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("gameId", gameId);
                    mService.send("cancel invitation", jsonObject);
                    showProgress(false);
                } catch (Exception ex) {
                    Log.i(TAG, ex.getMessage());
                }
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        final ProgressBar waitGame = (ProgressBar) findViewById(R.id.waitGame);
        final RelativeLayout progressBarLayout = (RelativeLayout) findViewById(R.id.progressBarLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            waitGame.setVisibility(show ? View.GONE : View.VISIBLE);
            progressBarLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            waitGame.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            waitGame.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            waitGame.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    waitGame.setVisibility(show ? View.VISIBLE : View.GONE);
                    progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            //mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private String getMessageDialog(String leader, String leaderRaiting, String countInvite) {
        StringBuilder result = new StringBuilder();
        result.append(leader + " " + getString(R.string.invite_message1) + " " +
                leaderRaiting + " " + getString(R.string.invite_message2) + " " +
                countInvite + " " + getString(R.string.invite_message3));
        return result.toString();
    }

    private void openChat(String gameId) {

        Intent intent = new Intent(Main.this, Chat.class);
        intent.putExtra(EXTRA_MESSAGE_USED_ID_USER, profile.getId());
        intent.putExtra(EXTRA_MESSAGE_USED_GAME, gameId);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        if (mBound) {
            mService.deleteMainListener();
            unbindService(mConnection);
            Log.i(TAG, "unbindService");
            mBound = false;
        }
        showProgress(false);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }



}
