package bubok.wordgame.service2;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;

import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by bubok on 17.03.2016.
 */

public class SocketService extends Service {

    private static final String TAG = "SOCKET_SERVICE";

    private static Socket mainSocket;
    private static Socket chatSocket;

    private SocketChatListener chatListener;
    private SocketMainListener mainListener;
    private SocketStartGameListener gameListener;

    private final SocketIOBinder mBinder = new SocketIOBinder();

    public SocketService(){
        Log.i(TAG, "SocketService");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        String url = intent.getStringExtra("url");
        String chatNamespace = intent.getStringExtra("chatNamespace");
        try{
            Manager manager = new Manager(new URI(url));
            mainSocket = manager.socket("/");
            chatSocket = manager.socket(chatNamespace);
        } catch (Exception ex){
            Log.i(TAG, ex.getMessage());
        }

        setupSocketMain();
        setupStartGame();
        setupSocketChat();

        mainSocket.connect();
        chatSocket.connect();
        return Service.START_NOT_STICKY;
    }


    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return mBinder;
    }

    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    private void setupSocketMain() {
        mainSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (mainListener != null) {
                    mainListener.onConnected();
                }
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (mainListener != null) {
                    mainListener.onDisconnect();
                }
            }
        });
        mainSocket.on("not found", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (mainListener != null) {
                    mainListener.onNotFound();
                }
            }
        });
        mainSocket.on("info", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (mainListener != null) {
                    mainListener.onInfo(jsonObject);
                }
            }
        });
        mainSocket.on("invite", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "invite");
                JSONObject jsonObject = (JSONObject) args[0];
                if (mainListener != null) {
                    Log.i(TAG, "invite not null");
                    mainListener.onInviteChat(jsonObject);
                }
            }
        });
        mainSocket.on("open chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (mainListener != null) {
                    mainListener.onOpenChat(jsonObject);
                }
            }
        });
        mainSocket.on("canceled game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (mainListener != null) {
                    mainListener.onCancelGame();
                }
            }
        });

        mainSocket.on("game is started", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (mainListener != null) {
                    mainListener.onGameIsStarted();
                }
            }
        });
    }

    private void setupSocketChat(){
        chatSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                if (chatListener != null) {
                chatListener.onConnected();
                }
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                if (chatListener != null) {
                    chatListener.onDisconnect();
                }
            }
        });
        chatSocket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (chatListener != null) {
                    chatListener.onMessage(jsonObject);
                }
            }
        });
        chatSocket.on("joined", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (chatListener != null) {
                    chatListener.onJoined(jsonObject);
                }
            }
        });
        chatSocket.on("user left", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (chatListener != null) {
                    chatListener.onUserLeft(jsonObject);
                }
            }
        });
        chatSocket.on("close game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (chatListener != null) {
                    chatListener.onCloseGame(jsonObject);
                }
            }
        });
        chatSocket.on("get info game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (chatListener != null) {
                    chatListener.onInfoGame(jsonObject);
                }
            }
        });
        chatSocket.on("change status message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (chatListener != null) {
                    chatListener.onChangeStatusMessage(jsonObject);
                }
            }
        });

    }

    private void setupStartGame() {
        mainSocket.on("initialize game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (gameListener != null)
                    gameListener.onInitializeGame(jsonObject);
            }
        });
        mainSocket.on("invite accepted", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (gameListener != null)
                    gameListener.onInvAccept(jsonObject);
            }
        });
        mainSocket.on("invite canceled", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (gameListener != null)
                    gameListener.onInvCancel(jsonObject);
            }
        });
        mainSocket.on("open chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                if (gameListener != null)
                    gameListener.onOpenChat(jsonObject);
            }
        });
        mainSocket.on("user online", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray jsonArray = (JSONArray) args[0];
                if (gameListener != null)
                    gameListener.onUserOnline(jsonArray);
            }
        });
    }

    public void send(String event, String message) {
        mainSocket.emit(event, message);
    }

    public void send(String event, JSONObject message) {
        mainSocket.emit(event, message);
    }

    public void send(String event) {
        mainSocket.emit(event);
    }


    public void chatSend(String event, String message) {
        chatSocket.emit(event, message);
    }

    public void chatSend(String event, JSONObject message) {
        chatSocket.emit(event, message);
    }

    public void chatSend(String event) {
        chatSocket.emit(event);
    }

    public void deleteMainListener() {
        mainListener = null;
    }

    public void deleteGameListener() {
        gameListener = null;
    }

    public class SocketIOBinder extends Binder {

        public SocketService getService(){
            return SocketService.this;
        }

        public void setMainListener(SocketMainListener listener) {
            mainListener = listener;
        }


        public void setChatListener(SocketChatListener listener) { chatListener = listener; }

        public void setGameListener(SocketStartGameListener listener) {
            gameListener = listener;
        }

    }

    public interface SocketMainListener{
        void onConnected();

        void onNotFound();

        void onInfo(JSONObject jsonObject);

        void onOpenChat(JSONObject jsonObject);

        void onInviteChat(JSONObject jsonObject);

        void onCancelGame();

        void onGameIsStarted();

        void onDisconnect();
    }

    public interface SocketStartGameListener {
        void onInitializeGame(JSONObject jsonObject);

        void onInvAccept(JSONObject jsonObject);

        void onInvCancel(JSONObject jsonObject);

        void onOpenChat(JSONObject jsonObject);

        void onUserOnline(JSONArray jsonArray);
    }

    public interface SocketChatListener{
        void onConnected();

        void onMessage(JSONObject jsonObject);

        void onJoined(JSONObject jsonObject);

        void onUserLeft(JSONObject jsonObject);

        void onCloseGame(JSONObject jsonObject);

        void onInfoGame(JSONObject jsonObject);

        void onChangeStatusMessage(JSONObject jsonObject);

        void onDisconnect();
    }
}
