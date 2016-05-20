package bubok.wordgame.other;

import android.graphics.Bitmap;

/**
 * Created by bubok on 01.03.2016.
 */

/**
 * Сообщение и данные о нем
 */
public class Message {
    public Message(String avatar, String username, String message, String idMessage, String status, String idUser) {
        Username = username;
        Message = message;
        Avatar = avatar;
        IDMessage = idMessage;
        Status= status;
        IdUser = idUser;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getAvatar() {
        return Avatar;
    }

    public void setAvatar(String avatar) {
        Avatar = avatar;
    }

    private String Avatar;
    private String Username;
    private String Message;
    private String IDMessage;
    private String Status;
    private String IdUser;

    public String getIdUser() {
        return IdUser;
    }

    public void setIdUser(String idUser) {
        IdUser = idUser;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getIDMessage() {
        return IDMessage;
    }

    public void setIDMessage(String IDMessage) {
        this.IDMessage = IDMessage;
    }

    public Bitmap getAvatarBitmap() {
        return AvatarBitmap;
    }

    public void setAvatarBitmap(Bitmap avatarBitmap) {
        AvatarBitmap = avatarBitmap;
    }

    private Bitmap AvatarBitmap;

}
