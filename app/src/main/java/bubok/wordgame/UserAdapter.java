package bubok.wordgame;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by bubok on 21.03.2016.
 */
public class UserAdapter extends BaseAdapter {

    private static final String TAG = "USER_ADAPTER";
    private ArrayList<User> userArrayList;
    private Context context;

    UserAdapter(Context context, ArrayList<User> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
    }

    public ArrayList<String> getCheckedUser() {
        ArrayList<String> stringArrayList = new ArrayList<>();
        for (int i = 0; i < userArrayList.size(); i++) {
            User it = userArrayList.get(i);
            if (it.isSelected())
                stringArrayList.add(it.getUserID());
        }
        return stringArrayList;
    }

    @Override
    public int getCount() {
        return userArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return userArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.user_row_layout, parent, false);
        }

        ImageView avatar = (ImageView) row.findViewById(R.id.imageViewAvatar);
        TextView username = (TextView) row.findViewById(R.id.textViewUsername);
        final CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkBoxSelected);

        username.setText(userArrayList.get(position).getName());
        checkBox.setChecked(userArrayList.get(position).isSelected());
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userArrayList.get(position).setIsSelected(checkBox.isChecked());
            }
        });

        new AsyncTasks.DownloadImageTask(avatar).execute(userArrayList.get(position).getAvatar());

        return row;
    }
}
