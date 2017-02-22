package Backend.Database;

public class LeaderBoardRecord {

    private String username;
    private int rating;

    public LeaderBoardRecord(String username, int rating) {
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
