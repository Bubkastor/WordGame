package bubok.wordgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class UserList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ArrayList<User> userArrayList = (ArrayList<User>) getIntent().getExtras().getSerializable("inviteList");
    }
}
