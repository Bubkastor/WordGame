package bubok.wordgame;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.List;

public class StartGame extends AppCompatActivity {

    enum TYPE_MEDIA{
        IMAGE, VIDEO, AUDIO
    }

    public final static String EXTRA_MESSAGE_USERS_INVITE = "bubok.wordgame.users.invite";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;

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
    private List<String> usersInvite;

    private static File mediaFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        titleLinear = (LinearLayout) findViewById(R.id.titleLinear);
        buttonMediaLinear = (LinearLayout) findViewById(R.id.buttonMediaLinear);
        mediaLinear = (LinearLayout) findViewById(R.id.mediaLayout);
        imageViewPrev = (ImageView) findViewById(R.id.imageViewPrev);
        editTextSrcWord = (EditText) findViewById(R.id.editTextSrcWord);
        videoViewPrev = (VideoView) findViewById(R.id.videoViewPrev);

        mediaController = new MediaController(this);
        videoViewPrev.setMediaController(mediaController);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
        Intent intent = getIntent();
        if(intent.getExtras()!= null){
            usersInvite = intent.getExtras().getStringArrayList(EXTRA_MESSAGE_USERS_INVITE);
        }


    }

    public void buttonAddPhotoClick(View v){
        Intent takePhoto= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePhoto.resolveActivity((getPackageManager()))!= null){
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

            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");

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
                default:
                    break;
            }
        }

    }

    private  void ShowPreviewVideo(Intent data){
        ChangeVisibleMediaConteiner();
        videoByte =  VideoToByte(data);
        try{
            RandomAccessFile f = new RandomAccessFile(mediaFile, "r");
            videoByte = new byte[(int)f.length()];
            f.read(videoByte);
        } catch (Exception ex){
            Log.i("RandomAccessFile", ex.getMessage());
        }
        mediaLinear.setVisibility(View.VISIBLE);
        videoViewPrev.setVisibility(View.VISIBLE);
        videoViewPrev.setMediaController(mediaController);
        Uri videoUri = data.getData();
        videoViewPrev.setVideoURI(videoUri);
        media = TYPE_MEDIA.VIDEO;
    }

    private byte[] VideoToByte(Intent data){
        byte[] result = "byte".getBytes();
        Uri videoUri = data.getData();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(videoUri.getPath());

            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fileInputStream.read(buf))) {
                out.write(buf, 0, n);
            }
            result = out.toByteArray();
        }catch (Exception ex){
            Log.i("VIDEO", ex.getMessage());
        }
        return result;
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
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        return baos.toByteArray();
    }

    public void buttonAddFrendsClick(View v){


    }

    public void startGame(View v) {
        byte [] data = "not empty".getBytes();
        String typeMedia = "";
        String type = "";
        switch (media){
            case IMAGE:
                data = BitMapToByte(prevImage);
                typeMedia = "png";
                type = "image";
                break;
            case VIDEO:
                data = videoByte;
                typeMedia = "mp4";
                type = "video";
                break;
            default:
                break;
        }

        String word = editTextSrcWord.getText().toString();
        JSONObject sendData = new JSONObject();
        try{
            sendData.put("DATA", data);
            sendData.put("WORD", word);
            sendData.put("CONTENT_TYPE", typeMedia);
            sendData.put("TYPE", type);
            sendData.put("PLAYERS_IDS" , usersInvite);
            main.mSocket.emit("start game", sendData);
        } catch (Exception ex){
            Log.i("START GAME", ex.getMessage());
        }
    }

}
