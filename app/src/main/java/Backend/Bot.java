package Backend;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


public class Bot extends AbstractPlayer {

    public Bot(int playersNumber) {
        super(playersNumber);
    }

    public int tellMove() {
        //Just choose a random card
        int index = ThreadLocalRandom.current().nextInt(0, hand.size());
        int value = hand.get(index);
        hand.remove(Integer.valueOf(value));
        return value;
    }

    public int tellChosenRow() {
        //Just choose a row to get as few points as possible
        int minPoints = Integer.MAX_VALUE;
        int index = 0;
        ArrayList<ArrayList<Integer>> board = this.board;
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

}
