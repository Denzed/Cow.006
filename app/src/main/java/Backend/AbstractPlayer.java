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
    volatile int chosenRowIndex;
    volatile int chosenCardIndex;

    enum updateStateTypes { ADD_CARD, CLEAR_ROW }
    ArrayList<Integer> scores;
    protected ArrayList<Integer> hand;
    ArrayList<ArrayList<Integer>> board;

    private volatile boolean choosingRowToTake;
    private volatile boolean choosingCardToTake;

    private Queue<Move> queue;


    AbstractPlayer(int playersNumber) {
        this.playersNumber = playersNumber;
        scores = new ArrayList<>(Collections.nCopies(playersNumber, 0));
        queue = new ArrayDeque<>();
        board = new ArrayList<>();
        for (int i = 0; i < ROWS; ++i) {
            board.add(new ArrayList<>());
        }
        hand = new ArrayList<>();
    }


    private class Move {
        updateStateTypes type;
        int player, card;
        ArrayList<Integer> row;

        private Move(updateStateTypes type, int player, ArrayList<Integer> row, int card){
            this.type = type;
            this.player = player;
            this.row = row;
            this.card = card;
        }
    }

    public synchronized PriorityQueue<Integer> getCardsFromQueue(){
        PriorityQueue<Integer> res = new PriorityQueue<>();
        for (Move move: queue){
            res.add(move.card);
        }
        System.out.println("Player requested move queue of length " + res.size());
        return res;
    }

    public synchronized Queue<Move> getQueue() {
        return queue;
    }

    public synchronized void updateOneMove(){
        Move move = queue.poll();
        updateState(move.type, move.player, move.row, move.card);
    }

    public synchronized boolean isChoosingRowToTake(){
        return choosingRowToTake;
    }

    synchronized void setChoosingRowToTake(boolean value){
        choosingRowToTake = value;
    }

    boolean isChoosingCardToTake() {
        return choosingCardToTake;
    }

    void setChoosingCardToTake(boolean value) {
        choosingCardToTake = value;
    }

    public abstract int tellMove();

    public abstract int tellChosenRow();

    public synchronized void tellRow(int index){
        chosenRowIndex = index;
        setChoosingRowToTake(false);
    }

    public synchronized void tellCard(int card){
        chosenCardIndex = card;
        setChoosingCardToTake(false);
    }

    public void setHand(ArrayList<Integer> hand) {
        this.hand = hand;
        Collections.sort(this.hand);
        System.out.println(hand);
        System.out.println("SERVER GAVE HAND");
    }

    void setBoard(ArrayList<ArrayList<Integer>> board){
        this.board = board;
    }

    int getMinOnBoard(){
        int minOnBoard = DECK_SIZE;
        for (ArrayList<Integer> cur : board){
            minOnBoard = Math.min(minOnBoard, cur.get(cur.size() - 1));
        }
        return minOnBoard;
    }

    protected synchronized void playRound(boolean smallestTook,
                                          int chosenRowIndex,
                                          ArrayList<Map.Entry<Integer,Integer>> moves) {
        ArrayList<ArrayList<Integer>> currentBoard = new ArrayList<>();
        for (ArrayList<Integer> row: board) {
            currentBoard.add(new ArrayList<>(row));
        }

        int smallestCard = moves.get(0).getValue();
        int playerIndexWithSmallestCard = moves.get(0).getKey();
        if (smallestTook) {
            ArrayList<Integer> chosenRow = currentBoard.get(chosenRowIndex);
            ArrayList<Integer> chosenRowB = board.get(chosenRowIndex);
            queue.add(new Move(CLEAR_ROW, playerIndexWithSmallestCard, chosenRowB, smallestCard));
            updateState(CLEAR_ROW, playerIndexWithSmallestCard, chosenRow, smallestCard);
        }
        else {
            ArrayList<Integer> updatingRow = getUpdatingRow(currentBoard, smallestCard);
            ArrayList<Integer> updatingRowB = getUpdatingRow(board, smallestCard);
            queue.add(new Move(ADD_CARD, playerIndexWithSmallestCard, updatingRowB, smallestCard));
            updateState(ADD_CARD, playerIndexWithSmallestCard, updatingRow, smallestCard);
        }
        for (int i = 1; i < playersNumber; i++){
            int currentCard = moves.get(i).getValue();
            int currentPlayer = moves.get(i).getKey();
            ArrayList<Integer> updatingRow = getUpdatingRow(currentBoard, currentCard);
            ArrayList<Integer> updatingRowB = getUpdatingRow(board, currentCard);
            if (updatingRow.size() == COLUMNS){
                queue.add(new Move(CLEAR_ROW, currentPlayer, updatingRowB, currentCard));
                updateState(CLEAR_ROW, currentPlayer, updatingRow, currentCard);
            }
            else{
                queue.add(new Move(ADD_CARD, currentPlayer, updatingRowB, currentCard));
                updateState(ADD_CARD, currentPlayer, updatingRow, currentCard);
            }
        }
    }

    private ArrayList<Integer> getUpdatingRow(ArrayList<ArrayList<Integer>> board, int card) {
        int maxCard = 0;
        ArrayList<Integer> row = board.get(0);
        for (int i = 0; i < ROWS; i++){
            ArrayList<Integer> currentRow = board.get(i);
            int lastInRow = currentRow.get(currentRow.size() - 1);
            if (lastInRow < card && lastInRow > maxCard){
                maxCard = lastInRow;
                row = board.get(i);
            }
        }
        return row;
    }

    protected ArrayList<Integer> getUpdatingRow(int card) {
        int maxCard = 0;
        ArrayList<Integer> row = board.get(0);
        for (int i = 0; i < ROWS; i++){
            ArrayList<Integer> currentRow = board.get(i);
            int lastInRow = currentRow.get(currentRow.size() - 1);
            if (lastInRow < card && lastInRow > maxCard){
                maxCard = lastInRow;
                row = board.get(i);
            }
        }
        return row;
    }

    private void updateState(updateStateTypes type, int choosingPlayer, ArrayList <Integer> row, int card) {
        if (type == CLEAR_ROW) {
            updateScore(choosingPlayer, getRowPoints(row));
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
