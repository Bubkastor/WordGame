    package bubok.wordgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

    public class WinGame extends AppCompatActivity {
    private TextView textViewMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win_game);
        textViewMessage = (TextView) findViewById(R.id.textViewMessage);
        Intent intent = getIntent();
        textViewMessage.setText(intent.getStringExtra(Chat.EXTRA_MESSAGE_WINGAME));
    }

    public void buttonMainMenuClick(View v){
        Intent intent = new Intent(this, main.class);
        startActivity(intent);
    }
}
