package bubok.wordgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Login extends AppCompatActivity {
    private EditText editTextlogin;
    public final static String EXTRA_MESSAGE = "bubok.wodgame.login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editTextlogin = (EditText) findViewById(R.id.editTextLogin);
    }

    public void ButtonEnterClick(View v){
        Intent intent = new Intent(Login.this, Cheat.class);
        String message =  editTextlogin.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
