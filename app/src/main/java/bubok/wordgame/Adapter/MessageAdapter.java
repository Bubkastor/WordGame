package bubok.wordgame.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bubok.wordgame.AsyncTasks.DownloadImageTask;
import bubok.wordgame.Activity.Chat;
import bubok.wordgame.Class.*;
import bubok.wordgame.R;

/**
 * Created by bubok on 01.03.2016.
 */
public class MessageAdapter extends BaseAdapter {

    private Context context;
    private List<Message> messages;
    private boolean optionPanel;
    private String leaderId;

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public boolean isOptionPanel() {
        return optionPanel;
    }

    public void setOptionPanel(boolean optionPanel) {
        this.optionPanel = optionPanel;
    }

    public MessageAdapter(Context context, ArrayList<Message> messages) {
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
        return position;
    }

    public  void ChangeStatus(String id, String status){
        for (Message message: messages)
            if (message.getIDMessage().equals(id)){
                message.setStatus(status);
                Log.i("STATUS", "Change Status:" + status);
            }

    }

    static class ViewHolder {
        public ImageButton likeButton;
        public ImageButton dislikeButton;
        public ImageButton buttonCorrect;
        public ImageView avatar;
        public TextView message;
        public RelativeLayout optionPanel;
        public ImageView likeView;
        public ImageView dislikeView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_row_layout, parent, false);

            holder = new ViewHolder();
            holder.likeButton = (ImageButton) convertView.findViewById(R.id.likeViewButton);
            holder.dislikeButton = (ImageButton) convertView.findViewById(R.id.dislikeViewButton);
            holder.buttonCorrect = (ImageButton) convertView.findViewById(R.id.buttonCorrect);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.message = (TextView) convertView.findViewById(R.id.textViewMessage);
            holder.optionPanel = (RelativeLayout) convertView.findViewById(R.id.optionLayout);
            holder.likeView = (ImageView) convertView.findViewById(R.id.likeView);
            holder.dislikeView = (ImageView) convertView.findViewById(R.id.dislikeView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject sendData = new JSONObject();
                try {
                    sendData.put("id", messages.get(position).getIDMessage());
                    sendData.put("status", "1");
                } catch (Exception ex) {
                    Log.i("JSON", ex.getMessage());
                }
                Chat.mService.chatSend("change status message", sendData);
            }
        });

        holder.dislikeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject sendData = new JSONObject();
                try {
                    sendData.put("id", messages.get(position).getIDMessage());
                    sendData.put("status", "2");
                } catch (Exception ex) {
                    Log.i("JSON", ex.getMessage());
                }
                Chat.mService.chatSend("change status message", sendData);
            }
        });

        holder.buttonCorrect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject sendData = new JSONObject();
                try {
                    sendData.put("id", messages.get(position).getIDMessage());
                } catch (Exception ex) {
                    Log.i("JSON", ex.getMessage());
                }
                Chat.mService.chatSend("message correct", sendData);
            }
        });


        if (this.optionPanel) {
            if (!this.leaderId.equals(messages.get(position).getIdUser())) {
                holder.optionPanel.setVisibility(RelativeLayout.VISIBLE);
            } else {
                holder.optionPanel.setVisibility(RelativeLayout.GONE);
            }
        }

        new DownloadImageTask(holder.avatar)
                .execute(messages.get(position).getAvatar());

        holder.message.setText(messages.get(position).getUsername() + "\n" + messages.get(position).getMessage());

        switch (messages.get(position).getStatus()){
            case "1":
                holder.likeView.setVisibility(ImageView.VISIBLE);
                holder.dislikeView.setVisibility(ImageView.GONE);
                break;
            case "2":
                holder.dislikeView.setVisibility(ImageView.VISIBLE);
                holder.likeView.setVisibility(ImageView.GONE);
                break;
            default:
                holder.likeView.setVisibility(ImageView.GONE);
                holder.dislikeView.setVisibility(ImageView.GONE);
                break;
        }
        return convertView;
    }

    public void add(Message message){
        messages.add(message);
    }

}
