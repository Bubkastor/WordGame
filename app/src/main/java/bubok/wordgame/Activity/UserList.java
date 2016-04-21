package bubok.wordgame.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import bubok.wordgame.adapter.UserAdapter;
import bubok.wordgame.R;
import bubok.wordgame.other.User;

public class UserList extends AppCompatActivity {
    private UserAdapter userAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ArrayList<User> userArrayList =
                (ArrayList<User>) getIntent().getExtras().getSerializable("inviteList");

        ListView listView = (ListView) findViewById(R.id.listViewUser);
        userAdapter = new UserAdapter(this, userArrayList);
        listView.setAdapter(userAdapter);
        findViewById(R.id.inviteFriends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openStartGame();
            }
        });
    }

    private void openStartGame() {
        Intent intent = new Intent(UserList.this, StartGame.class);
        intent.putExtra(StartGame.EXTRA_MESSAGE_USERS_INVITE, userAdapter.getCheckedUser());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
