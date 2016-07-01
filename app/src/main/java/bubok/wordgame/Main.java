package bubok.wordgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import bubok.wordgame.activity.About;
import bubok.wordgame.activity.Chat;
import bubok.wordgame.activity.SocialFriends;
import bubok.wordgame.activity.StartGame;
import bubok.wordgame.activity.Statistics;
import bubok.wordgame.other.QuickstartPreferences;
import bubok.wordgame.other.User;
import bubok.wordgame.service.RegistrationIntentService;
import bubok.wordgame.service.SocketService;

public class Main extends AppCompatActivity implements android.support.v4.app.FragmentManager.OnBackStackChangedListener {

    /**
     *использется для передачи интента id_game
     */
    public static final String EXTRA_MESSAGE_USED_GAME = "bubok.wordgame.game";
    /**
     *использется для передачи интента id_user
     */
    public static final String EXTRA_MESSAGE_USER = "bubok.wordgame.user";
    public static final String EXTRA_MESSAGE_USED_ID = "bubok.wordgame.id";
    public static final String EXTRA_MESSAGE_USED_NAME = "bubok.wordgame.name";
    public static final String EXTRA_MESSAGE_USED_SOCIAL = "bubok.wordgame.name.social";
    public static final String EXTRA_MESSAGE_USED_AVATAR = "bubok.wordgame.name.avatar";
    public static final String SOCIAL_NETWORK_TAG = "SocialIntegrationMain.SOCIAL_NETWORK_TAG";
    /**
     * Для логирования
     */
    private static final String TAG = "MAIN";
    private boolean mBound;
    private static Intent service;
    public static String idUSer;
    private Context context;
    private static SocketService mService;
    private AlertDialog.Builder builder;
    private ImageView avatar;
    private Picasso picasso;
    private OkHttpClient okHttpClient;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private static Login frag;
    private static FragmentTransaction fTrans;

    private User currentUser;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        homeAsUpByBackStack();

        context = this;
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new Login())
                    .commit();
        }

        okHttpClient = new OkHttpClient();
        picasso = new Picasso.Builder(this)
                .downloader(new OkHttpDownloader(okHttpClient))
                .build();
        mRegistrationBroadcastReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.i("BROAD", "true");
                } else {
                    Log.i("BROAD", "false");
                }
            }
        };
        registerReceiver();

        initService();

        avatar = (ImageView) findViewById(R.id.imageViewAvatar);

        initButton();
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent2 = new Intent(this, RegistrationIntentService.class);
            startService(intent2);
        }
        DrawerBuilder drawerBuilder = new DrawerBuilder();
        drawerBuilder.withActivity(this). build();
        Log.i(TAG, "onCreate");

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackStackChanged() {
        homeAsUpByBackStack();
    }
    private void homeAsUpByBackStack() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void onNewIntent(Intent intent) {
        if (intent.getExtras() != null) {
            currentUser = (User)intent.getExtras().get(EXTRA_MESSAGE_USER);
        }
    }

    private boolean isReceiverRegistered;

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Проверка сервиса google play
     * @return True если рабоатет
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Инициализирует сервис с сервером
     *
     */
    private void initService() {
        service = new Intent(this, SocketService.class);
        service.putExtra("url", getResources().getString(R.string.URL));
        service.putExtra("chatNamespace", getResources().getString(R.string.ChatNamespace));
        startService(service);
    }


    /**
     * Обработка собыйтий от сервера
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

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
                        sendUserInfo.put("NAME", currentUser.getName());
                        sendUserInfo.put("AVATAR", currentUser.getAvatar());
                        sendUserInfo.put("USER_ID_" + currentUser.getSocialNetwork().toUpperCase()
                                , currentUser.getUserID());
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
                        setAvatar(jsonObject.getString("avatar"));
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
            JSONObject send = new JSONObject();
            if (currentUser != null) {
                try {
                    send.put("id", currentUser.getUserID());
                    send.put("social", currentUser.getSocialNetwork());
                    mService.send("login", send);
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage());
                }
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    /**
     * Установка аватара
     * @param url URL адресс аватара
     */
    private void setAvatar(final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(context).load(url).into(avatar);
            }
        });
    }

    /**
     * Инициализация Кнопок на экране
     */
    private void initButton() {

        findViewById(R.id.buttonStatistics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, Statistics.class);
                //intent.putExtra(EXTRA_MESSAGE_USED_ID_USER, profile.getId());
                startActivity(intent);
            }
        });
        findViewById(R.id.buttonLogo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, About.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonFriends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, SocialFriends.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonStartGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, StartGame.class);
                startActivity(intent);
            }
        });

        View buttonShare = findViewById(R.id.buttonShare);
        if(buttonShare != null)
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("*/*");

                intent.putExtra(Intent.EXTRA_TEXT,"\n Вас друзья приглашают поиграть в WordGame \nhttps://play.google.com/store/apps/details?id=com.shazam.android" );
                try {
                    context.startActivity(Intent.createChooser(intent, "Поделиться с помощью").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (android.content.ActivityNotFoundException ex) {
                }
            }
        });


    }

    /**
     * Установка имени на экране
     * @param name строка
     */
    private void setName(final String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textViewName = (TextView) findViewById(R.id.textViewName);
                textViewName.setText(name);
            }
        });
    }

    /**
     * Востанавливаем связь с сервисами
     */
    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        registerReceiver();
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    /**
     * Закрытие приложения
     */
    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        finish();
    }

    /**
     * Обработка приглашения в игру
     *
     * @param leader
     * @param gameId
     * @param leaderRaiting
     * @param countInvite
     */
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

    /**
     * Показать прогресс на экране
     * @param show
     */
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
        }
    }

    /**
     * Создание текста сообщения в даилоге
     * @param leader
     * @param leaderRaiting
     * @param countInvite
     * @return
     */
    private String getMessageDialog(String leader, String leaderRaiting, String countInvite) {
        StringBuilder result = new StringBuilder();
        result.append(leader).append(" ")
                .append(getString(R.string.invite_message1))
                .append(" ")
                .append(leaderRaiting)
                .append(" ")
                .append(getString(R.string.invite_message2))
                .append(" ")
                .append(countInvite)
                .append(" ")
                .append(getString(R.string.invite_message3));
        return result.toString();
    }

    /**
     * Открытие окна чата
     * @param gameId
     */
    private void openChat(String gameId) {

        Intent intent = new Intent(Main.this, Chat.class);
        //intent.putExtra(EXTRA_MESSAGE_USED_ID_USER, profile.getId());
        intent.putExtra(EXTRA_MESSAGE_USED_GAME, gameId);
        startActivity(intent);
    }

    /**
     * Отписываемся от событий сервера
     */
    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SOCIAL_NETWORK_TAG);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
