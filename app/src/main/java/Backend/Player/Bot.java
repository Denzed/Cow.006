package Backend.Player;

import java.util.Random;

import Backend.Game.Board;
import Backend.Game.Row;

import static Backend.Game.GameConstants.*;

public class Bot extends AbstractPlayer {

    public Bot(int playersNumber, PlayerInformation playerInformation) {
        super(playersNumber, playerInformation);
    }

    public int chooseCard() {
        int index = new Random().nextInt(hand.size());
        int value = hand.get(index);
        hand.remove(Integer.valueOf(value));
        return value;
    }

    public int chooseRow() {
        if (board.getMinOnBoard() < getCardsQueue().peek()){
            return -1;
        }

        int minPoints = Integer.MAX_VALUE;
        int index = 0;
        Board board = this.getBoard();
        for (int i = 0; i < ROWS; i++){
            Row row = board.get(i);
            int rowPoints = row.getRowPenalty();
            if (rowPoints < minPoints){
                minPoints = rowPoints;
                index = i;
            }
        }
        return index;
    }

    @Override
    public void buildBoardModificationsQueue(int chosenRowIndex) {
        super.buildBoardModificationsQueue(chosenRowIndex);
        while (!getBoardModificationQueue().isEmpty()){
            updateOneTurn();
        }
    }

}
