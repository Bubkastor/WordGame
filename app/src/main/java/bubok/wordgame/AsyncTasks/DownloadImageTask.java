package bubok.wordgame.AsyncTasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.net.URL;

import bubok.wordgame.Activity.Main;

/**
 * Created by bubok on 22.03.2016.
 */

public class DownloadImageTask extends AsyncTask<String, Void, String> {

    private final ImageView viewImage;

    public DownloadImageTask(ImageView viewImage) {
        this.viewImage = viewImage;
    }

    protected String doInBackground(String... urls) {
        String key = urls[0];
        String result = null;
        if (Main.storage.get(key) == null) {
            try {
                URL newUrl = new URL(urls[0]);
                Bitmap bitmap = BitmapFactory.decodeStream(newUrl.openConnection().getInputStream());
                Main.storage.put(key, bitmap);
                result = Main.storage.get(key);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
        } else {
            result = Main.storage.get(key);
        }
        return result;
    }

    protected void onPostExecute(String filePath) {
        viewImage.setImageURI(Uri.parse(filePath));
    }
}


