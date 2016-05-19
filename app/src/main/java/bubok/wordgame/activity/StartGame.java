package bubok.wordgame.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.github.rtoshiro.view.video.FullscreenVideoLayout;

import net.gotev.uploadservice.MultipartUploadRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import bubok.wordgame.other.SingleUploadBroadcastReceiver;
import bubok.wordgame.other.User;
import bubok.wordgame.R;
import bubok.wordgame.service.SocketService;


public class StartGame extends AppCompatActivity implements SingleUploadBroadcastReceiver.Delegate {

    enum TYPE_MEDIA{
        IMAGE, VIDEO, AUDIO
    }

    public final static String EXTRA_MESSAGE_USERS_INVITE = "bubok.wordgame.users.invite";
    private static final String TAG = "START_GAME";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private static final int REQUEST_IMAGE_GALLERY = 3;

    private View countInvSend;
    private View countInvAccept;
    private View startGameProgress;
    private View progressBarLayout;
    private View buttonMediaLinear;
    private View mediaLinear;
    private View editTextSrcWord;
    private View imageViewPrev;
    private View videoViewPrev;
    private View timePrev;
    private View separatorTop;
    private View separatorBot;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String fileName;

    private TYPE_MEDIA media;
    private AlertDialog.Builder builder;

    private boolean firstClick = true;

    private Context context;

    private Intent service;

    private final SingleUploadBroadcastReceiver uploadReceiver =
            new SingleUploadBroadcastReceiver();


    private Uri mediaUri;
    private String gameId = "";
    private String mediaID = "";

    private static SocketService mService;
    private boolean mBound;

    private Chronometer chronometer;
    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
        context = StartGame.this;

        service = new Intent(this, SocketService.class);

        fileName = Environment.getExternalStorageDirectory() + "/record.3gpp";

        initView();
        initButton();
        initDialog();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
    }

    private boolean isPlay = false;

    private void initView() {
        buttonMediaLinear = findViewById(R.id.buttonMediaLinear);
        mediaLinear = findViewById(R.id.mediaLayout);
        imageViewPrev = findViewById(R.id.imageViewPrev);
        editTextSrcWord = findViewById(R.id.editTextSrcWord);
        videoViewPrev = findViewById(R.id.videoViewPrev);
        startGameProgress = findViewById(R.id.startGameProgress);
        progressBarLayout = findViewById(R.id.progressBarLayout);
        countInvSend = findViewById(R.id.countInvSend);
        countInvAccept = findViewById(R.id.countInvAccept);
        timePrev = findViewById(R.id.timePrev);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        separatorTop = findViewById(R.id.separatorTop);
        separatorBot = findViewById(R.id.separatorBot);
    }

    private void initButton() {
        findViewById(R.id.buttonAddAudio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.recordLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.playLayout).setVisibility(View.GONE);
                findViewById(R.id.audioLayout).setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.voiceButtons).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstClick) {
                    ((at.markushi.ui.CircleButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_black_48dp));
                    recordStart();
                    firstClick = false;
                } else {
                    ((at.markushi.ui.CircleButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_voice_black_48dp));
                    recordStop();
                    firstClick = true;
                    findViewById(R.id.recordLayout).setVisibility(View.GONE);
                    findViewById(R.id.playLayout).setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.playButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(v);
            }
        });

        findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((at.markushi.ui.CircleButton) findViewById(R.id.playButton)).setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp));
                isPlay = false;
                playStop();
                findViewById(R.id.recordLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.playLayout).setVisibility(View.GONE);
            }
        });

        findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.audioLayout).setVisibility(View.GONE);
                ((at.markushi.ui.CircleButton) findViewById(R.id.playButton)).setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp));
                setAudio();
            }
        });

        findViewById(R.id.playButtonPrev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(v);
            }
        });


        findViewById(R.id.buttonAddPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.show();
            }
        });

        findViewById(R.id.buttonAddFrends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.send("user online", gameId);
            }
        });

        findViewById(R.id.resetPrevView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPrevView();
            }
        });

        findViewById(R.id.buttonRunGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });

        findViewById(R.id.buttonAddVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAddVideoClick();
        }
        });


    }

    private void initDialog() {

        builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.photo_title));
        builder.setMessage(getString(R.string.photo_message));

        builder.setPositiveButton(getString(R.string.photo_button1), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cameraPhoto();
            }
        });

        builder.setNegativeButton(getString(R.string.photo_button2), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                galleryPhoto();
            }
        });
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(context, "Вы ничего не выбрали",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startChrono() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    private void stopChrono() {
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    private void setAudio() {
        changeVisibleMediaContainer();
        mediaLinear.setVisibility(View.VISIBLE);
        findViewById(R.id.audiPrev).setVisibility(View.VISIBLE);
        File f = new File(fileName);
        mediaUri = Uri.fromFile(f);
        media = TYPE_MEDIA.AUDIO;
    }

    private void playAudio(View v) {
        if (!isPlay) {
            ((at.markushi.ui.CircleButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_black_48dp));
            playStart();
            isPlay = true;
        } else {
            ((at.markushi.ui.CircleButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp));
            isPlay = false;
            playStop();
        }
    }

    private void recordStart() {
        try {
            releaseRecorder();
            startChrono();
            File outFile = new File(fileName);
            if (outFile.exists()) {
                outFile.delete();
            }

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(fileName);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void recordStop() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            stopChrono();
        }
    }

    private void playStart() {
        try {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                timePrev.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((TextView) timePrev).setText(getTimeString(mediaPlayer.getCurrentPosition()));
                                    }
                                });
                            } else {
                                timer.cancel();
                                timer.purge();
                            }
                        }
                    });
                }
            }, 0, 1000);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    View v;
                    if (findViewById(R.id.audioLayout).getVisibility() == View.VISIBLE) {
                        v = findViewById(R.id.playButton);
                    } else {
                        v = findViewById(R.id.playButtonPrev);
                    }

                    playAudio(v);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTimeString(long millis) {
        StringBuilder buf = new StringBuilder();
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf.append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }

    private void playStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart");
        uploadReceiver.register(this);
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        closeGame();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(StartGame.this);
                closeGame();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void closeGame() {
        Log.i(TAG, "closeGame");
        try {
            JSONObject sendData = new JSONObject();
            sendData.put("gameId", gameId);
            mService.send("cancel game", sendData);
        } catch (Exception ex) {
            Log.i(TAG, ex.getMessage());
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        if (intent.getExtras() != null) {
            ArrayList<String> usersInvite = intent.getExtras().getStringArrayList(EXTRA_MESSAGE_USERS_INVITE);
            try {
                ((TextView) countInvSend).setText(Integer.toString(usersInvite.size()));
                JSONObject sendData = new JSONObject();
                JSONArray players = new JSONArray(usersInvite);

                sendData.put("PLAYERS_IDS", players);
                sendData.put("gameId", gameId);
                mService.send("send invite", sendData);
            } catch (Exception ex) {
                Log.i(TAG, ex.getMessage());
            }
        }

        super.onNewIntent(intent);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.SocketIOBinder binder = (SocketService.SocketIOBinder) service;
            mService = binder.getService();
            binder.setGameListener(new SocketService.SocketStartGameListener() {
                @Override
                public void onInitializeGame(JSONObject jsonObject) {
                    Log.i(TAG, "onInitializeGame");
                    try {
                        gameId = jsonObject.getString("gameId");
                        Log.i(TAG, gameId);
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onInvAccept(JSONObject jsonObject) {
                    Log.i(TAG, "onInvAccept");
                    try {
                        String countPlayers = jsonObject.getString("countPlayers");
                        String username = jsonObject.getString("user");
                        acceptPlayer(username, countPlayers);

                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onInvCancel(JSONObject jsonObject) {
                    Log.i(TAG, "onInvCancel");
                    try {
                        String countPlayers = jsonObject.getString("countPlayers");
                        String username = jsonObject.getString("user");
                        cancelPlayer(username, countPlayers);
                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onOpenChat(JSONObject jsonObject) {
                    Log.i(TAG, "Chat open");
                    try {
                        String game = jsonObject.getString("gameId");
                        Intent intent = new Intent(StartGame.this, Chat.class);
                        intent.putExtra(Main.EXTRA_MESSAGE_USED_ID_USER, Main.idUSer);
                        intent.putExtra(Main.EXTRA_MESSAGE_USED_GAME, game);

                        startActivity(intent);

                    } catch (Exception ex) {
                        Log.i(TAG, ex.getMessage());
                    }
                }

                @Override
                public void onUserOnline(JSONArray jsonArray) {
                    openUsersOnline(jsonArray);
                }
            });
            if (gameId.equals(""))
                mService.send("initialize game");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    private void cancelPlayer(final String username, final String countPlayers) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) countInvAccept).setText(countPlayers);
                StringBuilder messageToast = new StringBuilder(username + " ");
                messageToast.append(getString(R.string.toast_cancel));
                Toast toast = Toast.makeText(getApplicationContext(),
                        messageToast.toString(), Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void acceptPlayer(final String username, final String countPlayers) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) countInvAccept).setText(countPlayers);
                StringBuilder messageToast = new StringBuilder(username + " ");
                messageToast.append(getString(R.string.toast_accept));
                Toast toast = Toast.makeText(getApplicationContext(),
                        messageToast.toString(), Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private void openUsersOnline(JSONArray jsonArray) {
        Log.i(TAG, "openUsersOnline");
        Intent intent = new Intent(StartGame.this, UserList.class);
        ArrayList<User> inviteList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                User itUser = new User(
                        jsonArray.getJSONObject(i).getString("userId"),
                        jsonArray.getJSONObject(i).getString("FIO"),
                        jsonArray.getJSONObject(i).getString("avatar")
                );
                inviteList.add(itUser);
            }
        } catch (Exception ex) {
            Log.i(TAG, ex.getMessage());
        }
        intent.putExtra("inviteList", inviteList);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            startGameProgress.setVisibility(show ? View.GONE : View.VISIBLE);
            progressBarLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            startGameProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            startGameProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            startGameProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    startGameProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                    progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    private void clearPrevView() {
        changeVisibleMediaContainer();
        mediaLinear.setVisibility(View.GONE);
        imageViewPrev.setVisibility(View.GONE);
        videoViewPrev.setVisibility(View.GONE);
        findViewById(R.id.audiPrev).setVisibility(View.GONE);
        ((TextView) timePrev).setText("00:00");
        media = null;
    }

    private void galleryPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void cameraPhoto() {
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhoto.resolveActivity((getPackageManager())) != null) {
            startActivityForResult(takePhoto, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void buttonAddVideoClick() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if(intent.resolveActivity((getPackageManager()))!= null){
            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void changeVisibleMediaContainer() {
        int optVisible;
        if (buttonMediaLinear.getVisibility() == LinearLayout.VISIBLE) {
            optVisible = LinearLayout.GONE;
        } else {
            optVisible = LinearLayout.VISIBLE;
        }
        buttonMediaLinear.setVisibility(optVisible);
        separatorTop.setVisibility(optVisible);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_IMAGE_CAPTURE:
                    showPreviewPhoto(data);
                    break;
                case REQUEST_VIDEO_CAPTURE:
                    showPreviewVideo(data);
                    break;
                case REQUEST_IMAGE_GALLERY:
                    showPreviewPhotoGallery(data);
                    break;
                default:

                    break;
            }
        }

    }

    private void showPreviewVideo(Intent data) {
        changeVisibleMediaContainer();
        mediaLinear.setVisibility(View.VISIBLE);
        videoViewPrev.setVisibility(View.VISIBLE);
        Uri videoUri = data.getData();
        try {
            ((FullscreenVideoLayout)videoViewPrev).setActivity(this);
            ((FullscreenVideoLayout)videoViewPrev).setVideoURI(videoUri);
        } catch (Exception e){
            Log.i(TAG, e.getMessage());
        }

        mediaUri = videoUri;
        media = TYPE_MEDIA.VIDEO;
    }

    private void showPreviewPhotoGallery(Intent data) {
        changeVisibleMediaContainer();
        mediaLinear.setVisibility(View.VISIBLE);
        imageViewPrev.setVisibility(View.VISIBLE);

        Uri selectedImage = data.getData();
        ((ImageView) imageViewPrev).setImageURI(selectedImage);
        mediaUri = selectedImage;
        media = TYPE_MEDIA.IMAGE;
    }

    private void showPreviewPhoto(Intent data) {
        changeVisibleMediaContainer();
        mediaLinear.setVisibility(View.VISIBLE);
        imageViewPrev.setVisibility(View.VISIBLE);

        Bitmap photo = (Bitmap) data.getExtras().get("data");
        ((ImageView) imageViewPrev).setImageBitmap(photo);
        mediaUri = getImageUri(getApplicationContext(), photo);

        media = TYPE_MEDIA.IMAGE;
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void startGame() {

        if(media == null)
            return;
        sendServer();
    }

    private void sendServer() {
        showProgress(true);
        Cursor cursor = getContentResolver().query(mediaUri, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        String folderPath = "";
        int idx = 0;
        String mediaPath;
        switch (media){
            case IMAGE:
                idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                folderPath = "img";
                mediaPath = cursor.getString(idx);
                break;
            case VIDEO:
                idx = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
                folderPath = "video";
                mediaPath = cursor.getString(idx);
                break;
            case AUDIO:
                mediaPath = fileName;
                folderPath = "audio";
                break;
            default:
                return;
        }
        //TODO Возможна ошибка проверить
        cursor.close();
        try {
            File f = new File(mediaPath);

            String uploadId = UUID.randomUUID().toString();
            uploadReceiver.setDelegate(this);
            uploadReceiver.setUploadID(uploadId);
            new MultipartUploadRequest(context, uploadId, getString(R.string.URL) + getString(R.string.URL_Upload))
                    .addFileToUpload(f.getAbsolutePath(), folderPath)
                    .setMaxRetries(2)
                    .startUpload();
        } catch (Exception ex){
            Log.i(TAG, ex.getMessage());
        }
    }

    @Override
    public void onStop(){
        Log.i(TAG, "onStop");
        uploadReceiver.unregister(this);
        if (mBound) {
            mService.deleteGameListener();
            unbindService(mConnection);
            mBound = false;
        }
        if (startGameProgress.getVisibility() == View.VISIBLE) {
            finish();
        }
        super.onStop();
    }

    @Override
    public void onCompleted(int serverResponseCode, byte[] serverResponseBody) {
        Log.i(TAG, "onCompleted");
        try {
            mediaID = new String(serverResponseBody, "UTF-8");
            Log.i(TAG, mediaID);
        } catch (Exception ex) {
            Log.i(TAG, ex.getMessage());
        }
        String word = ((TextView) editTextSrcWord).getText().toString();
        JSONObject sendData = new JSONObject();
        try {
            Log.i(TAG, "Start game");
            sendData.put("gameId", gameId);
            sendData.put("mediaId", mediaID);
            sendData.put("WORD", word);
            mService.send("start game", sendData);
        } catch (Exception ex) {
            Log.i(TAG, ex.getMessage());
        }

    }
}
