package bubok.wordgame.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.gorbin.asne.core.SocialNetwork;
import com.github.gorbin.asne.core.SocialNetworkManager;
import com.github.gorbin.asne.core.listener.OnLoginCompleteListener;
import com.github.gorbin.asne.vk.VkSocialNetwork;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;
import com.github.gorbin.asne.twitter.TwitterSocialNetwork;
import com.vk.sdk.VKScope;

import java.util.List;

import bubok.wordgame.R;



public class Login extends Fragment implements SocialNetworkManager.OnInitializationCompleteListener,
        OnLoginCompleteListener {

    public static SocialNetworkManager mSocialNetworkManager;
    private SocialNetwork socialNetwork;

    private static final String TAG = "LOGIN";

    private Button vk;
    private Button fb;
    private Button tw;
    private Button exit;

    public Login(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        vk = (Button) rootView.findViewById(R.id.vk);
        fb = (Button) rootView.findViewById(R.id.fb);
        tw = (Button) rootView.findViewById(R.id.tw);
        exit = (Button) rootView.findViewById(R.id.exit);

        exit.setOnClickListener(exitClick);
        vk.setOnClickListener(loginClick);
        fb.setOnClickListener(loginClick);
        tw.setOnClickListener(loginClick);

        String VK_KEY = getResources().getString(R.string.vk_app_id);

        String[] vkScope = new String[] {
                VKScope.FRIENDS,
                VKScope.WALL,
                VKScope.PHOTOS,
                VKScope.NOHTTPS,
                VKScope.STATUS,
        };
        if (mSocialNetworkManager == null){
            mSocialNetworkManager = new SocialNetworkManager();

            VkSocialNetwork vkNetwork = new VkSocialNetwork(this, VK_KEY, vkScope);
            mSocialNetworkManager.addSocialNetwork(vkNetwork);

            getFragmentManager().beginTransaction().add(mSocialNetworkManager, Main.SOCIAL_NETWORK_TAG).commit();
            mSocialNetworkManager.setOnInitializationCompleteListener(this);
        } else {
            if(!mSocialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
                List<SocialNetwork> socialNetworks = mSocialNetworkManager.getInitializedSocialNetworks();
                for (SocialNetwork socialNetwork : socialNetworks) {
                    socialNetwork.setOnLoginCompleteListener(this);
                    initSocialNetwork(socialNetwork);
                }
            }
        }

        return rootView;

    }

    private void initSocialNetwork(SocialNetwork socialNetwork){
        if(socialNetwork.isConnected()){
            switch (socialNetwork.getID()){
                case VkSocialNetwork.ID:
                    this.socialNetwork = socialNetwork;
                    vk.setText("Show VK profile");
                    //startProfile(socialNetwork.getID());
                    break;
            }
        }
    }
    private View.OnClickListener exitClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vk.setText("Login VK");
            fb.setText("Login FB");
            tw.setText("Login TW");
            socialNetwork.logout();
        }
    };

    private View.OnClickListener  loginClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int networkId = 0;
            switch (v.getId()){
                case R.id.vk:
                    networkId = VkSocialNetwork.ID;
                    break;
                case R.id.fb:
                    networkId = FacebookSocialNetwork.ID;
                    break;
                case R.id.tw:
                    networkId = TwitterSocialNetwork.ID;
                    break;
            }
            SocialNetwork socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
            if(!socialNetwork.isConnected())
                if(networkId != 0)
                    socialNetwork.requestLogin();
        }
    };

    @Override
    public void onSocialNetworkManagerInitialized() {
        for (SocialNetwork socialNetwork : mSocialNetworkManager.getInitializedSocialNetworks()) {
            socialNetwork.setOnLoginCompleteListener(this);
            initSocialNetwork(socialNetwork);

        }
    }

    @Override
    public void onLoginSuccess(int socialNetworkID) {
        getFragmentManager().beginTransaction().hide(this).commit();
        Toast.makeText(getActivity(), "Login Success", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
        Toast.makeText(getActivity(), "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
    }
}


