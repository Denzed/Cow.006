package Backend;

import java.util.ArrayList;
import java.util.Scanner;


public class Player  extends AbstractPlayer{

    public Player(int playersNumber) {
        super(playersNumber);
    }

    public int tellMove() {
        askForAMove();

        int value;
        Scanner in = new Scanner(System.in);
        while(true) {
            value = in.nextInt();
            if (hand.contains(value)) {
                hand.remove(Integer.valueOf(value));
                System.out.print("Played: " + value);
                break;
            }
            else {
                System.out.println("You don't have this card");
            }
        }
        return value;
    }

    public int tellChosenRow() {
        askForAChoice();

        int index;
        Scanner in = new Scanner(System.in);
        while(true) {
            index = in.nextInt();
            if (0 <= index && index < ROWS) {
                return index;
            }
            else {
                System.out.println("Not in range");
            }
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
