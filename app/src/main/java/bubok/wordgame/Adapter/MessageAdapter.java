package bubok.wordgame.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bubok.wordgame.Activity.Chat;
import bubok.wordgame.AsyncTasks.DownloadImageTask;
import bubok.wordgame.Class.Message;
import bubok.wordgame.R;

/**
 * Created by bubok on 01.03.2016.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private final List<Message> messages;
    private boolean optionPanel;
    private String leaderId;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_chat_layout, parent, false);

        return new ViewHolder(v,
                (ImageButton) v.findViewById(R.id.buttonCorrect),
                (ImageView) v.findViewById(R.id.avatar),
                (TextView) v.findViewById(R.id.textViewName),
                (TextView) v.findViewById(R.id.textViewMessage),
                (RelativeLayout) v.findViewById(R.id.optionLayout),
                (RelativeLayout) v.findViewById(R.id.backgroundLayout));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (isOptionPanel()) {
            if (!getLeaderId().equals(messages.get(position).getIdUser())) {
                holder.optionPanel.setVisibility(RelativeLayout.VISIBLE);
            } else {
                holder.optionPanel.setVisibility(RelativeLayout.GONE);
            }
        }

        new DownloadImageTask(holder.avatar)
                .execute(messages.get(position).getAvatar());


        holder.name.setText(messages.get(position).getUsername());
        holder.message.setText(messages.get(position).getMessage());

        switch (Integer.parseInt(messages.get(position).getStatus())) {
            case 1: //GREEN
                holder.background.setBackgroundColor(Color.GREEN);
                break;
            case -1: //RED
                //dislike
                holder.background.setBackgroundColor(Color.RED);
                break;
            default:
                //empty
                holder.background.setBackgroundColor(Color.WHITE);
                break;
        }
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
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public Message getItem(int possiton) {
        return messages.get(possiton);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView,
                          ImageButton buttonCorrect,
                          ImageView avatar,
                          TextView name,
                          TextView message,
                          RelativeLayout optionPanel,
                          RelativeLayout background) {
            super(itemView);
            this.buttonCorrect = buttonCorrect;
            this.avatar = avatar;
            this.name = name;
            this.message = message;
            this.optionPanel = optionPanel;
            this.background = background;
        }

        public final ImageButton buttonCorrect;
        public final ImageView avatar;
        public final TextView name;
        public final TextView message;
        public final RelativeLayout optionPanel;
        public final RelativeLayout background;
    }

    public void add(Message message) {
        messages.add(message);
    }

    private String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    private boolean isOptionPanel() {
        return optionPanel;
    }

    public void setOptionPanel(boolean optionPanel) {
        this.optionPanel = optionPanel;
    }

    public MessageAdapter(ArrayList<Message> messages) {
        this.messages = messages;
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
}
