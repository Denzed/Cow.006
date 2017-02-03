package Backend;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Random;

import static Backend.GameConstants.*;


public class Bot extends AbstractPlayer {

    public Bot(int remoteNumber, int botsNumber) {
        super(remoteNumber, botsNumber);
    }

    public int move() {
        //Just choose a random card
        int index = new Random().nextInt(hand.size());
        int value = hand.get(index);
        hand.remove(Integer.valueOf(value));
        return value;
    }

    public int setChosenRow() {
        //Just choose a row to get as few points as possible
        int minPoints = Integer.MAX_VALUE;
        int index = 0;
        Board board = this.getBoard();
        for (int i = 0; i < ROWS; i++){
            ArrayList<Integer> row = board.get(i);
            int rowPoints = getRowPoints(row);
            if (rowPoints < minPoints){
                minPoints = rowPoints;
                index = i;
            }
        }
        return index;
    }

    @Override
    protected void playRound(SmallestTakeTypes smallestTakeType, int chosenRowIndex, ArrayList<Pair<Integer, Integer>> moves) {
        super.playRound(smallestTakeType, chosenRowIndex, moves);
        while (!getQueue().isEmpty()){
            updateOneMove();
        }
    }
}
