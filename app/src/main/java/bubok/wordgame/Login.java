package bubok.wordgame;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;


public class Login extends AppCompatActivity {

    public final static String EXTRA_MESSAGE_TOKEN = "bubok.wordgame.TOKEN";
    CallbackManager callbackManager;
    private AccessToken accessToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);
        accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null)
            OpenMainScreen();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.i("FACEBOOK", "onSuccess");
                        accessToken = loginResult.getAccessToken();
                        OpenMainScreen();
                    }

                    @Override
                    public void onCancel() {
                        Log.i("FACEBOOK", "onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.i("FACEBOOK", "onError");
                    }
                });
    }
    void OpenMainScreen(){
        Intent intent = new Intent(Login.this, main.class);
        intent.putExtra(EXTRA_MESSAGE_TOKEN, accessToken.getToken());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

