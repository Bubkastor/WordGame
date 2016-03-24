package bubok.wordgame.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import bubok.wordgame.AsyncTasks.DownloadImageTask;
import bubok.wordgame.R;

public class WinGame extends AppCompatActivity {
    private static final String TAG = "WIN_GAME";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win_game);

        Intent intent = getIntent();

        TextView textViewWord = (TextView) findViewById(R.id.textViewWord);
        textViewWord.setText(intent.getStringExtra(Chat.EXTRA_MESSAGE_WORD));

        TextView textViewLeadName = (TextView) findViewById(R.id.textViewLeadName);
        textViewLeadName.setText(intent.getStringExtra(Chat.EXTRA_MESSAGE_LEAD_NAME));

        TextView textViewWinerName = (TextView) findViewById(R.id.textViewWinerName);
        textViewWinerName.setText(intent.getStringExtra(Chat.EXTRA_MESSAGE_WIN_NAME));

        TextView textViewTime = (TextView) findViewById(R.id.textViewTime);
        textViewTime.setText(intent.getStringExtra(Chat.EXTRA_MESSAGE_TIME));

        ImageView imageViewWiner = (ImageView) findViewById(R.id.imageViewWiner);

        new DownloadImageTask(imageViewWiner).execute(intent.getStringExtra(Chat.EXTRA_MESSAGE_WIN_AVATAR));

        ImageView imageViewLeader = (ImageView) findViewById(R.id.imageViewLeader);
        new DownloadImageTask(imageViewLeader).execute(intent.getStringExtra(Chat.EXTRA_MESSAGE_LEAD_AVATAR));

        Log.i(TAG, "onCreate");
    }

    public void buttonMainMenuClick(View v){
        Log.i(TAG, "buttonMainMenuClick");
        finish();
    }
}
