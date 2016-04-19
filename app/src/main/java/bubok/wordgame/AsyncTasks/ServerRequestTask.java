package bubok.wordgame.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by bubok on 25.03.2016.
 */
public class ServerRequestTask extends AsyncTask<String, Void, Boolean> {
    private final static String TAG = "ServerRequestTask";
    private final String sendDate;

    public ServerRequestTask(String sendDate) {
        this.sendDate = sendDate;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            URL url = new URL(params[0] + sendDate);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                urlConnection.disconnect();
                return true;
            } else {
                urlConnection.disconnect();
                return false;
            }

        } catch (Exception ex) {
            Log.i(TAG, ex.getMessage());
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean answer) {
        Log.i(TAG, "Result: " + answer);
    }
}
