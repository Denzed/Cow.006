package Backend;
import java.util.ArrayList;
import java.util.Collections;

abstract class AbstractPlayer {
    ArrayList<Integer> hand;
    private int score;
    public abstract int tellMove();
    public abstract int tellChosenRow();

    void setHand(ArrayList<Integer> hand) {
        this.hand = hand;
        Collections.sort(this.hand);
    }

    void updateScore(int points){
        score += points;
    }

    int getScore() {
        return score;
    }
}
