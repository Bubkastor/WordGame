package bubok.wordgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by bubok on 01.03.2016.
 */
public class MessageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Message> messages;

    public boolean isOptionPanel() {
        return optionPanel;
    }

    public void setOptionPanel(boolean optionPanel) {
        this.optionPanel = optionPanel;
    }

    private boolean optionPanel;

    MessageAdapter(Context context, ArrayList<Message> messages){
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }
    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    public  void ChangeStatus(String id, String status){
        for (Message message: messages)
            if (message.getIDMessage().equals(id)){
                message.setStatus(status);
                Log.i("STATUS", "Change Status:" + status);
            }

    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View row = convertView;
        if( convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.chat_row_layout, parent, false);
        }
        ImageButton likeButton = (ImageButton) row.findViewById(R.id.likeViewButton);
        likeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject sendData = new JSONObject();
                try {
                    sendData.put("id", messages.get(position).getIDMessage());
                    sendData.put("status", "1");
                } catch (Exception ex) {
                }
                Chat.mSocket.emit("change status message", sendData);
            }
        });
        ImageButton dislikeButton = (ImageButton) row.findViewById(R.id.dislikeViewButton);
        dislikeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject sendData = new JSONObject();
                try {
                    sendData.put("id", messages.get(position).getIDMessage());
                    sendData.put("status", "2");
                } catch (Exception ex) {
                }
                Chat.mSocket.emit("change status message", sendData);
            }
        });

        ImageButton buttonCorrect = (ImageButton) row.findViewById(R.id.buttonCorrect);
        buttonCorrect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject sendData = new JSONObject();
                try {
                    sendData.put("id", messages.get(position).getIDMessage());
                } catch (Exception ex) {
                }
                Chat.mSocket.emit("message correct", sendData);
            }
        });



        ImageView avatar = (ImageView) row.findViewById(R.id.avatar);
        TextView message = (TextView) row.findViewById(R.id.textViewMessage);



        if(this.optionPanel){
            RelativeLayout optionPanel = (RelativeLayout) row.findViewById(R.id.optionLayout);
            optionPanel.setVisibility(RelativeLayout.VISIBLE);
        }
        if (messages.get(position).getAvatarBitmap() == null){
            new DownloadImageTask(avatar, messages.get(position))
                    .execute(messages.get(position).getAvatar());
        }else{
                avatar.setImageBitmap(messages.get(position).getAvatarBitmap());
        }


        message.setText(messages.get(position).getUsername() + "\n" + messages.get(position).getMessage());
        ImageView likeView = (ImageView) row.findViewById(R.id.likeView);
        ImageView dislikeView = (ImageView) row.findViewById(R.id.dislikeView);
        switch (messages.get(position).getStatus()){
            case "1":
                likeView.setVisibility(ImageView.VISIBLE);
                dislikeView.setVisibility(ImageView.GONE);
                break;
            case "2":
                dislikeView.setVisibility(ImageView.VISIBLE);
                likeView.setVisibility(ImageView.GONE);
                break;
            default:
                likeView.setVisibility(ImageView.GONE);
                dislikeView.setVisibility(ImageView.GONE);
                break;
        };
        return row;
    }

    public void add(Message message){
        messages.add(message);
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView viewImage;
        Message message;
        public DownloadImageTask(ImageView viewImage, Message message) {
            this.message = message;
            this.viewImage = viewImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String key = urls[0];
            Bitmap mIcon11 = null;
            if(Login.mMemoryCache.get(key) == null){
                try {
                    URL newUrl = new URL(urls[0]);
                    mIcon11 = BitmapFactory.decodeStream(newUrl.openConnection().getInputStream());
                    Login.mMemoryCache.put(key, mIcon11);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            } else {
                mIcon11 = Login.mMemoryCache.get(key);
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            message.setAvatarBitmap(result);
            viewImage.setImageBitmap(result);
        }
    }



}
