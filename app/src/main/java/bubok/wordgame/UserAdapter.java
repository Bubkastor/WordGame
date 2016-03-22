package bubok.wordgame;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by bubok on 21.03.2016.
 */
public class UserAdapter extends BaseAdapter {

    private ArrayList<User> userArrayList;
    private Context context;

    UserAdapter(Context context, ArrayList<User> userArrayList) {
        this.context = context;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
