package bubok.wordgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import bubok.wordgame.Service.SocketService;

public class StartGame extends AppCompatActivity {

    enum TYPE_MEDIA{
        IMAGE, VIDEO, AUDIO
    }

    public final static String EXTRA_MESSAGE_USERS_INVITE = "bubok.wordgame.users.invite";
    private static final String TAG = "START_GAME";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private static final int REQUEST_IMAGE_GALLERY = 3;

    private TextView countInvSend;
    private TextView countInvAccept;

    private LinearLayout titleLinear;
    private LinearLayout buttonMediaLinear;
    private LinearLayout mediaLinear;
    private EditText editTextSrcWord;
    private ImageView imageViewPrev;
    private VideoView videoViewPrev;
    private static Bitmap prevImage;
    private MediaController mediaController;
    private byte[] videoByte;
    private TYPE_MEDIA media;
    private ArrayList<String> usersInvite = new ArrayList<>();
    private AlertDialog.Builder builder;
    private static File mediaFile;
    private View startGameProgress;
    private View progressBarLayout;
    private Context context;
    private boolean mBound;
    private Intent service;


    private static String gameId = "";
    private static SocketService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
        context = StartGame.this;

        service = new Intent(this, SocketService.class);


        String title = "Выбор картинки";
        String message = "Выберите откуда взять картинку";
        String button1String = "Камера";
        String button2String = "Галерея";
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cameraPhoto();
            }
        });
        builder.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
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

        titleLinear = (LinearLayout) findViewById(R.id.titleLinear);
        buttonMediaLinear = (LinearLayout) findViewById(R.id.buttonMediaLinear);
        mediaLinear = (LinearLayout) findViewById(R.id.mediaLayout);
        imageViewPrev = (ImageView) findViewById(R.id.imageViewPrev);
        editTextSrcWord = (EditText) findViewById(R.id.editTextSrcWord);
        videoViewPrev = (VideoView) findViewById(R.id.videoViewPrev);
        startGameProgress = findViewById(R.id.startGameProgress);
        progressBarLayout = findViewById(R.id.progressBarLayout);
        countInvSend = (TextView) findViewById(R.id.countInvSend);
        countInvAccept = (TextView) findViewById(R.id.countInvAccept);
        ImageButton resetPrevView = (ImageButton) findViewById(R.id.resetPrevView);
        resetPrevView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPrevView();
            }
        });
        mediaController = new MediaController(this);
        videoViewPrev.setMediaController(mediaController);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);

        findViewById(R.id.buttonAddFrends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.send("user online");
            }
        });
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        if (intent.getExtras() != null) {
            usersInvite = intent.getExtras().getStringArrayList(EXTRA_MESSAGE_USERS_INVITE);
            countInvSend.setText(Integer.toString(usersInvite.size()));
            try {
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

    private ServiceConnection mConnection = new ServiceConnection() {

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
                        intent.putExtra(main.EXTRA_MESSAGE_USED_ID_USER, main.idUSer);
                        intent.putExtra(main.EXTRA_MESSAGE_USED_GAME, game);

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
                countInvAccept.setText(countPlayers);
                StringBuilder messageToast = new StringBuilder();
                messageToast.append(username + " ");
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
                countInvAccept.setText(countPlayers);
                StringBuilder messageToast = new StringBuilder();
                messageToast.append(username + " ");
                messageToast.append(getString(R.string.toast_accept));
                Toast toast = Toast.makeText(getApplicationContext(),
                        messageToast.toString(), Toast.LENGTH_SHORT);
                toast.show();
            }
        });

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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            //mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void clearPrevView() {
        ChangeVisibleMediaConteiner();
        mediaLinear.setVisibility(View.GONE);
        imageViewPrev.setVisibility(View.GONE);
        videoViewPrev.setVisibility(View.GONE);
        prevImage = null;
        media = null;
    }
    private void galleryPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    public void showChoicePhoto(View v) {
        builder.show();
    }

    private void cameraPhoto() {
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhoto.resolveActivity((getPackageManager())) != null) {
            startActivityForResult(takePhoto, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void buttonAddVideoClick(View v){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Uri fileUri = getOutputMediaFileUri(REQUEST_VIDEO_CAPTURE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        if(intent.resolveActivity((getPackageManager()))!= null){
            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private static Uri getOutputMediaFileUri(int type){

        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type){

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraVideo");

        if (! mediaStorageDir.exists()){

            if (! mediaStorageDir.mkdirs()){

                Log.d("MyCameraVideo", "Failed to create directory MyCameraVideo.");
                return null;
            }
        }

        java.util.Date date= new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());


        if(type == REQUEST_VIDEO_CAPTURE) {

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".3gp");

        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_IMAGE_CAPTURE:
                    ShowPreviewPhoto(data);
                    break;
                case REQUEST_VIDEO_CAPTURE:
                    ShowPreviewVideo(data);
                    break;
                case REQUEST_IMAGE_GALLERY:
                    ShowPreviewPhotoGallery(data);
                    break;
                default:

                    break;
            }
        }

    }

    private  void ShowPreviewVideo(Intent data){
        ChangeVisibleMediaConteiner();
        try{
            RandomAccessFile f = new RandomAccessFile(mediaFile, "r");
            videoByte = new byte[(int)f.length()];
            f.read(videoByte);
        } catch (Exception ex){
            Log.i(TAG, ex.getMessage());
        }
        mediaLinear.setVisibility(View.VISIBLE);
        videoViewPrev.setVisibility(View.VISIBLE);
        videoViewPrev.setMediaController(mediaController);
        videoViewPrev.setVideoPath(mediaFile.getAbsolutePath());
        media = TYPE_MEDIA.VIDEO;
    }

    private void ChangeVisibleMediaConteiner() {
        int optVisable;
        if (titleLinear.getVisibility() == LinearLayout.VISIBLE){
            optVisable = LinearLayout.GONE;
        } else {
            optVisable = LinearLayout.VISIBLE;
        }
        titleLinear.setVisibility(optVisable);
        buttonMediaLinear.setVisibility(optVisable);
    }

    private void ShowPreviewPhotoGallery(Intent data) {
        ChangeVisibleMediaConteiner();

        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        prevImage = BitmapFactory.decodeFile(picturePath);
        mediaLinear.setVisibility(View.VISIBLE);
        imageViewPrev.setVisibility(View.VISIBLE);
        imageViewPrev.setImageBitmap(prevImage);
        media = TYPE_MEDIA.IMAGE;
    }

    private void ShowPreviewPhoto(Intent data){
        ChangeVisibleMediaConteiner();
        Bundle extras = data.getExtras();
        prevImage = (Bitmap) extras.get("data");
        mediaLinear.setVisibility(View.VISIBLE);
        imageViewPrev.setVisibility(View.VISIBLE);
        imageViewPrev.setImageBitmap(prevImage);
        media = TYPE_MEDIA.IMAGE;
    }

    private byte[] BitMapToByte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        return baos.toByteArray();
    }

    public void startGame(View v) {
        byte[] data;
        String typeMedia;
        String type;
        if(media == null)
            return;
        switch (media){
            case IMAGE:
                showProgress(true);
                data = BitMapToByte(prevImage);
                typeMedia = "jpeg";
                type = "image";
                break;
            case VIDEO:
                showProgress(true);
                data = videoByte;
                typeMedia = "3gp";
                type = "video";
                break;
            default:
                return;

        }

        String word = editTextSrcWord.getText().toString();
        JSONObject sendData = new JSONObject();
        try{
            Log.i(TAG, "Start game");
            sendData.put("gameId", gameId);
            sendData.put("DATA", data);
            sendData.put("WORD", word);
            sendData.put("CONTENT_TYPE", typeMedia);
            sendData.put("TYPE", type);
            mService.send("start game", sendData);
        } catch (Exception ex){
            Log.i(TAG, ex.getMessage());
        }
    }

    @Override
    public void onStop(){
        Log.i(TAG, "onStop");

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
}
