package bubok.wordgame.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.method.Touch;
import android.util.EventLog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bubok.fullscreenimageview.FullScreenImageView;
import com.github.rtoshiro.view.video.FullscreenVideoLayout;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import java.util.ArrayList;
import bubok.wordgame.adapter.MessageAdapter;
import bubok.wordgame.other.Message;
import bubok.wordgame.R;
import bubok.wordgame.service.SocketService;

/**
 * активити чата
 */
public class Chat extends AppCompatActivity {

    public final static String EXTRA_MESSAGE_WIN_NAME = "bubok.wordgame.WIN.NAME";
    public final static String EXTRA_MESSAGE_WIN_AVATAR = "bubok.wordgame.WIN.AVATAR";
    public final static String EXTRA_MESSAGE_LEAD_ID = "bubok.wordgame.LEAD.ID";
    public final static String EXTRA_MESSAGE_LEAD_NAME = "bubok.wordgame.LEAD.NAME";
    public final static String EXTRA_MESSAGE_LEAD_AVATAR = "bubok.wordgame.LEAD.AVATAR";
    public final static String EXTRA_MESSAGE_TIME = "bubok.wordgame.TIME";
    public final static String EXTRA_MESSAGE_WORD = "bubok.wordgame.WORD";
    public final static String EXTRA_MESSAGE_LEAD_IS_ADMIN = "bubok.wordgame.IS.ADMIN";

    private static final String TAG = "CHAT";
    private Context context;
    private FullScreenImageView imageView;
    private EditText editTextMessage;
    private MessageAdapter messageAdapter;
    private View showMedia;
    private String mGame;
    private String idUser;

    private FullscreenVideoLayout video;

    private Boolean isAdmin;

    private Intent service;
    public static SocketService mService;
    private boolean mBound;

    private RecyclerView mRecyclerView;

    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = Chat.this;
        service = new Intent(this, SocketService.class);

        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        editTextMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    buttonSendClick();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.buttonSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonSendClick();
            }
        });
        showMedia = findViewById(R.id.showMedia);

        Intent intent = getIntent();
        idUser = intent.getStringExtra(Main.EXTRA_MESSAGE_USED_ID_USER);
        mGame = intent.getStringExtra(Main.EXTRA_MESSAGE_USED_GAME);

        mRecyclerView = (RecyclerView) findViewById(R.id.listViewCheat);

        imageView = (FullScreenImageView) findViewById(R.id.imageViewChat);

        video = (FullscreenVideoLayout) findViewById(R.id.videoView);

        video.setActivity(this);


        final LinearLayoutManager linearLayoutManager  = new LinearLayoutManager (this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        ArrayList<Message> arrayList = new ArrayList<>();

        messageAdapter = new MessageAdapter(arrayList);
        mRecyclerView.setAdapter(messageAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                JSONObject sendData = new JSONObject();
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        try {
                            sendData.put("id", messageAdapter.getItem(viewHolder.getAdapterPosition()).getIDMessage());
                            sendData.put("status", 1);
                        } catch (Exception ex) {
                            Log.i("JSON", ex.getMessage());
                        }
                        Chat.mService.chatSend("change status message", sendData);
                        Log.i(TAG, "Swipe LEFT");
                        break;
                    case ItemTouchHelper.RIGHT:
                        Log.i(TAG, "Swipe RIGHT");
                        try {
                            sendData.put("id", messageAdapter.getItem(viewHolder.getAdapterPosition()).getIDMessage());
                            sendData.put("status", -1);
                        } catch (Exception ex) {
                            Log.i("JSON", ex.getMessage());
                        }
                        Chat.mService.chatSend("change status message", sendData);
                        break;
                    default:
                        break;
                }
                messageAdapter.notifyDataSetChanged();
            }
        };
        itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

    }

    /**
     * открыть фото на весь экран
     */
    private void showPhoto(){
        imageView.setFullScreen();
    }

    /**
     * обработка событий сервер
     * связаных с чатом
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            SocketService.SocketIOBinder binder = (SocketService.SocketIOBinder) service;
            mService = binder.getService();
            binder.setChatListener(new SocketService.SocketChatListener() {
                @Override
                public void onConnected() {

                }

                @Override
                public void onMessage(JSONObject jsonObject) {
                    Log.i(TAG, "MESSAGE");
                    try {
                        addMessageInCheat(
                                jsonObject.getString("avatar"),
                                jsonObject.getString("username"),
                                jsonObject.getString("message"),
                                jsonObject.getString("idMessage"),
                                jsonObject.getString("status"),
                                jsonObject.getString("userId"));
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onJoined(JSONObject jsonObject) {
                    try {
                        String username = jsonObject.getString("username");
                        Log.i(TAG, "USER JOINED: " + username);
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onUserLeft(JSONObject jsonObject) {
                    try {
                        Log.i(TAG, "USER LEFT: " + jsonObject.getString("username"));
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }

                }

                @Override
                public void onCloseGame(JSONObject jsonObject) {
                    Log.i(TAG, "close chat");
                    try {
                        String winerName = null;
                        if (jsonObject.has("winerName"))
                            winerName = jsonObject.getString("winerName");

                        String winerAvatar = null;
                        if (jsonObject.has("winerAvatar"))
                            winerAvatar = jsonObject.getString("winerAvatar");
                        String leaderAvatar = jsonObject.getString("leaderAvatar");
                        String leaderId = jsonObject.getString("leaderId");
                        String leaderName = jsonObject.getString("leaderName");
                        String timeGame = jsonObject.getString("timeGame");
                        String word = jsonObject.getString("word");

                        closeCheat(winerName, winerAvatar, leaderId, leaderName, leaderAvatar, timeGame, word);
                        mService.chatSend("leave chat");
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());

                    }
                }

                @Override
                public void onInfoGame(JSONObject jsonObject) {
                    Log.i(TAG, "info");
                    try {

                        String id = jsonObject.getString("mediaId");
                        String url = getString(R.string.URL) + "/file/" + id;
                        String leaderId = jsonObject.getString("leaderId");
                        isAdmin = jsonObject.getBoolean("admin");

                        String mediaType = jsonObject.getString("mediaType").split("/")[0];

                        if (isAdmin) {
                            Log.i(TAG, "i'm admin");

                            Handler handler = new Handler(getBaseContext().getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    itemTouchHelper.attachToRecyclerView(mRecyclerView);
                                }
                            });
                        }
                        messageAdapter.setLeaderId(leaderId);
                        messageAdapter.setOptionPanel(isAdmin);
                        setMediaContainer(url, mediaType);
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onChangeStatusMessage(JSONObject jsonObject) {
                    Log.i(TAG, "change status message");
                    try {
                        String id = jsonObject.getString("idMessage");
                        String status = jsonObject.getString("status");
                        changeStatus(id, status);
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onDisconnect() {

                }
            });
            mBound = true;
            JSONObject answer = new JSONObject();
            try {
                answer.put("idUser", idUser);
                answer.put("game", mGame);
            } catch (Exception ex) {
                Log.i(TAG, ex.getMessage());
            }
            mService.chatSend("add user", answer);
            mService.chatSend("get info game", mGame);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };

    /**
     * подключение к серверу
     */
    @Override
    public void onStart() {
        super.onStart();
        if (!mBound)
            bindService(service, mConnection, Context.BIND_AUTO_CREATE);

    }

    /**
     * изменеия стстуса сообщения
     * @param id
     * @param status
     */
    private void changeStatus(String id, String status) {
        messageAdapter.ChangeStatus(id, status);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * закрытие чата вызов экрана победы с параметрами
     * @param winnerName
     * @param winerAvatar
     * @param leaderId
     * @param leaderName
     * @param leaderAvatar
     * @param timeGame
     * @param word
     */
    private void closeCheat(String winnerName, String winerAvatar, String leaderId, String leaderName, String leaderAvatar, String timeGame, String word) {
        Intent intent = new Intent(Chat.this, WinGame.class);
        intent.putExtra(EXTRA_MESSAGE_WIN_NAME, winnerName);
        intent.putExtra(EXTRA_MESSAGE_WIN_AVATAR, winerAvatar);
        intent.putExtra(EXTRA_MESSAGE_LEAD_AVATAR, leaderAvatar);
        intent.putExtra(EXTRA_MESSAGE_LEAD_NAME, leaderName);
        intent.putExtra(EXTRA_MESSAGE_LEAD_ID, leaderId);
        intent.putExtra(EXTRA_MESSAGE_LEAD_IS_ADMIN, isAdmin);

        intent.putExtra(EXTRA_MESSAGE_TIME, timeGame);
        intent.putExtra(EXTRA_MESSAGE_WORD, word);
        startActivity(intent);
        finish();
    }

    /**
     * добавления сообщения в чат
     * @param urlAvatar
     * @param login
     * @param message
     * @param idMessage
     * @param status
     * @param userId
     */
    private void addMessageInCheat(String urlAvatar, String login, String message, String idMessage, String status, String userId) {
        Message message1 = new Message(urlAvatar, login, message, idMessage, status, userId);
        messageAdapter.add(message1);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, Integer.toString(messageAdapter.getItemCount()));
                messageAdapter.notifyDataSetChanged();
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                    }
                });

            }
        });
    }

    /**
     * отправка сообшения серверу
     */
    private void buttonSendClick() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Log.i(TAG, "send");
        String message = editTextMessage.getText().toString();
        editTextMessage.setText("");
        JSONObject sendMessage = new JSONObject();
        try{
            sendMessage.put("message", message);
            sendMessage.put("game", mGame);
            mService.chatSend("message", sendMessage);

        } catch (Exception ex){
            Log.i(TAG, "ERROR: " + ex.getMessage());
        }
    }

    /**
     * установка медиа контента на экран
     * взависимоти от типа контенат
     * @param url
     * @param mediaType
     */
    private void setMediaContainer(final String url, final String mediaType) {
        Handler handler = new Handler(getBaseContext().getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.GONE);
                video.setVisibility(View.GONE);
                Uri uri = Uri.parse(url);
                Log.i(TAG, "uri " + uri.toString());

                switch (mediaType) {
                    case "image":
                        ((ImageButton)showMedia).setImageResource(R.drawable.photo);
                        showMedia.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showPhoto();
                            }
                        });
                        Picasso.with(context).load(uri.toString()).into(imageView);

                        break;
                    case "audio":
                        ((ImageButton)showMedia).setImageResource(R.drawable.audio);
                    case "video":
                        ((ImageButton)showMedia).setImageResource(R.drawable.video);
                        video.setVisibility(View.VISIBLE);
                        try {
                            video.setVideoURI(uri);
                        } catch (Exception ex) {
                            Log.i(TAG, ex.getMessage());
                        }
                        break;
                    default:
                        break;
                }

            }
        });
    }

    /**
     * при уничтожение приложения отписатся от сервера
     * отправить событие что мы покинули игру
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.chatSend("leave chat");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

}