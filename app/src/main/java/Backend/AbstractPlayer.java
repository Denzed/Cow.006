package Backend;

import android.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;

import static Backend.AbstractPlayer.updateStateTypes.ADD_CARD;
import static Backend.AbstractPlayer.updateStateTypes.CLEAR_ROW;
import static Backend.GameConstants.CARD_PENALTY;
import static Backend.GameConstants.COLUMNS;
import static Backend.GameConstants.DECK_SIZE;
import static Backend.GameConstants.NOT_A_CARD;
import static Backend.GameConstants.ROWS;
import static Backend.GameConstants.SmallestTakeTypes;

//import javafx.util.Pair;

public abstract class AbstractPlayer {

    protected int id; //for game
    protected String username; //Google API
    protected String userID; //Google API
    protected ArrayList<ArrayList<String>> finalResults; // 'name-result'
    protected int playersNumber;
    protected int remoteNumber;
    protected int botsNumber;
    protected volatile ArrayList<Integer> hand;
    volatile int chosenRowIndex; //???
    volatile int chosenCardValue; // ???
    ArrayList<Integer> scores;
    private volatile Board board;
    private volatile Board currentBoard;
    private volatile GameState state;
    private volatile boolean choosingRowToTake, choosingCardToTake;
    private volatile Queue<Move> queue;
    private volatile ArrayDeque<Integer> cardsQueue;
    AbstractPlayer(int remoteNumber, int botsNumber) {
        this.state = GameState.NEW_GAME;
        this.playersNumber = remoteNumber + botsNumber;
        this.remoteNumber = remoteNumber;
        this.botsNumber = botsNumber;

        scores = new ArrayList<>(Collections.nCopies(playersNumber, 0));
        queue = new ArrayDeque<>();
        cardsQueue = new ArrayDeque<>();
        board = new Board();
        currentBoard = new Board();
        finalResults = new ArrayList<>();
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

    protected static int getRowPoints(ArrayList<Integer> row) {
        int res = 0;
        for (Integer card : row) {
            res += CARD_PENALTY[card];
        }
        return res;
    }

    boolean isConnected() {
        return true;
    }

    public void updateOneMove(){
        cardsQueue.poll();
        Move move = queue.poll();
        updateState(board, move.type, move.player, move.rowIndex, move.card);
    }

    public ArrayDeque<Integer> getCardsQueue() {
        return cardsQueue;
    }

    protected void setCardsQueue(ArrayDeque<Integer> cardsQueue) {
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
        chosenCardValue = card;
        setChoosingCardToTake(false);
    }

    void setBoard(Board board, Board currentBoard) {
        setBoard(currentBoard);
        this.board = board;
    }

    int getMinOnBoard(){
        int minOnBoard = DECK_SIZE;
        for (ArrayList<Integer> cur : currentBoard){
            if (cur.isEmpty()) {
                return NOT_A_CARD;
            }
            minOnBoard = Math.min(minOnBoard, cur.get(cur.size() - 1));
        }
        return minOnBoard;
    }

    protected void playRound(SmallestTakeTypes smallestTakeType, int chosenRowIndex, ArrayList<Pair<Integer, Integer>> moves) {
        int playerIndexWithSmallestCard = moves.get(0).first;
        int smallestCard = moves.get(0).second;
        if (smallestTakeType == SmallestTakeTypes.SMALLEST_TAKE) {
            queue.add(new Move(CLEAR_ROW, playerIndexWithSmallestCard, chosenRowIndex, NOT_A_CARD));
            updateState(currentBoard, CLEAR_ROW, playerIndexWithSmallestCard, chosenRowIndex,
                    NOT_A_CARD);
            queue.add(new Move(ADD_CARD, playerIndexWithSmallestCard, chosenRowIndex, smallestCard));
            updateState(currentBoard, ADD_CARD, playerIndexWithSmallestCard, chosenRowIndex,
                    smallestCard);

        }

        for (int i = smallestTakeType == SmallestTakeTypes.SMALLEST_TAKE ? 1 : 0; i < playersNumber; i++){
            int currentPlayer = moves.get(i).first;
            int currentCard = moves.get(i).second;
            int updatingRowIndex = getUpdatingRowIndex(currentBoard, currentCard);
            updateStateTypes updateStateType = currentBoard.get(updatingRowIndex).size() >= COLUMNS ? CLEAR_ROW : ADD_CARD;
            if (updateStateType == CLEAR_ROW) {
                queue.add(new Move(CLEAR_ROW, currentPlayer, updatingRowIndex, currentCard));
                updateState(currentBoard, CLEAR_ROW, currentPlayer, updatingRowIndex, NOT_A_CARD);
            }
            queue.add(new Move(ADD_CARD, currentPlayer, updatingRowIndex, currentCard));
            updateState(currentBoard, ADD_CARD, currentPlayer, updatingRowIndex, currentCard);
        }
    }

    private int getUpdatingRowIndex(Board board, int card) {
        int maxCard = 0;
        int index = 0;
        for (int i = 0; i < ROWS; i++){
            ArrayList<Integer> currentRow = board.get(i);
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

    protected void updateState(Board board, updateStateTypes type, int choosingPlayer, int
            rowIndex, int card) {
        ArrayList<Integer> row = board.get(rowIndex);
        if (type == CLEAR_ROW) {
            if (board == this.board) {
                updateScore(choosingPlayer, getRowPoints(row));
            }
            row.clear();
        } else {
            row.add(card);
        }
    }

    protected void updateScore(int playerIndex, int points) {
        scores.set(playerIndex, scores.get(playerIndex) + points);
    }

    void buildFinalResultsSinglePlayer() {
        System.out.println("    void buildFinalResultsSinglePlayer() { <- HERE I AM");
        ArrayList<String> legend = new ArrayList<>();
        legend.addAll(Arrays.asList("Player", "Score"));
        finalResults.add(legend);
        for (int i = 0; i < playersNumber; i++){
            ArrayList<String> resultLine = new ArrayList<>();
            resultLine.add((i == id) ? "YOU" : "Opponent #" + i);
            resultLine.add(scores.get(i).toString());
            finalResults.add(resultLine);
        }
    }

    void buildFinalResultsMultiPlayer(ArrayList<String> usernames, ArrayList<String> ratings, ArrayList<String> ratingChanges) {
        ArrayList<String> legend = new ArrayList<>();
        legend.addAll(Arrays.asList("Player", "Score", "Rating", "Delta"));
        finalResults.add(legend);
        for (int i = 0; i < playersNumber; i++){
            ArrayList<String> resultLine = new ArrayList<>();
            resultLine.add(usernames.get(i) + ((i == id) ? "(YOU)" : ""));
            resultLine.add(String.valueOf(scores.get(i)));
            resultLine.add(ratings.get(i));
            resultLine.add(ratingChanges.get(i));
            finalResults.add(resultLine);
        }
    }

    public ArrayList<ArrayList<String>> getFinalResults() {
        return finalResults;
    }

    public ArrayList<Integer> getHand() {
        return hand;
    }

    protected void setHand(ArrayList<Integer> hand) {
        this.hand = hand;
        if (this.state == GameState.NEW_GAME) {
            this.state = GameState.NEXT_ROUND;
        }
        Collections.sort(this.hand);
    }

    public Board getBoard() {
        return board;
    }

    protected void setBoard(Board currentBoard) {
        this.currentBoard = currentBoard;
    }

    public int getId(){
        return id;
    }

    void setId(int id) {
        this.id = id;
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

    protected void setGameInterrupted() {
    }

    protected void setGameFinished() {
    }

    public GameState getState() {
        return this.state;
    }

    public enum updateStateTypes {ADD_CARD, CLEAR_ROW}

    public enum GameState {NEW_GAME, NEXT_ROUND}

    public class Move {
        public updateStateTypes type;
        public int player, rowIndex, card;

        private Move(updateStateTypes type, int player, int rowIndex, int card) {
            this.type = type;
            this.player = player;
            this.rowIndex = rowIndex;
            this.card = card;
        }
    }

}