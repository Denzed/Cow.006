package Backend.Database;

public class DatabaseRecord {

    private String userID;
    private String username;
    private int rating;
    private int played;
    private int points;
    private int ratingChange;

    DatabaseRecord(String userID, String username, int rating, int played, int points){
        this.userID = userID;
        this.username = username;
        this.rating = rating;
        this.played = played;
        this.points = points;
    }


    public int getPlayed() {
        return played;
    }

    public void updatePlayed(){
        played++;
    }

    public int getRatingChange(){
        return ratingChange;
    }

    public int getRating() {
        return rating;
    }

    public int getPoints() {
        return points;
    }

    public String getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public void setRatingChange(int ratingChange) {
        this.ratingChange = ratingChange;
    }

    public void updateRating(int opponentsNumber) {
        rating += ratingChange / opponentsNumber;
    }
}
