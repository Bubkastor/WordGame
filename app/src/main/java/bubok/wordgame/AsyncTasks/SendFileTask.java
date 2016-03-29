package bubok.wordgame.AsyncTasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;


import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;


import bubok.wordgame.Activity.Login;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import okhttp3.Response;

/**
 * Created by bubok on 29.03.2016.
 */
public class SendFileTask extends AsyncTask<String, Void, String> {


    @Override
    protected String doInBackground(String... params) {
        try {
            OkHttpClient client = new OkHttpClient();
            File f = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyCameraVideo");
            f.createNewFile();

            Bitmap bitmap = Login.mMemoryCache.get(params[0]);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

            //                .url("http://192.168.0.102:3000/img")


        } catch (Exception ex) {
            Log.i("TAG", ex.getMessage());
        }
        return null;
    }
}
