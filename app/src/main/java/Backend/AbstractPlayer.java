package Backend;

import android.util.Pair;

import java.util.*;
import static Backend.AbstractPlayer.updateStateTypes.*;
import static Backend.GameConstants.*;

public abstract class AbstractPlayer {
    protected int id; //for game
    protected String username; //Google API
    protected String userID; //Google API
    protected ArrayList<String> finalResults; // 'name-result'
    int playersNumber;
    int remoteNumber;
    int botsNumber;
    volatile int chosenRowIndex; //???
    volatile int chosenCardIndex; // ???
    enum updateStateTypes { ADD_CARD, CLEAR_ROW }
    ArrayList<Integer> scores;
    protected ArrayList<Integer> hand;
    private Board board;
    private Board currentBoard;
    private volatile boolean choosingRowToTake; //???
    private volatile boolean choosingCardToTake; //???
    private volatile boolean gameStarted, gameInterrupted, gameFinished; //???
    private Queue<Move> queue;
    private ArrayDeque<Integer> cardsQueue;

    boolean isConnected() {
        return true;
    }

    public void setFinalResults(ArrayList<String> finalResults){
        this.finalResults = finalResults;
    }

    public ArrayList<String> getFinalResults() {
        return finalResults;
    }

    AbstractPlayer(int remoteNumber, int botsNumber) {
        this.playersNumber = remoteNumber + botsNumber;
        this.remoteNumber = remoteNumber;
        this.botsNumber = botsNumber;
        System.out.println(playersNumber + "; " + remoteNumber + "; " + botsNumber);

        scores = new ArrayList<>(Collections.nCopies(playersNumber, 0));
        queue = new ArrayDeque<>();
        cardsQueue = new ArrayDeque<>();
        board = new Board();
        currentBoard = new Board();
        for (int i = 0; i < ROWS; ++i) {
            board.add(new ArrayList<>());
            currentBoard.add(new ArrayList<>());
        }
        hand = new ArrayList<>();
    }

    AbstractPlayer(int remoteNumber, int botsNumber, String username, String userID){
        this(remoteNumber, botsNumber);
        this.username = username;
        this.userID = userID;
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
        cardsQueue.poll();
        Move move = queue.poll();
        if (id == 0)
            System.out.println(move.type + " " + move.player + " " + move.rowIndex + " " + move.card);

        updateState(board, move.type, move.player, move.rowIndex, move.card);
        if (id == 0)
            System.out.println("AFTER UPDATING " + board);
    }

    public ArrayDeque<Integer> getCardsQueue() {
        return cardsQueue;
    }

    void setCardsQueue(ArrayDeque<Integer> cardsQueue) {
        this.cardsQueue = cardsQueue;
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

    public abstract int move();

    public abstract int setChosenRow();

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

    void setBoard(Board board, Board currentBoard){
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

    protected void playRound(SmallestTakeTypes smallestTakeType, int chosenRowIndex, ArrayList<Pair<Integer, Integer>> moves) {
        int playerIndexWithSmallestCard = moves.get(0).first;
        int smallestCard = moves.get(0).second;
        if (smallestTakeType == SmallestTakeTypes.SMALLEST_TAKE) {
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

        for (int i = smallestTakeType == SmallestTakeTypes.SMALLEST_TAKE ? 1 : 0; i < playersNumber; i++){
            int currentPlayer = moves.get(i).first;
            int currentCard = moves.get(i).second;
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

    private int getUpdatingRowIndex(Board board, int card) {
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


    private void updateState(Board board, updateStateTypes type, int choosingPlayer, int rowIndex, int card) {
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

    public Board getBoard() {
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

    public boolean isGameStarted() {
        return gameStarted;
    }

    protected void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    protected void setGameFinished(boolean gameFinished) {
        this.gameFinished = gameFinished;
    }

    public boolean isGameInterrupted() {
        return gameInterrupted;
    }

    protected void setGameInterrupted(boolean gameInterrupted) {
        this.gameInterrupted = gameInterrupted;
    }
}