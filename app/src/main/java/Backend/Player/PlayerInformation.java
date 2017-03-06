package Backend.Player;

public class PlayerInformation {
    private final static int STRIPPED_LENGTH = 10;

    private String username;
    private String userID;

    public PlayerInformation(String username, String userID){
        this.username = username;
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public String getUserID() {
        return userID;
    }

    String getStrippedUsername() {
        return (username.length() > STRIPPED_LENGTH
                ? username.substring(0, STRIPPED_LENGTH) + "..."
                : username);
    }

}
