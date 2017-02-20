package Backend.Game;

import java.util.ArrayList;

import static Backend.Game.GameConstants.DECK_SIZE;
import static Backend.Game.GameConstants.NOT_A_CARD;
import static Backend.Game.Row.RowModificationTypes.CLEAR_ROW;

public class Board extends ArrayList<Row> {

    public int getMinOnBoard(){
        int minOnBoard = DECK_SIZE;
        for (Row currentRow : this){
            if (currentRow.isEmpty()) {
                return NOT_A_CARD;
            }
            minOnBoard = Math.min(minOnBoard, currentRow.get(currentRow.size() - 1));
        }
        return minOnBoard;
    }

    public void applyBoardModification(BoardModification boardModification) {
        Row row = this.get(boardModification.getRowIndex());
        if (boardModification.getType() == CLEAR_ROW) {
            row.clear();
        } else {
            row.add(boardModification.getCard());
        }
    }

}

