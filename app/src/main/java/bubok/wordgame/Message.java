package bubok.wordgame;

/**
 * Created by bubok on 01.03.2016.
 */
public class Message {
    public Message(String username, String message) {
        Username = username;
        Message = message;
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

    String Username;
    String Message;

}
