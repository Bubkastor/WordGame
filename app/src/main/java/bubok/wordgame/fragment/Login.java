package bubok.wordgame.fragment;

import android.content.Intent;
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

import com.github.gorbin.asne.core.listener.OnRequestSocialPersonCompleteListener;
import com.github.gorbin.asne.core.persons.SocialPerson;
import com.github.gorbin.asne.vk.VkSocialNetwork;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;
import com.github.gorbin.asne.twitter.TwitterSocialNetwork;
import com.vk.sdk.VKScope;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bubok.wordgame.R;
import bubok.wordgame.activity.Main;

public class Login extends Fragment implements SocialNetworkManager.OnInitializationCompleteListener,
        OnLoginCompleteListener {

    public static SocialNetworkManager mSocialNetworkManager;

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

        ArrayList<String> fbScope = new ArrayList<String>();
        fbScope.addAll(Arrays.asList("public_profile, email"));
        mSocialNetworkManager = (SocialNetworkManager) getFragmentManager().findFragmentByTag(Main.SOCIAL_NETWORK_TAG);
        if (mSocialNetworkManager == null){
            mSocialNetworkManager = new SocialNetworkManager();

            VkSocialNetwork vkNetwork = new VkSocialNetwork(this, VK_KEY, vkScope);
            mSocialNetworkManager.addSocialNetwork(vkNetwork);

            FacebookSocialNetwork fbNetwork = new FacebookSocialNetwork(this, fbScope);
            mSocialNetworkManager.addSocialNetwork(fbNetwork);

            getFragmentManager().beginTransaction().add(mSocialNetworkManager, Main.SOCIAL_NETWORK_TAG).commit();
            mSocialNetworkManager.setOnInitializationCompleteListener(this);
        } else {
            if(!mSocialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
                onSocialNetworkManagerInitialized();
            }
        }

        return rootView;

    }

    private void initSocialNetwork(SocialNetwork socialNetwork){

        if(socialNetwork.isConnected()){
            switch (socialNetwork.getID()){
                //TODO получить данные
                case VkSocialNetwork.ID:
                    vk.setText("Show VK profile");
                    break;
                case FacebookSocialNetwork.ID:
                    fb.setText("Show FB profile");
                    break;
                case TwitterSocialNetwork.ID:
                    tw.setText("Show TW profile");
                    break;
            }
            //startProfile(socialNetwork.getID());
        }
    }

    private View.OnClickListener exitClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vk.setText("Login VK");
            fb.setText("Login FB");
            tw.setText("Login TW");
            List<SocialNetwork> socialNetworks = mSocialNetworkManager.getInitializedSocialNetworks();
            for (SocialNetwork socialNetwork : socialNetworks) {
                socialNetwork.logout();
            }

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
                    //networkId = TwitterSocialNetwork.ID;
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

    private void hideFragment(){
        getFragmentManager().beginTransaction().hide(this).commit();
    }

    @Override
    public void onLoginSuccess(int socialNetworkID) {
        // TODO потверждение логинации
        //hideFragment();
        Toast.makeText(getActivity(), "Login Success", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
        // TODO Логинация не удалась
        Toast.makeText(getActivity(), "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    private void startProfile(int socialNetworkID){

        final SocialNetwork socialNetwork = mSocialNetworkManager.getSocialNetwork(socialNetworkID);
        socialNetwork.setOnRequestCurrentPersonCompleteListener(new OnRequestSocialPersonCompleteListener() {
            @Override
            public void onRequestSocialPersonSuccess(int socialNetworkId, SocialPerson socialPerson) {
                String avatar = socialPerson.avatarURL;
                String name = socialPerson.name;
                String id = socialPerson.id;
                String socialNetwork = "";
                switch (socialNetworkId){
                    case VkSocialNetwork.ID:
                        socialNetwork = "VK";
                        break;
                    case FacebookSocialNetwork.ID:
                        socialNetwork = "FB";
                        break;
                    case TwitterSocialNetwork.ID:
                        socialNetwork = "TW";
                        break;
                }
                Intent intent = new Intent();
                intent.putExtra(Main.EXTRA_MESSAGE_USED_SOCIAL, socialNetwork);
                intent.putExtra(Main.EXTRA_MESSAGE_USED_ID, id);
                intent.putExtra(Main.EXTRA_MESSAGE_USED_AVATAR, avatar);
                intent.putExtra(Main.EXTRA_MESSAGE_USED_NAME, name);
                startActivity(intent);
                //hideFragment();
            }

            @Override
            public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
                Toast.makeText(getActivity(), "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        socialNetwork.requestCurrentPerson();
    }
}


