package Backend.Game;

import Backend.Player.AbstractPlayer;

public class BoardModification extends Turn {

    private Row.RowModificationTypes type;
    private int rowIndex;

    public BoardModification(int card, int ID, Row.RowModificationTypes type, int rowIndex) {
        super(card, ID);
        this.type = type;
        this.rowIndex = rowIndex;
    }

    public Row.RowModificationTypes getType() {
        return type;
    }

    public int getRowIndex() {
        return rowIndex;
    }



}
