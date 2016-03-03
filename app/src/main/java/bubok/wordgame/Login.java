package bubok.wordgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Login extends AppCompatActivity {
    private EditText editTextlogin;
    public final static String EXTRA_MESSAGE_LOGIN = "bubok.wodgame.login";
    public final static String EXTRA_MESSAGE_REGISTER = "bubok.wodgame.REGISTER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent intent = getIntent();


        editTextlogin = (EditText) findViewById(R.id.editTextLogin);
        if (intent.getExtras() != null) {
            Boolean registerLogin = intent.getExtras().getBoolean(main.EXTRA_MESSAGE_USED_ACCOUNT);
            String message = intent.getExtras().getString(main.EXTRA_MESSAGE_USED_ERROR);
            if (registerLogin) editTextlogin.setError(message);
        }

    }

    public void ButtonEnterClick(View v){
        Intent intent = new Intent(Login.this, main.class);
        String message =  editTextlogin.getText().toString();
        intent.putExtra(EXTRA_MESSAGE_LOGIN, message);
        startActivity(intent);
    }
    public void ButtonRegisterClick(View v){
        Intent intent = new Intent(Login.this, main.class);
        String message =  editTextlogin.getText().toString();
        intent.putExtra(EXTRA_MESSAGE_LOGIN, message);
        intent.putExtra(EXTRA_MESSAGE_REGISTER, true);
        startActivity(intent);
    }
}

