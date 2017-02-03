package Backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

public class GameResult {
    private static final int TOP_PLAYER_RATING_LIMIT = 2400;
    private static final int NEWBIE_GAMES_PLAYED = 30;
    private static final int NEWBIE_K_FACTOR = 40;
    private static final int DEFAULT_K_FACTOR = 20;
    private static final int TOP_PLAYER_K_FACTOR = 10;
    private String userID;
    private String username;
    private int rating;
    private int points;
    private int delta;

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    private int gamesPlayed;

    public GameResult(String userID, String username, int points, int rating, int gamesPlayed){
        this.userID = userID;
        this.username = username;
        this.points = points;
        this.rating = rating;
        this.gamesPlayed = gamesPlayed;
    }

    public int getKFactor() {
        if (gamesPlayed <= NEWBIE_GAMES_PLAYED){
            return NEWBIE_K_FACTOR;
        } else if (rating < TOP_PLAYER_RATING_LIMIT){
            return DEFAULT_K_FACTOR;
        }
        return TOP_PLAYER_K_FACTOR;
    }

    public int getRating() {
        return rating;
    }

    public int getPoints() {
        return points;
    }

    public void updateRating(double delta) {
        this.delta = (int)delta;
        rating += delta;
    }

    public void updateGamesPlayed(){
        gamesPlayed++;
    }

    public static void recalc(ArrayList<GameResult> gameResults){
        ArrayList<Double> deltas = new ArrayList<>(Collections.nCopies(gameResults.size(), 0.));
        System.out.println("recalc");
        for (int i = 0; i < gameResults.size(); i++){
            for (int j = 0; j < gameResults.size(); j++){
                if (i == j){
                    continue;
                }

                int resA = gameResults.get(i).getPoints();
                int resB = gameResults.get(j).getPoints();
                double actualResult = 0;
                if (resA < resB){
                    actualResult = 1;
                } else if (resA == resB){
                    actualResult = 0.5;
                }

                int ratingA = gameResults.get(i).getRating();
                int ratingB = gameResults.get(j).getRating();
                int kFactor = gameResults.get(i).getKFactor();
                deltas.set(i, deltas.get(i) + (kFactor * (actualResult - getExpectedResult(ratingA, ratingB))));
            }
        }

        int opponentsNumber = gameResults.size() - 1;
        for (int i = 0; i < gameResults.size(); i++) {
            gameResults.get(i).updateRating(deltas.get(i) / opponentsNumber);
            gameResults.get(i).updateGamesPlayed();
        }
        System.out.println("recalced");
    }

    private static double getExpectedResult(int ratingA, int ratingB) {
        return 1 / (1 + Math.pow(10, (ratingB - ratingA) / 400.));
    }

    public String getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public int getDelta() {
        return delta;
    }
}
