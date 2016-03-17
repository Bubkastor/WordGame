package bubok.wordgame.Fragment;

import android.database.Observable;
import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by bubok on 16.03.2016.
 */
public class SocketModel {
    private static final String TAG = "SocketModel";

    private static Socket socket;

    private final SocketObservable socketObservable = new SocketObservable();
    private boolean isConnected;

    public SocketModel() {
        Log.i(TAG, "new Instance");
    }

    public SocketModel(String url) {
        Log.i(TAG, "new Instance");
        {
            try {
                socket = IO.socket(url);
                socket.connect();
                isConnected = socket.connected();
            } catch (URISyntaxException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void registerObserver(final Observer observer){
        socketObservable.registerObserver(observer);
        if(isConnected)
            observer.onConnected(this);
    }

    public void unregisterObserver(final Observer observer){
        socketObservable.unregisterObserver(observer);
    }

    public Socket getSocket(){
        return socket;
    }


    public interface Observer{
        void onConnected(SocketModel socketModel);
        void onSucceeded(SocketModel socketModel);
        void onFailed(SocketModel socketModel);
    }

    private class  SocketObservable extends Observable<Observer>{
        public void notifyStarted() {
            for (final Observer observer : mObservers) {
                observer.onConnected(SocketModel.this);
            }
        }

        public void notifySucceeded() {
            for (final Observer observer : mObservers) {
                observer.onSucceeded(SocketModel.this);
            }
        }

        public void notifyFailed() {
            for (final Observer observer : mObservers) {
                observer.onFailed(SocketModel.this);
            }
        }
    }
}

