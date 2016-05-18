package bubok.wordgame.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import bubok.wordgame.asyncTasks.ServerRequestTask;
import bubok.wordgame.R;

public class WinGame extends AppCompatActivity {
    private static final String TAG = "WIN_GAME";
    private final StringBuilder sendDate = new StringBuilder();
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win_game);
        context = this;
        Intent intent = getIntent();

        TextView textViewWord = (TextView) findViewById(R.id.textViewWord);
        textViewWord.setText(intent.getStringExtra(Chat.EXTRA_MESSAGE_WORD));

        TextView textViewLeadName = (TextView) findViewById(R.id.textViewLeadName);
        textViewLeadName.setText(intent.getStringExtra(Chat.EXTRA_MESSAGE_LEAD_NAME));



        if (intent.getStringExtra(Chat.EXTRA_MESSAGE_WIN_NAME) != null){
            TextView textViewWinerName = (TextView) findViewById(R.id.textViewWinerName);
            textViewWinerName.setText(intent.getStringExtra(Chat.EXTRA_MESSAGE_WIN_NAME));
            ImageView imageViewWiner = (ImageView) findViewById(R.id.imageViewWiner);
            Picasso.with(context).load(intent.getStringExtra(Chat.EXTRA_MESSAGE_WIN_AVATAR)).into(imageViewWiner);
        } else {
            leaveAdmin();
        }


        TextView textViewTime = (TextView) findViewById(R.id.textViewTime);
        textViewTime.setText(intent.getStringExtra(Chat.EXTRA_MESSAGE_TIME));



        ImageView imageViewLeader = (ImageView) findViewById(R.id.imageViewLeader);
        Picasso.with(context).load(intent.getStringExtra(Chat.EXTRA_MESSAGE_LEAD_AVATAR)).into(imageViewLeader);

        Boolean isAdmin = intent.getExtras().getBoolean(Chat.EXTRA_MESSAGE_LEAD_IS_ADMIN);
        if (isAdmin){
            findViewById(R.id.afterRatingLayout).setVisibility(View.GONE);
            findViewById(R.id.ratingLayout).setVisibility(View.GONE);
        }

        sendDate.append(intent.getStringExtra(Chat.EXTRA_MESSAGE_LEAD_ID));
        final String likeSendDate = sendDate.toString() + getString(R.string.Raiting_plus);
        final String url = getString(R.string.URL) + getString(R.string.URL_Raiting);
        ImageButton imageButtonLike = (ImageButton) findViewById(R.id.imageButtonLike);
        imageButtonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRaiting();
                new ServerRequestTask(likeSendDate).execute(url);
            }
        });
        final String disLikeSendDate = sendDate.toString() + getString(R.string.Raiting_minus);
        sendDate.append("");
        ImageButton imageButtonDislike = (ImageButton) findViewById(R.id.imageButtonDislike);
        imageButtonDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRaiting();
                new ServerRequestTask(disLikeSendDate).execute(url);
            }
        });

        Log.i(TAG, "onCreate");
    }

    private void leaveAdmin() {
        findViewById(R.id.winerContent).setVisibility(View.GONE);
        findViewById(R.id.leaveContent).setVisibility(View.VISIBLE);
    }

    public void buttonMainMenuClick(View v){
        Log.i(TAG, "buttonMainMenuClick");
        finish();
    }

    private void hideRaiting() {
        findViewById(R.id.afterRatingLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.ratingLayout).setVisibility(View.GONE);
    }


}
