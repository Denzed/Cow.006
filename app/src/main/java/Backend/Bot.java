package Backend;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


public class Bot extends AbstractPlayer {

/*    private final GameHandler gh;
    Bot(GameHandler gh){
        this.gh = gh;
    }
*/    public int tellMove() {
        System.out.println("Please, make a move");

        int index = ThreadLocalRandom.current().nextInt(0, hand.size());
        int value = hand.get(index);
        hand.remove(Integer.valueOf(value));

        System.out.print("Played: " + value);
        System.out.print(" Cards left: ");
        for (int x : hand){
            System.out.print(x + " ");
        }
        System.out.println();

        return value;
    }

    public int tellChosenRow() {
//        int minPoints = Integer.MAX_VALUE;
        int index = 0;
/*        ArrayList<ArrayList<Integer>> board = gh.getBoard();
        for (int i = 0; i < ROWS; i++){
            ArrayList<Integer> row = board.get(i);
            int rowPoints = getRowPoints(row);
            if (rowPoints < minPoints){
                minPoints = rowPoints;
                index = i;
            }
        }
*/        return index;
    }

}
