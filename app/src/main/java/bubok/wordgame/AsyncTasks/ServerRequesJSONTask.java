package bubok.wordgame.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by bubok on 14.04.2016.
 */
class ServerRequesJSONTask extends AsyncTask<String, Void, JSONObject> {
    private final static String TAG = "ServerRequesJSONTask";
    private final String sendDate;
    private JSONObject answer;

    public ServerRequesJSONTask(String sendDate, JSONObject answer) {
        this.sendDate = sendDate;
        this.answer = answer;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        JSONObject result = new JSONObject();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(params[0] + sendDate);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            result = new JSONObject(stringBuilder.toString());

            if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {

                urlConnection.disconnect();
            } else {
                urlConnection.disconnect();
            }

        } catch (Exception ex) {
            Log.i(TAG, ex.getMessage());
        }

        return result;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        Log.i(TAG, "Result: " + result);
        this.answer = result;
    }
}
