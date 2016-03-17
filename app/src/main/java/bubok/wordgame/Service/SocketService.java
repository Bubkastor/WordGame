package bubok.wordgame.Service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by bubok on 17.03.2016.
 */
public class SocketService extends Service {

    private static final String TAG = "SOCKET_SERVICE";

    private static Socket socket;
    private static Socket chatSocket;
    private static Manager manager;
    private static boolean firstInitMainSocket;
    private String hostIP;

    private SocketIOListener mListener;
    private SocketIOBinder mBinder = new SocketIOBinder();

    public SocketService(){
        Log.i(TAG, "SocketService");
    }
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();

        initializeDiscoveryListener();

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return mBinder;
    }
    public void initializeDiscoveryListener() {
        hostIP = "http://server20160304034355.azurewebsites.net";
        setupSocketIO();
    }

    private void setupSocketIO() {
        try {
            Log.i("socket", hostIP);
            socket = IO.socket(hostIP);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("INFO", "connected");
                    //StartListener
                    mListener.onConnected();
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                }
            });
            socket.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject jsonObject = (JSONObject) args[0];
                    if (mListener != null) {
                        mListener.onMessage(jsonObject);
                    }
                }
            });
            socket.on("not found", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject jsonObject = (JSONObject) args[0];
                    if (mListener != null) {
                        mListener.onNotFound(jsonObject);
                    }
                }
            });
            socket.on("info", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject jsonObject = (JSONObject) args[0];
                    if (mListener != null) {
                        mListener.onInfo(jsonObject);
                    }
                }
            });


            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public void send(String event, String message){
        socket.emit(event, message);
    }
    public void send(String event, JSONObject message){
        socket.emit(event, message);
    }
    public void send(String event){ socket.emit(event);}

    public class SocketIOBinder extends Binder {
        public SocketService getService(){
            return SocketService.this;
        }

        public void setListener(SocketIOListener listener){
            mListener = listener;
        }
    }

    public interface SocketIOListener{
        public void onConnected();
        public void onMessage(JSONObject jsonObject);
        public void onNotFound(JSONObject jsonObject);
        public void onInfo(JSONObject jsonObject);

    }
}
