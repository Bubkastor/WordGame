package bubok.wordgame.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import bubok.wordgame.AsyncTasks.DownloadImageTask;
import bubok.wordgame.R;

public class Statistics extends AppCompatActivity {

    private View avatar;
    private View totalGame;
    private View totalTime;
    private View totalWins;
    private View leaderRaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        initView();

        Bundle intent =  getIntent().getExtras();

        String user_id = intent.getString(Main.EXTRA_MESSAGE_USED_ID_USER);
        String url = getString(R.string.URL) + getString(R.string.URL_Statistic);
        new ServerRequestJSONTask(user_id).execute(url);

    }
    private void initView(){
        avatar = findViewById(R.id.avatar);
        totalGame = findViewById(R.id.totalGame);
        totalTime = findViewById(R.id.totalTime);
        totalWins = findViewById(R.id.totalWins);
        leaderRaiting = findViewById(R.id.leaderRaiting);

    }

    public class ServerRequestJSONTask extends AsyncTask<String, Void, JSONObject> {
        private final static String TAG = "ServerRequestJSONTask";
        private String sendDate;

        public ServerRequestJSONTask(String sendDate) {
            this.sendDate = sendDate;
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
            try {
                if (result.has("AVATAR"))
                    new DownloadImageTask((ImageView) avatar).execute(result.getString("AVATAR"));

                if (result.has("TOTAL_GAME"))
                    ((TextView) totalGame).setText(getString(R.string.activity_Statistics_total_game) + result.get("TOTAL_GAME"));

                if(result.has("TOTAL_WINS"))
                    ((TextView) totalWins).setText(getString(R.string.activity_Statistics_total_wins) + result.get("TOTAL_WINS"));

                if(result.has("LEADING_RAITING"))
                    ((TextView) leaderRaiting).setText(getString(R.string.activity_Statistics_leader_rating) + result.get("LEADING_RAITING"));

                if(result.has("TOTAL_TIME"))
                    ((TextView) totalTime).setText(getString(R.string.activity_Statistics_total_time) + result.get("TOTAL_TIME"));


            }catch (Exception ex){
                Log.i(TAG, ex.getMessage());
            }
        }
    }


}
