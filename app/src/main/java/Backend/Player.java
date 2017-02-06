package Backend;

//import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static Backend.GameConstants.*;

//some lines are commented: it's for console testing
public class Player extends AbstractPlayer {

    public Player(int remoteNumber, int botsNumber) {
        super(remoteNumber, botsNumber);
    }

    public Player(int remoteNumber, int botsNumber, String username, String userID) {
        super(remoteNumber, botsNumber, username, userID);
    }


    public int move() {
        askForAMove();

//        just wait while player is choosing card
        setChoosingCardToTake(true);
        while (isChoosingCardToTake()){
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }

        int value = chosenCardValue;
//        int value = new Scanner(System.in).nextInt();
        hand.remove(Integer.valueOf(value));
        return value;
    }

    public int setChosenRow() {
        askForAChoice();

//        just wait while player is choosing row
        setChoosingRowToTake(true);
        while (isChoosingRowToTake()){
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }
        int index = chosenRowIndex;

//        int index = new Scanner(System.in).nextInt();

        if (0 <= index && index < ROWS) {
            return index;
        } else {
            System.out.println("Not in range");
            return 0;
        }
    }

    private void askForAMove(){
        showScores();
        showBoard();
        System.out.println("Please, make a move");
        showCardsLeft();
    }

    private void showCardsLeft(){
        System.out.print("Cards left: ");
        for (Integer card : hand){
            System.out.print(card + " ");
        }
        System.out.println();
    }

    private void askForAChoice() {
        System.out.println("Please, choose a row");
    }

    private void showBoard(){
        System.out.println("BOARD:");
        for (ArrayList<Integer> row : getBoard()){
            for (Integer card : row) {
                System.out.print(card + " ");
            }
            System.out.println();
        }
    }

    private void showScores(){
        System.out.print("SCORES: ");
        for (int i = 0; i < playersNumber; i++){
            if (i == getId()){
                System.out.print("(YOU: " + scores.get(i) + ") ");
            } else {
                System.out.print(scores.get(i) + " ");
            }
        }
        System.out.println();

    }

/*    @Override
    protected void playRound(SmallestTakeTypes smallestTakeType, int chosenRowIndex, ArrayList<Pair<Integer, Integer>> moves) {
        super.playRound(smallestTakeType, chosenRowIndex, moves);
        while (!getQueue().isEmpty()){
            updateOneMove();
        }
    }
*/
}
