package Backend.Database;

import Backend.Database.DatabaseRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rating {
    
    private static final int TOP_PLAYER_RATING_LIMIT = 2400;
    private static final int NEWBIE_GAMES_PLAYED = 30;
    private static final int NEWBIE_K_FACTOR = 40;
    private static final int DEFAULT_K_FACTOR = 20;
    private static final int TOP_PLAYER_K_FACTOR = 10;

    private static int getKFactor(int rating, int played) {
        if (played <= NEWBIE_GAMES_PLAYED){
            return NEWBIE_K_FACTOR;
        } else if (rating < TOP_PLAYER_RATING_LIMIT){
            return DEFAULT_K_FACTOR;
        }
        return TOP_PLAYER_K_FACTOR;
    }

    public static void updateRatings(List<DatabaseRecord> playersInformation) {
        List<Double> ratingChanges = calcRatingChanges(playersInformation);
        int opponentsNumber = playersInformation.size() - 1;
        for (int i = 0; i < playersInformation.size(); i++) {
            DatabaseRecord currentPlayer = playersInformation.get(i);
            currentPlayer.setRatingChange(ratingChanges.get(i).intValue());
            currentPlayer.updateRating(opponentsNumber);
            currentPlayer.updatePlayed();
        }
    }

    //this is elo-like rating system; game between n players <-> game between each pair of players
    private static List<Double> calcRatingChanges(List<DatabaseRecord> playersInformation) {
        List<Double> ratingChanges = new ArrayList<>(Collections.nCopies(playersInformation.size(), 0.));
        for (int i = 0; i < playersInformation.size(); i++) {
            for (int j = 0; j < playersInformation.size(); j++) {
                if (i == j) {
                    continue;
                }
                DatabaseRecord playerA = playersInformation.get(i);
                DatabaseRecord playerB = playersInformation.get(j);
                int resA = playerA.getPoints();
                int resB = playerB.getPoints();
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
