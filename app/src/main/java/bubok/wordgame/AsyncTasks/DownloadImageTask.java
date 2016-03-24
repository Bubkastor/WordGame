package bubok.wordgame.AsyncTasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.net.URL;

import bubok.wordgame.Activity.Login;

/**
 * Created by bubok on 22.03.2016.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private ImageView viewImage;

    public DownloadImageTask(ImageView viewImage) {
        this.viewImage = viewImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String key = urls[0];
        Bitmap result = null;
        if (Login.mMemoryCache.get(key) == null) {
            try {
                URL newUrl = new URL(urls[0]);
                result = BitmapFactory.decodeStream(newUrl.openConnection().getInputStream());
                Login.mMemoryCache.put(key, result);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
        } else {
            result = Login.mMemoryCache.get(key);
        }
        return result;
    }

    protected void onPostExecute(Bitmap result) {
        viewImage.setImageBitmap(result);
    }
}


