package bubok.wordgame.activity2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.Toast;

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
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import bubok.wordgame.R;


public class Login extends AppCompatActivity {

    public final static String EXTRA_MESSAGE_TOKEN = "bubok.wordgame.TOKEN";
    public final static String EXTRA_MESSAGE_ID_USER = "bubok.wordgame.id.user";
    public final static String EXTRA_MESSAGE_ID_SOCIAL = "bubok.wordgame.social";
    private static final String TAG = "LOGIN";
    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private static  LruCache<String, Bitmap> mMemoryCache;
    private Profile profile;
    private ProfileTracker profileTracker;

    private String[] scope = new String[] {
            VKScope.FRIENDS, VKScope.EMAIL, VKScope.STATUS
    };

    public void VKLogIn(View v){
        VKSdk.login(this, scope);
    }

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
                Profile.fetchProfileForCurrentAccessToken();
                AccessToken.setCurrentAccessToken(currentAccessToken);
            }
        };

        accessTokenTracker.startTracking();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
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

                    OpenMainScreen("fb");
                }

                @Override
                public void onCancel() {
                    Log.i(TAG, "onCancel");
                }

                @Override
                public void onError(FacebookException exception) {
                    Log.i(TAG, "onError");
                    Log.i(TAG, exception.getMessage());
                }
            });
        Profile mProfile = Profile.getCurrentProfile();
        if (accessToken != null) {
            OpenMainScreen("fb");
        } else if (VKSdk.isLoggedIn()) {
            OpenMainScreen("vk");
        }
    }

    private void OpenMainScreen(String socialNetwork) {
        Intent intent = new Intent(Login.this, Main.class);

        if (socialNetwork.equals("fb")) {
            String idUser = accessToken.getUserId();
            intent.putExtra(EXTRA_MESSAGE_ID_USER, idUser);
            intent.putExtra(EXTRA_MESSAGE_ID_SOCIAL, socialNetwork);
        } else if (socialNetwork.equals("vk")) {
            //String idUser = accessToken.getUserId();
            //intent.putExtra(EXTRA_MESSAGE_ID_USER, idUser);
            intent.putExtra(EXTRA_MESSAGE_ID_SOCIAL, socialNetwork);
        } else {
            return;
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

            @Override
            public void onResult(VKAccessToken res) {
                OpenMainScreen("vk");
                Toast.makeText(getApplicationContext(), "Good", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        }))
        {
            super.onActivityResult(requestCode, resultCode, data);
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
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


