package bubok.wordgame.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import bubok.wordgame.AsyncTasks.DownloadImageTask;
import bubok.wordgame.AsyncTasks.ServerRequestTask;
import bubok.wordgame.R;

public class WinGame extends AppCompatActivity {
    private static final String TAG = "WIN_GAME";
    private StringBuilder sendDate = new StringBuilder();
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

        sendDate.append(intent.getStringExtra(Chat.EXTRA_MESSAGE_LEAD_ID));
        final String likeSendDate = sendDate.toString() + getString(R.string.Raiting_plus);

        final String url = getString(R.string.URL_Raiting);
        ImageButton imageButtonLike = (ImageButton) findViewById(R.id.imageButtonLike);
        imageButtonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new ServerRequestTask(likeSendDate).execute(url);
            }
        });
        final String disLikeSendDate = sendDate.toString() + getString(R.string.Raiting_minus);
        sendDate.append("");
        ImageButton imageButtonDislike = (ImageButton) findViewById(R.id.imageButtonDislike);
        imageButtonDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new ServerRequestTask(disLikeSendDate).execute(url);
            }
        });

        Log.i(TAG, "onCreate");
    }

    public void buttonMainMenuClick(View v){
        Log.i(TAG, "buttonMainMenuClick");
        finish();
    }


}
