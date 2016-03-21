package bubok.wordgame;

import java.io.Serializable;

/**
 * Created by bubok on 21.03.2016.
 */
public class User implements Serializable {
    private String userID;
    private String name;
    private String avatar;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


    public User(String userID, String name, String avatar) {
        this.userID = userID;
        this.name = name;
        this.avatar = avatar;
    }
}
