package Backend.Player;

public class PlayerInformation {

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

}
