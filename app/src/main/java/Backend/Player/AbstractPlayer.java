package Backend.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

import Backend.Game.Board;
import Backend.Game.BoardModification;
import Backend.Game.Row;
import Backend.Game.Turn;

import static Backend.Game.GameConstants.COLUMNS;
import static Backend.Game.GameConstants.NOT_A_CARD;
import static Backend.Game.Row.RowModificationTypes.ADD_CARD;
import static Backend.Game.Row.RowModificationTypes.CLEAR_ROW;
import static Backend.Game.Row.getModifyingRowIndex;
import static java.lang.Integer.parseInt;

public abstract class AbstractPlayer {

    private List<List<String>> finalResults; //???
    private PlayerInformation playerInformation;
    private List<PlayerInformation> playerInformations;
    private int playersNumber;
    protected volatile List<Integer> hand;
    volatile int chosenRowIndex;
    volatile int chosenCardValue;
    private List<Integer> scores;
    volatile Board board;
    private volatile Board currentBoard;
    private volatile GameState state;
    private volatile boolean choosingRowToTake;
    private volatile boolean choosingCardToTake;
    private volatile Queue<BoardModification> boardModificationQueue;
    private volatile Deque<Integer> cardsQueue;
    private volatile Queue<Turn> turnsQueue;

    AbstractPlayer(int playersNumber, PlayerInformation playerInformation){
        this.state = GameState.NEW_GAME;
        scores = new ArrayList<>(Collections.nCopies(playersNumber, 0));
        this.playersNumber = playersNumber;
        boardModificationQueue = new ArrayDeque<>();
        board = new Board();
        currentBoard = new Board();
        hand = new ArrayList<>();
        cardsQueue = new ArrayDeque<>();
        turnsQueue = new ArrayDeque<>();
        finalResults = new ArrayList<>();
        this.playerInformation = playerInformation;
    }

    public void updateOneTurn(){
        cardsQueue.poll();
        BoardModification boardModification = boardModificationQueue.poll();
        if (boardModification.getType() == CLEAR_ROW){
            updateScore(
                    boardModification.getID(),
                    board.get(boardModification.getRowIndex()).getRowPenalty());
        }
        board.applyBoardModification(boardModification);
    }

    public void buildBoardModificationsQueue(int chosenRowIndex) {
        int playerIndexWithSmallestCard = turnsQueue.peek().getID();
        int smallestCard = turnsQueue.peek().getCard();
        if (chosenRowIndex != -1) {
            BoardModification boardModification = (new BoardModification(
                    NOT_A_CARD, playerIndexWithSmallestCard, CLEAR_ROW, chosenRowIndex));
            boardModificationQueue.add(boardModification);
            currentBoard.applyBoardModification(boardModification);

            boardModification = new BoardModification(
                    smallestCard, playerIndexWithSmallestCard, ADD_CARD, chosenRowIndex);
            boardModificationQueue.add(boardModification);
            currentBoard.applyBoardModification(boardModification);
            turnsQueue.poll();
        }

        while (!turnsQueue.isEmpty()){
            int currentPlayer = turnsQueue.peek().getID();
            int currentCard = turnsQueue.peek().getCard();
            turnsQueue.poll();
            int modifyingRowIndex = getModifyingRowIndex(currentBoard, currentCard);
            Row.RowModificationTypes rowModificationType = currentBoard.get(modifyingRowIndex).size() >= COLUMNS ? CLEAR_ROW : ADD_CARD;
            BoardModification boardModification;

            if (rowModificationType == CLEAR_ROW) {
                 boardModification = new BoardModification(
                        NOT_A_CARD, currentPlayer, CLEAR_ROW, modifyingRowIndex);
                boardModificationQueue.add(boardModification);
                currentBoard.applyBoardModification(boardModification);
            }

            boardModification = new BoardModification(
                    currentCard, currentPlayer, ADD_CARD, modifyingRowIndex);
            boardModificationQueue.add(boardModification);
            currentBoard.applyBoardModification(boardModification);
        }
    }

    protected void updateScore(int playerIndex, int points) {
        scores.set(playerIndex, scores.get(playerIndex) + points);
    }

    public void buildSinglePlayFinalResults() {
        ArrayList<String> legend = new ArrayList<>();
        legend.addAll(Arrays.asList("Player", "Score"));
        finalResults.add(legend);
        for (PlayerInformation playerInformation : playerInformations){
            List<String> resultLine = new ArrayList<>();
            resultLine.add(playerInformation.getStrippedUsername());
            resultLine.add(scores.get(playerInformations.indexOf(playerInformation)).toString());
            finalResults.add(resultLine);
        }
        Collections.sort(finalResults.subList(1, finalResults.size()),
                (a, b) -> parseInt(a.get(1)) - parseInt(b.get(1)));
    }

    public void buildMultiPlayFinalResults(List<String> ratings, List<String> ratingChanges) {
        ArrayList<String> legend = new ArrayList<>();
        legend.addAll(Arrays.asList("Player", "Score", "New Rating", "Delta"));
        finalResults.add(legend);
        for (int i = 0; i < playersNumber; i++){
            ArrayList<String> resultLine = new ArrayList<>();
            resultLine.add(playerInformations.get(i).getStrippedUsername());
            resultLine.add(String.valueOf(scores.get(i)));
            resultLine.add(ratings.get(i));
            resultLine.add("(" + ((parseInt(ratingChanges.get(i)) > 0) ? "+" : "") + ratingChanges.get(i) + ")");
            finalResults.add(resultLine);
        }
        Collections.sort(finalResults.subList(1, finalResults.size()),
                (a, b) -> parseInt(a.get(1)) - parseInt(b.get(1)));
    }

    protected List<List<String>> getFinalResults() {
        return finalResults;
    }

    public synchronized List<Integer> getHand() {
        return hand;
    }

    public void setHand(List<Integer> hand) {
        this.hand = Collections.synchronizedList(hand);
        if (this.state == GameState.NEW_GAME) {
            this.state = GameState.NEXT_ROUND;
        }
        Collections.sort(this.hand);
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public List<Integer> getScores(){
        return scores;
    }

    public void setGameInterrupted() {}

    public void setGameFinished() {}

    protected GameState getState() {
        return this.state;
    }

    public PlayerInformation getPlayerInformation() {
        return playerInformation;
    }

    protected List<PlayerInformation> getPlayersInformations() {
        return playerInformations;
    }

    public void setPlayersInformations(List<PlayerInformation> playerInformations) {
        this.playerInformations = playerInformations;
    }

    public void setCurrentBoard(Board currentBoard) {
        this.currentBoard = currentBoard;
    }

    public void setTurnsQueue(Queue<Turn> turnsQueue) {
        this.turnsQueue = turnsQueue;
    }

    protected enum GameState {NEW_GAME, NEXT_ROUND}

    public int getPlayersNumber() {
        return playersNumber;
    }

    public synchronized Deque<Integer> getCardsQueue() {return cardsQueue;}

    public void setCardsQueue(Deque<Integer> cardsQueue) {
        this.cardsQueue = cardsQueue;
    }

    public Queue<BoardModification> getBoardModificationQueue() {
        return boardModificationQueue;
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

    void setChoosingCardToTake(boolean value) {
        choosingCardToTake = value;
    }

    public abstract int chooseCard();

    public abstract int chooseRow();

    public void tellRow(int index){
        chosenRowIndex = index;
        setChoosingRowToTake(false);
    }

    public void tellCard(int card){
        chosenCardValue = card;
        setChoosingCardToTake(false);
    }

}