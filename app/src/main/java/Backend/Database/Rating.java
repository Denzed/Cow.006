package Backend.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rating {

    private static int getKFactor(int rating, int played) {
        final int TOP_PLAYER_RATING_LIMIT = 2400;
        final int NEWBIE_GAMES_PLAYED = 30;
        final int NEWBIE_K_FACTOR = 40;
        final int DEFAULT_K_FACTOR = 20;
        final int TOP_PLAYER_K_FACTOR = 10;

        if (played <= NEWBIE_GAMES_PLAYED){
            return NEWBIE_K_FACTOR;
        } else if (rating < TOP_PLAYER_RATING_LIMIT){
            return DEFAULT_K_FACTOR;
        }
        return TOP_PLAYER_K_FACTOR;
    }

    public static void updateRatings(List<DatabaseRecord> databaseRecords, List<Integer> scores,
            List<Integer> ratings, List<Integer> ratingChanges) {
        List<Double> ratingPreChanges = calcRatingChanges(databaseRecords, scores);
        int opponentsNumber = databaseRecords.size() - 1;
        for (int i = 0; i < databaseRecords.size(); i++) {
            DatabaseRecord currentPlayer = databaseRecords.get(i);
            int ratingChange = ratingPreChanges.get(i).intValue();
            currentPlayer.setRatingChange(ratingChange);
            currentPlayer.updateRating(opponentsNumber);
            currentPlayer.updatePlayed();
            ratings.add(currentPlayer.getRating());
            ratingChanges.add(currentPlayer.getRatingChange());
        }

    }

    private static List<Double> calcRatingChanges(List<DatabaseRecord> databaseRecords, List<Integer> scores) {
        List<Double> ratingChanges = new ArrayList<>(Collections.nCopies(databaseRecords.size(), 0.));
        for (int i = 0; i < databaseRecords.size(); i++) {
            for (int j = 0; j < databaseRecords.size(); j++) {
                if (i == j) {
                    continue;
                }
                DatabaseRecord playerA = databaseRecords.get(i);
                DatabaseRecord playerB = databaseRecords.get(j);
                int resA = scores.get(i);
                int resB = scores.get(j);
                int ratingA = playerA.getRating();
                int ratingB = playerB.getRating();
                int kFactor = getKFactor(ratingA, playerA.getPlayed());
                
                double actualResult = 0;
                if (resA < resB) {
                    actualResult = 1;
                } else if (resA == resB) {
                    actualResult = 0.5;
                }
                ratingChanges.set(i, ratingChanges.get(i) + (kFactor * (actualResult - getExpectedResult(ratingA, ratingB))));
            }
        }
        return ratingChanges;
    }
    
    private static double getExpectedResult(int ratingA, int ratingB) {
        return 1 / (1 + Math.pow(10, (ratingB - ratingA) / 400.));
    }

}
