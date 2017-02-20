package Backend.Game;

public class Turn {

    private int card;
    private int ID;

    public Turn(int card, int ID){
        this.card = card;
        this.ID = ID;
    }

    public int getCard() {
        return card;
    }

    public int getID() {
        return ID;
    }

}
