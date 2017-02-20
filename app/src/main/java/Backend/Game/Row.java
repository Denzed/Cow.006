package Backend.Game;

import java.util.ArrayList;
import java.util.List;

import static Backend.Game.GameConstants.CARD_PENALTY;
import static Backend.Game.GameConstants.ROWS;


public class Row extends ArrayList<Integer>{

    public enum RowModificationTypes { ADD_CARD, CLEAR_ROW };

    public int getRowPenalty() {
        int res = 0;
        for (Integer card : this) {
            res += CARD_PENALTY[card];
        }
        return res;
    }

    public static int getModifyingRowIndex(Board board, int card) {
        int maxCard = 0;
        int index = 0;
        for (int i = 0; i < ROWS; i++){
            List<Integer> currentRow = board.get(i);
            if (currentRow.isEmpty()) {
                index = i;
                break;
            }
            int lastInRow = currentRow.get(currentRow.size() - 1);
            if (lastInRow < card && lastInRow > maxCard){
                maxCard = lastInRow;
                index = i;
            }
        }
        return index;
    }

}
