package bubok.wordgame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import io.socket.client.Socket;

public class StartGame extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private LinearLayout titleLinear;
    private LinearLayout buttonMediaLinear;
    private LinearLayout mediaLinear;
    private ImageView imageViewPrev;
    private Socket mSocket;
    private static Bitmap prevImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
        titleLinear = (LinearLayout) findViewById(R.id.titleLinear);
        buttonMediaLinear = (LinearLayout) findViewById(R.id.buttonMediaLinear);
        mediaLinear = (LinearLayout) findViewById(R.id.mediaLayout);
        imageViewPrev = (ImageView) findViewById(R.id.imageViewPrev);

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
        imageViewPrev.setImageBitmap(prevImage);
    }

    public void buttonAddFrendsClick(View v){


    };
    public void StartGame(View v){

    };

}
