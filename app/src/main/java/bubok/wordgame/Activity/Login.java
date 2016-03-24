package bubok.wordgame.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import bubok.wordgame.R;


public class Login extends AppCompatActivity {

    public final static String EXTRA_MESSAGE_TOKEN = "bubok.wordgame.TOKEN";
    public final static String EXTRA_MESSAGE_ID_USER = "bubok.wordgame.id.user";
    private static final String TAG = "LOGIN";
    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private Profile mProfile;
    public static  LruCache<String, Bitmap> mMemoryCache;
    private String idUser;
    private Profile profile;
    private ProfileTracker profileTracker;

    private AccessTokenTracker accessTokenTracker;
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

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
                Profile.fetchProfileForCurrentAccessToken();
                AccessToken.setCurrentAccessToken(currentAccessToken);
            }
        };

        accessTokenTracker.startTracking();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                // App code
                if (currentProfile != null) {
                    Profile.setCurrentProfile(currentProfile);
                    profile = currentProfile;
                }

            }
        };
        profileTracker.startTracking();

        LoginManager.getInstance().registerCallback(callbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.i(TAG, "onSuccess");

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
        Intent intent = new Intent(Login.this, Main.class);
        idUser = accessToken.getUserId();
        intent.putExtra(EXTRA_MESSAGE_ID_USER, idUser);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }

    @Override
    public void onStop() {
        super.onStop();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }
}


