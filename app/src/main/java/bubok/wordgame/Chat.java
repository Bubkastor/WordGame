package bubok.wordgame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.VideoView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.util.ArrayList;

import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Chat extends AppCompatActivity {
    public final static String EXTRA_MESSAGE_WINGAME = "bubok.wordgame.WINGAME";
    private ListView listViewCheat;
    private Button buttonSend;
    private EditText editTextMessage;
    private MessageAdapter messageAdapter;
    private ImageView imageView;
    private VideoView videoView;
    private String URL;
    private String namespaceSocket;
    private String mGame;
    private String token = "";
    public static Socket mSocket;
    private Bitmap bMap;
    private File videoFile;
    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        URL = getResources().getString(R.string.URLOnline);
        namespaceSocket = getResources().getString(R.string.URLNamespace);

        Log.i("STRING", "URL + namespaceSocket: " + URL + namespaceSocket);
        {
            try {
                Manager manager = new Manager(new URI(URL));
                mSocket = manager.socket(namespaceSocket);
            } catch (URISyntaxException e) {
                Log.i("SOCKET", "ERROR: " + e.getMessage());
            }
        }
        Intent intent = getIntent();
        token = intent.getStringExtra(main.EXTRA_MESSAGE_USED_TOKEN);
        mGame = intent.getStringExtra(main.EXTRA_MESSAGE_USED_GAME);

        listViewCheat = (ListView) findViewById(R.id.listViewCheat);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        imageView = (ImageView) findViewById(R.id.imageViewChat);
        videoView = (VideoView) findViewById(R.id.videoViewChat);
        mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject answer = new JSONObject();
                try{
                    answer.put("token", token);
                    answer.put("game", mGame);
                }catch (Exception ex){}
                mSocket.emit("add user", answer);
            }
        }).on("joined", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONObject answer = (JSONObject) args[0];
                try {
                    String username = answer.getString("username");
                    Log.i("CHAT", "USER JOINED: " +username);
                } catch (Exception ex) {
                    Log.i("CHAT", "ERROR: " + ex.getMessage());
                }
            }
        }).on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHAT", "MESSAGE");
                JSONObject answer = (JSONObject) args[0];
                try {
                    AddMessageInCheat(
                            answer.getString("avatar"),
                            answer.getString("username"),
                            answer.getString("message"),
                            answer.getString("idMessage"),
                            answer.getString("status"));
                } catch (Exception ex) {
                    Log.i("CHAT", "ERROR: " + ex.getMessage());
                }
            }
        }).on("user left", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHAT", "USER LEFT");
                JSONObject answer = (JSONObject) args[0];
                try {
                    //AddMessageInCheat(answer.getString("avatar"), answer.getString("username"), "Left");
                } catch (Exception ex) {
                    Log.i("CHAT", "ERROR: " + ex.getMessage());
                }
            }
        }).on("close game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHAT", "close chat");
                JSONObject answer = (JSONObject) args[0];
                try {
                    String userWin = answer.getString("win");
                    String word = answer.getString("word");
                    String text = "Пользователь " + userWin + " победил загаданое слово " + word;
                    Log.i("CHAT", text);

                    CloseCheat(userWin, word);
                } catch (Exception ex) {
                    Log.i("CHAT", ex.getMessage());

                }
            }
        }).on("get info game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHAT", "info");
                JSONObject answer = (JSONObject) args[0];
                try {
                    String typeMedia = answer.getString("typeMedia");
                    String contentType = answer.getString("contentType");
                    byte[] decodedBytes = (byte[]) answer.get("data");
                    Boolean isAdmin = answer.getBoolean("admin");
                    messageAdapter.setOptionPanel(isAdmin);
                    SetMediaContainer(typeMedia, contentType, decodedBytes);
                } catch (Exception ex) {
                    Log.i("CHAT INFO ERR", ex.getMessage());
                }
            }
        }).on("change status message",new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("CHAT", "change status message");
                JSONObject answer = (JSONObject) args[0];
                try {
                    String id = answer.getString("idMessage");
                    String status = answer.getString("status");
                    ChangeStatus(id, status);

                } catch (Exception ex) {
                    Log.i("CHAT", ex.getMessage());
                }
            }
        });
        mSocket.connect();

        mSocket.emit("get info game", mGame);

        imageView.setImageResource(R.drawable.cat);

        ArrayList<Message> arrayList = new ArrayList<Message>();
        messageAdapter = new MessageAdapter(this, arrayList);
        listViewCheat.setAdapter(messageAdapter);
    }

    private synchronized void ChangeStatus(String id, String status){
        messageAdapter.ChangeStatus(id, status);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    private void CloseCheat(String userWin, String word) {
        Intent intent = new Intent(Chat.this, WinGame.class);
        String text = "Пользователь " + userWin + " победил загаданое слово " + word;
        intent.putExtra(EXTRA_MESSAGE_WINGAME, text);
        startActivity(intent);
    }

    private synchronized void AddMessageInCheat(String urlAvatar, String login, String message, String idMessage, String status){
        Message message1 = new Message(urlAvatar, login, message, idMessage, status);
        messageAdapter.add(message1);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    public void buttonSendClick(View v) {
        Log.i("CHEAT", "send");
        String message = editTextMessage.getText().toString();
        editTextMessage.setText("");
        JSONObject sendMessage = new JSONObject();
        try{
            sendMessage.put("message", message);
            sendMessage.put("game", mGame);
            mSocket.emit("message", sendMessage);

        } catch (Exception ex){
            Log.i("CHEAT", "ERROR: " + ex.getMessage());
        }
    }

    private synchronized void SetMediaContainer(String typeMedia, String contentType, byte[] decodedBytes ){

        switch (typeMedia){
            case "image":
                SetImageView(decodedBytes);
                break;
            case "audio":
                break;
            case "video":
                try{
                    SetVideoView(decodedBytes, contentType);
                } catch (Exception ex){
                    Log.i("VIDEO", ex.getMessage());
                }

                break;
            default:
                break;
        }
    }

    private  void SetImageView(byte[] decodedBytes){
        bMap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        Handler handler = new Handler(getBaseContext().getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(ImageView.VISIBLE);
                imageView.setImageBitmap(bMap);
            }
        });
    }

    private void SetVideoView(byte[] decodedBytes, String contentType) throws IOException{

        videoFile = new File(this.getFilesDir() + File.separator + "test." + contentType);
        OutputStream myOutputStream = new FileOutputStream(videoFile);
        myOutputStream.write(decodedBytes);
        myOutputStream.flush();
        myOutputStream.close();



        Handler handler = new Handler(getBaseContext().getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                videoView.setVisibility(ImageView.VISIBLE);
                Log.i("VIDEO", videoFile.getAbsolutePath());
                videoView.setVideoPath(videoFile.getAbsolutePath());

            }
        });

    }

    public File getTempFile(Context context, String url) {
        File file = null;
        try {
            String fileName = Uri.parse(url).getLastPathSegment();
            file = File.createTempFile(fileName, null, context.getCacheDir());
        }
        catch (IOException e) {
            Log.i("GET FILE", e.getMessage());
        }
        return file;
    }

    public void onPause(){
        //mSocket.disconnect();
        super.onPause();
    }
}