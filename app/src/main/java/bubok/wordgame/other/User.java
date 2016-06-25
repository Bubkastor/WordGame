package bubok.wordgame.other;

import java.io.Serializable;

/**
 * Created by bubok on 21.03.2016.
 */

/**
 * Пользователь для Адаптера и логинации
 */
public class User implements Serializable {
    private String userID;
    private String name;
    private String avatar;
    private String socialNetwork;
    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

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

    public String getSocialNetwork() {
        return socialNetwork;
    }

    public void setSocialNetwork(String socialNetwork) {
        this.socialNetwork = socialNetwork;
    }

    public User(String userID, String name, String avatar, String socialNetwork){
        this.userID = userID;
        this.name = name;
        this.avatar = avatar;
        this.isSelected = false;
        this.socialNetwork = socialNetwork;
    }

    public User(String userID, String name, String avatar) {
        this.userID = userID;
        this.name = name;
        this.avatar = avatar;
        this.isSelected = false;
        this.socialNetwork = "";
    }


}
