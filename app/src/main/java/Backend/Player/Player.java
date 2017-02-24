package Backend.Player;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Player extends AbstractPlayer {

    public Player(int playersNumber, PlayerInformation playerInformation) {
        super(playersNumber, playerInformation);
    }

    public int chooseCard() {
        showScores();
        askForAMove();
        setChoosingCardToTake(true);
        while (isChoosingCardToTake()){
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }

        System.out.println("CARD HAS BEEN CHOSEN: " + chosenCardValue);
        int value = chosenCardValue;
//        int value = new Scanner(System.in).nextInt();
        hand.remove(Integer.valueOf(value));
        return value;
    }

    public int chooseRow() {
        if (board.getMinOnBoard() < getCardsQueue().peek()){
            return -1;
        }
        showScores();
        askForAChoice();


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
        return index;
    }

    private void askForAMove(){
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
        for (Integer x : getScores()){
            System.out.print(x + " ");
        }
        System.out.println();
    }

/*    @Override
    public void buildBoardModificationsQueue(int chosenRowIndex) {
        super.buildBoardModificationsQueue(chosenRowIndex);
        while (!getBoardModificationQueue().isEmpty()){
            updateOneTurn();
        }
    }
*/
}
