package bubok.wordgame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.net.URL;

/**
 * Created by bubok on 22.03.2016.
 */
class AsyncTasks {

    static final class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView viewImage;

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
}

