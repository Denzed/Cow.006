package Backend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;


public class Player extends AbstractPlayer {

    public Player(int remoteNumber, int botsNumber) {
        super(remoteNumber, botsNumber);
    }

    public int tellMove() {
//        askForAMove();
        setChoosingCardToTake(true);
        while (isChoosingCardToTake()){}
        int value = chosenCardIndex;
//        int value = new Scanner(System.in).nextInt();
        if (hand.contains(value)) {
            hand.remove(Integer.valueOf(value));
            System.out.print("Played: " + value);
        }
        else {
            System.out.println("You don't have this card");
        }
        return value;
    }

    public int tellChosenRow() {
//        askForAChoice();
        setChoosingRowToTake(true);
        while (isChoosingRowToTake()){}
        int index = chosenRowIndex;
//      int index = new Scanner(System.in).nextInt();

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
        for (ArrayList<Integer> row : board){
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
            }
            else{
                System.out.print(scores.get(i) + " ");
            }
        }
        System.out.println();

    }

}
