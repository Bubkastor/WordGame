package bubok.wordgame.adapter;

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

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bubok.wordgame.activity.Chat;

import bubok.wordgame.other.Message;
import bubok.wordgame.R;

/**
 * Адаптер для сообщений в чате
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private final List<Message> messages;
    private boolean optionPanel;
    private String leaderId;

    /**
     * Создание холдера для хранения ссылок на все элементы
     * @param parent родитель
     * @param viewType тип view
     * @return ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_chat_layout, parent, false);


        return new ViewHolder(v,
                (ImageButton) v.findViewById(R.id.buttonCorrect),
                (ImageView) v.findViewById(R.id.avatar),
                (TextView) v.findViewById(R.id.textViewName),
                (TextView) v.findViewById(R.id.textViewMessage),
                (RelativeLayout) v.findViewById(R.id.backgroundLayout));
    }

    /**
     * Привязка данных к ViewHolder
     * @param holder холдер
     * @param pos позиция
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, final int pos) {
        //TODO проверить работает ли эта замена
        final int position = holder.getAdapterPosition();
        if (isOptionPanel()) {
            if (!getLeaderId().equals(messages.get(position).getIdUser())) {
                holder.buttonCorrect.setVisibility(RelativeLayout.VISIBLE);
            } else {
                holder.buttonCorrect.setVisibility(RelativeLayout.GONE);
            }
        }

        Picasso.with(holder.itemView.getContext())
                .load(messages.get(position).getAvatar())
                .resize(50,50)
                .into(holder.avatar);

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

    public Message getItem(int position) {
        return messages.get(position);
    }

    /**
     * Создание своего холдера на основе RecyclerView.ViewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView,
                          ImageButton buttonCorrect,
                          ImageView avatar,
                          TextView name,
                          TextView message,
                          RelativeLayout background) {
            super(itemView);
            this.buttonCorrect = buttonCorrect;
            this.avatar = avatar;
            this.name = name;
            this.message = message;
            this.background = background;
        }

        public final ImageButton buttonCorrect;
        public final ImageView avatar;
        public final TextView name;
        public final TextView message;
        public final RelativeLayout background;
    }

    /**
     * Добавление сообщение
     * @param message сообщение
     */
    public void add(Message message) {
        messages.add(message);
    }

    /**
     * Получение ид лидера
     * @return leaderId
     */
    private String getLeaderId() {
        return leaderId;
    }

    /**
     * Задаем значение лидера
     * @param leaderId id лидера
     */
    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    /**
     * Проверка панели доп. действий
     * @return нужна ли панель
     */
    private boolean isOptionPanel() {
        return optionPanel;
    }

    /**
     * Задаем панель
     * @param optionPanel bool
     */
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

    /**
     * Изменяем статус ообщения
     * @param id id сообщения
     * @param status статус сообщения
     */
    public  void ChangeStatus(String id, String status){
        for (Message message: messages)
            if (message.getIDMessage().equals(id)){
                message.setStatus(status);
                Log.i("STATUS", "Change Status:" + status);
            }

    }
}
