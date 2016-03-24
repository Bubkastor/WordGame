package bubok.wordgame.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.TextView;

import bubok.wordgame.R;

public class WinGame extends AppCompatActivity {
    private static final String TAG = "WIN_GAME";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win_game);
        TextView textViewMessage = (TextView) findViewById(R.id.textViewMessage);
        Intent intent = getIntent();
        textViewMessage.setText(intent.getStringExtra(Chat.EXTRA_MESSAGE_WINGAME));
        Log.i(TAG, "onCreate");
    }

    public void buttonMainMenuClick(View v){
        Log.i(TAG, "buttonMainMenuClick");
        finish();
    }
}
