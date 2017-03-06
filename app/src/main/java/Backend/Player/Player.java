package Backend.Player;

import java.util.concurrent.TimeUnit;

public class Player extends AbstractPlayer {

    public Player(int playersNumber, PlayerInformation playerInformation) {
        super(playersNumber, playerInformation);
    }

    public int chooseCard() {
        setChoosingCardToTake(true);
        while (isChoosingCardToTake()){
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }
        int value = chosenCardValue;
        hand.remove(Integer.valueOf(value));
        return value;
    }

    public int chooseRow() {
        if (board.getMinOnBoard() < getCardsQueue().peek()){
            return -1;
        }

        setChoosingRowToTake(true);
        while (isChoosingRowToTake()){
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }

        return chosenRowIndex;
    }

}