package Backend;

import java.util.*;
import static Backend.AbstractPlayer.updateStateTypes.*;

public abstract class AbstractPlayer {
    private static final int CARD_PENALTY[] = {0,
            1, 1, 1, 1, 2, 1, 1, 1, 1, 3,
            5, 1, 1, 1, 2, 1, 1, 1, 1, 3,
            1, 5, 1, 1, 2, 1, 1, 1, 1, 3,
            1, 1, 5, 1, 2, 1, 1, 1, 1, 3,
            1, 1, 1, 5, 2, 1, 1, 1, 1, 3,
            1, 1, 1, 1, 7, 1, 1, 1, 1, 3,
            1, 1, 1, 1, 2, 5, 1, 1, 1, 3,
            1, 1, 1, 1, 2, 1, 5, 1, 1, 3,
            1, 1, 1, 1, 2, 1, 1, 5, 1, 3,
            1, 1, 1, 1, 2, 1, 1, 1, 5, 3,
            1, 1, 1, 1};
    static final int DECK_SIZE = 104;
    static final int ROUNDS = 10;
    static final int ROWS = 4;
    private static final int COLUMNS = 5;
    static final int STOP_POINTS = 66;
    protected int id;
    int playersNumber;
    int remoteNumber;
    int botsNumber;
    volatile int chosenRowIndex;
    volatile int chosenCardIndex;

    public boolean isConnected() {
        return true;
    }

    enum updateStateTypes { ADD_CARD, CLEAR_ROW }
    ArrayList<Integer> scores;
    protected ArrayList<Integer> hand;
    ArrayList<ArrayList<Integer>> board;
    protected ArrayList<ArrayList<Integer>> currentBoard;

    private volatile boolean choosingRowToTake;
    private volatile boolean choosingCardToTake;

    private Queue<Move> queue;


    AbstractPlayer(int remoteNumber, int botsNumber) {
        this.playersNumber = remoteNumber + botsNumber;
        this.remoteNumber = remoteNumber;
        this.botsNumber = botsNumber;
        System.out.println(playersNumber + "; " + remoteNumber + "; " + botsNumber);

        scores = new ArrayList<>(Collections.nCopies(playersNumber, 0));
        queue = new  ArrayDeque<>();
        board = new ArrayList<>();
        currentBoard = new ArrayList<>();
        for (int i = 0; i < ROWS; ++i) {
            board.add(new ArrayList<Integer>());
            currentBoard.add(new ArrayList<Integer>());
        }
        hand = new ArrayList<>();
    }

    private class Move {
        updateStateTypes type;
        int player, rowIndex, card;

        private Move(updateStateTypes type, int player, int rowIndex, int card){
            this.type = type;
            this.player = player;
            this.rowIndex = rowIndex;
            this.card = card;
        }
    }
    public void updateOneMove(){

        if (id == 0)
            System.out.println("BEFORE UPDATING " + board);
        Move move = queue.poll();
        if (id == 0)
            System.out.println(move.type + " " + move.player + " " + move.rowIndex + " " + move.card);

        updateState(board, move.type, move.player, move.rowIndex, move.card);
        if (id == 0)
            System.out.println("AFTER UPDATING " + board);
    }

    public synchronized PriorityQueue<Integer> getCardsFromQueue(){
        PriorityQueue<Integer> res = new PriorityQueue<>();
        for (Move move: queue){
            res.add(move.card);
        }
        return res;
    }

    public Queue<Move> getQueue() {
        return queue;
    }

    public boolean isChoosingRowToTake(){
        return choosingRowToTake;
    }

    protected void setChoosingRowToTake(boolean value){
        choosingRowToTake = value;
    }

    public boolean isChoosingCardToTake() {
        return choosingCardToTake;
    }

    protected void setChoosingCardToTake(boolean value) {
        choosingCardToTake = value;
    }

    public abstract int tellMove();

    public abstract int tellChosenRow();

    public void tellRow(int index){
        chosenRowIndex = index;
        setChoosingRowToTake(false);
    }

    public void tellCard(int card){
        chosenCardIndex = card;
        setChoosingCardToTake(false);
    }

    public void setHand(ArrayList<Integer> hand) {
        this.hand = hand;
        Collections.sort(this.hand);
    }

    void setBoard(ArrayList<ArrayList<Integer>> board, ArrayList<ArrayList<Integer>> currentBoard){
        this.board = board;
        this.currentBoard = currentBoard;
    }

    int getMinOnBoard(){
        int minOnBoard = DECK_SIZE;
        for (ArrayList<Integer> cur : currentBoard){
            minOnBoard = Math.min(minOnBoard, cur.get(cur.size() - 1));
        }
        return minOnBoard;
    }

    protected void playRound(boolean smallestTook, int chosenRowIndex, ArrayList<Map.Entry<Integer, Integer>> moves) {

        int smallestCard = moves.get(0).getValue();
        int playerIndexWithSmallestCard = moves.get(0).getKey();
        if (smallestTook) {
            Move tmp = (new Move(CLEAR_ROW, playerIndexWithSmallestCard, chosenRowIndex, smallestCard));
            queue.add(new Move(CLEAR_ROW, playerIndexWithSmallestCard, chosenRowIndex, smallestCard));
            if (id == 0)
                System.out.println(tmp.type + " " + tmp.player + " " + tmp.rowIndex + " " + tmp.card);
            updateState(currentBoard, CLEAR_ROW, playerIndexWithSmallestCard, chosenRowIndex, smallestCard);
        }

        if (id == 0)
            System.out.println(id + "\tCURBOARD = " + currentBoard);
        if (id == 0)
            System.out.println(id + "\tBOARD = " + board);

        for (int i = smallestTook ? 1 : 0; i < playersNumber; i++){
            int currentCard = moves.get(i).getValue();
            int currentPlayer = moves.get(i).getKey();
            int updatingRowIndex = getUpdatingRowIndex(currentBoard, currentCard);
            if (currentBoard.get(updatingRowIndex).size() >= COLUMNS){
                Move tmp = new Move(ADD_CARD, currentPlayer, updatingRowIndex, currentCard);
                queue.add(new Move(CLEAR_ROW, currentPlayer, updatingRowIndex, currentCard));
                if (id == 0)
                    System.out.println(currentBoard.get(updatingRowIndex).size() + " " +tmp.type + " " + tmp.player + " " + tmp.rowIndex + " " + tmp.card);
                updateState(currentBoard, CLEAR_ROW, currentPlayer, updatingRowIndex, currentCard);
            } else {
                Move tmp = new Move(ADD_CARD, currentPlayer, updatingRowIndex, currentCard);
                queue.add(new Move(ADD_CARD, currentPlayer, updatingRowIndex, currentCard));
                if (id == 0)
                    System.out.println(currentBoard.get(updatingRowIndex).size() + " " + tmp.type + " " + tmp.player + " " + tmp.rowIndex + " " + tmp.card);
                updateState(currentBoard, ADD_CARD, currentPlayer, updatingRowIndex, currentCard);
            }
            if (id == 0)
                System.out.println(id + "\tCURBOARD = " + currentBoard);
            if (id == 0)
                System.out.println(id + "\tBOARD = " + board);

        }
    }

    protected int getUpdatingRowIndex(ArrayList<ArrayList<Integer>> board, int card) {
        int maxCard = 0;
        int index = 0;
        for (int i = 0; i < ROWS; i++){
            ArrayList<Integer> currentRow = board.get(i);
            int lastInRow = currentRow.get(currentRow.size() - 1);
            if (lastInRow < card && lastInRow > maxCard){
                maxCard = lastInRow;
                index = i;
            }
        }
        return index;
    }


    protected void updateState(ArrayList<ArrayList<Integer>> board, updateStateTypes type, int choosingPlayer, int rowIndex, int card) {
        ArrayList<Integer> row = board.get(rowIndex);
        if (type == CLEAR_ROW) {
            if (board == this.board) {
                updateScore(choosingPlayer, getRowPoints(row));
            }
            row.clear();
        }
        row.add(card);
    }

    static int getRowPoints(ArrayList<Integer> row) {
        int res = 0;
        for (Integer card : row){
            res += CARD_PENALTY[card];
        }
        return res;
    }

    private void updateScore(int playerIndex, int points){
        scores.set(playerIndex, scores.get(playerIndex) + points);
    }

    public ArrayList<Integer> getHand() {
        return hand;
    }

    public ArrayList<ArrayList<Integer>> getBoard() {
        return board;
    }

    void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    }

    int getScore() {
        return scores.get(getId());
    }

    public int getPlayersNumber() {
        return playersNumber;
    }

    public ArrayList<Integer> getScores(){
        return scores;
    }
}
