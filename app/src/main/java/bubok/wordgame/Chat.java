package bubok.wordgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;

import bubok.wordgame.Service.SocketService;


public class Chat extends AppCompatActivity implements SurfaceHolder.Callback {

    public final static String EXTRA_MESSAGE_WINGAME = "bubok.wordgame.WINGAME";
    private static final String TAG = "CHAT";
    private Context context;
    private ImageView imageView;
    private EditText editTextMessage;
    private MessageAdapter messageAdapter;
    private String mGame;
    private String idUser;

    private Bitmap bMap;
    private File videoFile;
    private int heightDiff = 0;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;

    private SurfaceView videoView;
    private SurfaceHolder holder;
    private MediaPlayer mp;

    private Intent service;
    public static SocketService mService;
    private boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = Chat.this;
        service = new Intent(this, SocketService.class);
        final View activityRootView = findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                Rect r = new Rect();
                activityRootView.getWindowVisibleDisplayFrame(r);
                heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
            }
        });

        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        editTextMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    buttonSendClick(v);
                    return true;
                }
                return false;
            }
        });

        Intent intent = getIntent();
        idUser = intent.getStringExtra(main.EXTRA_MESSAGE_USED_ID_USER);
        mGame = intent.getStringExtra(main.EXTRA_MESSAGE_USED_GAME);

        ListView listViewCheat = (ListView) findViewById(R.id.listViewCheat);
        imageView = (ImageView) findViewById(R.id.imageViewChat);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomImageFromThumb(imageView);
            }
        });
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        videoView = (SurfaceView) findViewById(R.id.videoViewChat);

        holder = videoView.getHolder();

        holder.addCallback(Chat.this);;
        mp = new MediaPlayer();


        ArrayList<Message> arrayList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, arrayList);
        listViewCheat.setAdapter(messageAdapter);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
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
                        AddMessageInCheat(
                                jsonObject.getString("avatar"),
                                jsonObject.getString("username"),
                                jsonObject.getString("message"),
                                jsonObject.getString("idMessage"),
                                jsonObject.getString("status"));
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

                    }

                }

                @Override
                public void onCloseGame(JSONObject jsonObject) {
                    Log.i(TAG, "close chat");
                    try {
                        String userWin = jsonObject.getString("win");
                        String word = jsonObject.getString("word");
                        String text = "Пользователь " + userWin + " победил загаданое слово " + word;
                        Log.i(TAG, text);
                        CloseCheat(userWin, word);
                        mService.chatSend("leave chat");
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());

                    }
                }

                @Override
                public void onInfoGame(JSONObject jsonObject) {
                    Log.i(TAG, "info");
                    try {
                        String typeMedia = jsonObject.getString("typeMedia");
                        String contentType = jsonObject.getString("contentType");
                        byte[] decodedBytes = (byte[]) jsonObject.get("data");
                        Boolean isAdmin = jsonObject.getBoolean("admin");
                        messageAdapter.setOptionPanel(isAdmin);
                        SetMediaContainer(typeMedia, contentType, decodedBytes);
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
                        ChangeStatus(id, status);
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

    @Override
    public void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        if (!mBound)
            bindService(service, mConnection, Context.BIND_AUTO_CREATE);

    }

    private void zoomImageFromThumb(final View thumbView) {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        final ImageView expandedImageView = (ImageView) findViewById(R.id.fullScreenView);
        expandedImageView.setPadding(0,heightDiff,0,0);
        expandedImageView.setImageBitmap(bMap);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);


        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            //todo change delta if open keyboard
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });

    }

    private synchronized void ChangeStatus(String id, String status){
        messageAdapter.ChangeStatus(id, status);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    private void CloseCheat(String userWin, String word) {
        Intent intent = new Intent(Chat.this, WinGame.class);
        String text = "Пользователь " + userWin + " победил загаданое слово " + word;
        intent.putExtra(EXTRA_MESSAGE_WINGAME, text);
        startActivity(intent);
        finish();
    }

    private void AddMessageInCheat(String urlAvatar, String login, String message, String idMessage, String status) {
        Message message1 = new Message(urlAvatar, login, message, idMessage, status);
        messageAdapter.add(message1);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    public void buttonSendClick(View v) {
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

    private void SetMediaContainer(String typeMedia, String contentType, byte[] decodedBytes) {

        switch (typeMedia){
            case "image":
                SetImageView(decodedBytes);
                break;
            case "audio":
                break;
            case "video":
                try {
                    SetVideoView(decodedBytes, contentType);
                } catch (Exception ex) {
                    Log.i(TAG, ex.getMessage());
                }
                break;
            default:
                break;
        }
    }

    private  void SetImageView(byte[] decodedBytes){
        bMap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        Handler handler = new Handler(getBaseContext().getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(ImageView.VISIBLE);
                imageView.setImageBitmap(bMap);
            }
        });
    }

    private void SetVideoView(byte[] decodedBytes, String contentType) throws IOException {

        videoFile = new File(this.getFilesDir() + File.separator + "test." + contentType);
        OutputStream myOutputStream = new FileOutputStream(videoFile);
        myOutputStream.write(decodedBytes);
        myOutputStream.flush();
        myOutputStream.close();

        Handler handler = new Handler(getBaseContext().getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {


                MediaController mediaController = new MediaController(context);
                //videoView.setMediaController(mediaController);
                videoView.setVisibility(ImageView.VISIBLE);
                Log.i(TAG, videoFile.getAbsolutePath());
                //videoView.setVideoPath(videoFile.getAbsolutePath());
                try {
                    mp.setDataSource(videoFile.getAbsolutePath());
                    mp.prepare();

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mp.start();
            }
        });
    }

    public void onBackPressed() {
        finish();
    }
    @Override
    public void onStop(){
        Log.i(TAG, "onStop");
        mService.chatSend("leave chat");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

}