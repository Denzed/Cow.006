package Backend.Database;

public class DatabaseRecord {

    private String userID;
    private String username;
    private int rating;
    private int played;
    private int ratingChange;

    DatabaseRecord(String userID, String username, int rating, int played){
        this.userID = userID;
        this.username = username;
        this.rating = rating;
        this.played = played;
    }


    int getPlayed() {
        return played;
    }

    void updatePlayed(){
        played++;
    }

    int getRatingChange(){
        return ratingChange;
    }

    int getRating() {
        return rating;
    }

    void setRatingChange(int ratingChange) {
        this.ratingChange = ratingChange;
    }

    void updateRating(int opponentsNumber) {
        rating += ratingChange / opponentsNumber;
    }
}
