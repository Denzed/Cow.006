package Backend;

import android.util.Pair;
//import javafx.util.Pair;
import java.util.*;

import static Backend.AbstractPlayer.updateStateTypes.*;
import static Backend.GameConstants.*;

public abstract class AbstractPlayer {

    protected int id; //for game
    protected String username; //Google API
    protected String userID; //Google API
    protected ArrayList<ArrayList<String>> finalResults; // 'name-result'
    protected int playersNumber;
    protected int remoteNumber;
    protected int botsNumber;
    volatile int chosenRowIndex; //???
    volatile int chosenCardValue; // ???
    ArrayList<Integer> scores;
    public enum updateStateTypes { ADD_CARD, CLEAR_ROW }
    protected ArrayList<Integer> hand;
    private Board board;
    private Board currentBoard;
    public enum GameState { NEW_GAME, NEXT_ROUND, INTERRUPTED, FINISHED }
    private volatile GameState state;
    private volatile boolean choosingRowToTake, choosingCardToTake;
    private Queue<Move> queue;
    private ArrayDeque<Integer> cardsQueue;

    boolean isConnected() {
        return true;
    }

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

    public class Move {
        public updateStateTypes type;
        public int player, rowIndex, card;

        private Move(updateStateTypes type, int player, int rowIndex, int card){
            this.type = type;
            this.player = player;
            this.rowIndex = rowIndex;
            this.card = card;
        }
    }

    public void updateOneMove(){
        cardsQueue.poll();
        Move move = queue.poll();
        updateState(board, move.type, move.player, move.rowIndex, move.card);
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
        chosenCardValue = card;
        setChoosingCardToTake(false);
    }

    protected void setHand(ArrayList<Integer> hand) {
        this.hand = hand;
        if (this.state == GameState.NEW_GAME) {
            this.state = GameState.NEXT_ROUND;
        }
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
            queue.add(new Move(CLEAR_ROW, playerIndexWithSmallestCard, chosenRowIndex, smallestCard));
            updateState(currentBoard, CLEAR_ROW, playerIndexWithSmallestCard, chosenRowIndex, smallestCard);
        }

        for (int i = smallestTakeType == SmallestTakeTypes.SMALLEST_TAKE ? 1 : 0; i < playersNumber; i++){
            int currentPlayer = moves.get(i).first;
            int currentCard = moves.get(i).second;
            int updatingRowIndex = getUpdatingRowIndex(currentBoard, currentCard);
            updateStateTypes updateStateType = currentBoard.get(updatingRowIndex).size() >= COLUMNS ? CLEAR_ROW : ADD_CARD;
            queue.add(new Move(updateStateType, currentPlayer, updatingRowIndex, currentCard));
            updateState(currentBoard, updateStateType, currentPlayer, updatingRowIndex, currentCard);
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

    protected void updateState(Board board, updateStateTypes type, int choosingPlayer, int
            rowIndex, int card) {
        ArrayList<Integer> row = board.get(rowIndex);
        if (type == CLEAR_ROW) {
            if (board == this.board) {
                updateScore(choosingPlayer, getRowPoints(row));
            }
            row.clear();
        }
        row.add(card);
    }

    protected static int getRowPoints(ArrayList<Integer> row) {
        int res = 0;
        for (Integer card : row){
            res += CARD_PENALTY[card];
        }
        return res;
    }

    protected void updateScore(int playerIndex, int points){
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

    protected void setGameInterrupted() {
        this.state = GameState.INTERRUPTED;
    }

    protected void setGameFinished() {
        this.state = GameState.FINISHED;
    }

    public GameState getState() {
        return this.state;
    }

}