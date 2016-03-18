package bubok.wordgame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;

import com.facebook.AccessToken;
import com.facebook.AccessTokenSource;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


public class Login extends AppCompatActivity {

    public final static String EXTRA_MESSAGE_TOKEN = "bubok.wordgame.TOKEN";
    public final static String EXTRA_MESSAGE_PROFILE = "bubok.wordgame.PROFILE";
    private static final String TAG = "LOGIN";
    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private Profile mProfile;
    public static  LruCache<String, Bitmap> mMemoryCache;
    private ProfileTracker profileTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        loginButton.setReadPermissions("public_profile");

        accessToken = AccessToken.getCurrentAccessToken();
        //cache image
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return bitmap.getByteCount() / 1024;
            }
        };

        LoginManager.getInstance().registerCallback(callbackManager,
            new FacebookCallback<LoginResult>() {
                private ProfileTracker mProfileTracker;
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.i(TAG, "onSuccess");
                    if(Profile.getCurrentProfile() == null) {
                        mProfileTracker = new ProfileTracker() {
                            @Override
                            protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                                // profile2 is the new profile
                                Log.v("facebook - profile", profile2.getFirstName());
                                mProfile = profile2;
                                mProfileTracker.stopTracking();
                            }
                        };
                    }
                    mProfileTracker.startTracking();
                    accessToken = loginResult.getAccessToken();

                    OpenMainScreen();
                }

                @Override
                public void onCancel() {
                    Log.i(TAG, "onCancel");
                }

                @Override
                public void onError(FacebookException exception) {
                    Log.i(TAG, "onError");
                }
            });
        mProfile = Profile.getCurrentProfile();
        if (accessToken != null) {
            OpenMainScreen();
        }
    }

    private void OpenMainScreen() {
        Intent intent = new Intent(Login.this, main.class);
        intent.putExtra(EXTRA_MESSAGE_TOKEN, accessToken.getToken());
        mProfile = Profile.getCurrentProfile();
        intent.putExtra(EXTRA_MESSAGE_PROFILE, mProfile);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}


