package bubok.wordgame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.util.List;

import io.socket.client.Socket;

public class StartGame extends AppCompatActivity {

    enum TYPE_MEDIA{
        IMAGE, VIDEO, AUDIO
    }

    public final static String EXTRA_MESSAGE_USERS_INVITE = "bubok.wordgame.users.invite";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private LinearLayout titleLinear;
    private LinearLayout buttonMediaLinear;
    private LinearLayout mediaLinear;
    private EditText editTextSrcWord;
    private ImageView imageViewPrev;
    private static Bitmap prevImage;
    private TYPE_MEDIA media;
    private List<String> usersInvite;
    private Socket mSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        titleLinear = (LinearLayout) findViewById(R.id.titleLinear);
        buttonMediaLinear = (LinearLayout) findViewById(R.id.buttonMediaLinear);
        mediaLinear = (LinearLayout) findViewById(R.id.mediaLayout);
        imageViewPrev = (ImageView) findViewById(R.id.imageViewPrev);
        editTextSrcWord = (EditText) findViewById(R.id.editTextSrcWord);

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_IMAGE_CAPTURE:
                        ShowPreviewPhoto(data);
                    break;
                default:
                    break;
            }
        }

    }
    private void ChangeVisableMediaConteiner(){
        int optVisable = 0;
        if (titleLinear.getVisibility() == LinearLayout.VISIBLE){
            optVisable = LinearLayout.GONE;
        } else {
            optVisable = LinearLayout.VISIBLE;
        }
        titleLinear.setVisibility(optVisable);
        buttonMediaLinear.setVisibility(optVisable);


    }
    private void ShowPreviewPhoto(Intent data){
        ChangeVisableMediaConteiner();
        Bundle extras = data.getExtras();
        prevImage = (Bitmap) extras.get("data");
        mediaLinear.setVisibility(View.VISIBLE);
        imageViewPrev.setVisibility(View.VISIBLE);
        imageViewPrev.setImageBitmap(prevImage);
        media = TYPE_MEDIA.IMAGE;
    }
    public byte[] BitMapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        return b;
    }

    public void buttonAddFrendsClick(View v){


    };
    public void StartGame(View v){
        byte [] data = "not empty".getBytes();
        String typeMedia = "";
        String type = "";
        switch (media){
            case IMAGE:{
                data = BitMapToByte(prevImage);
                typeMedia = "png";
                type = "image";
                break;
            }
            case VIDEO:
                break;
            default:
                ;
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


    };

}
