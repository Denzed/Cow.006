package Backend.Database;

public class LeaderboardRecord {

    private String username;
    private int rating;

    public LeaderboardRecord(String username, int rating) {
        this.username = username;
        this.rating = rating;
    }

    public String getUsername() {
        return username;
    }

    public int getRating() {
        return rating;
    }
}
