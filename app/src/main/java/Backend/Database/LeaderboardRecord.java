package Backend.Database;

public class LeaderboardRecord {
    final static int STRIPPED_LENGTH = 10;


    private String username;
    private int rating;

    public LeaderboardRecord(String username, int rating) {
        this.username = username;
        this.rating = rating;
    }

    public String getUsername() {
        return username;
    }

    public String getStrippedUsername() {
        return (username.length() > STRIPPED_LENGTH
                ? username.substring(0, STRIPPED_LENGTH) + "..."
                : username);
    }

    public int getRating() {
        return rating;
    }
}
