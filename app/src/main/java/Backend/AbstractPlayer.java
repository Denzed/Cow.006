package Backend;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static Backend.AbstractPlayer.updateStateTypes.*;


abstract class AbstractPlayer {
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
    private int id;
    int playersNumber;

    enum updateStateTypes { ADD_CARD, CLEAR_ROW }
    List<Integer> scores = Arrays.asList(0, 0, 0, 0);
    ArrayList<Integer> hand;
    ArrayList<ArrayList<Integer>> board;

    AbstractPlayer(int playersNumber){
        this.playersNumber = playersNumber;
    }
    public abstract int tellMove();

    public abstract int tellChosenRow();

    void setHand(ArrayList<Integer> hand) {
        this.hand = hand;
        Collections.sort(this.hand);
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


    void playRound(boolean smallestTook, int chosenRowIndex, ArrayList<Pair<Integer, Integer>> moves) {

        int smallestCard = moves.get(0).getValue();
        int playerIndexWithSmallestCard = moves.get(0).getKey();
        if (smallestTook) {
            ArrayList<Integer> chosenRow = board.get(chosenRowIndex);
            updateState(CLEAR_ROW, playerIndexWithSmallestCard, chosenRow, smallestCard);
        }
        else {
            ArrayList<Integer> updatingRow = getUpdatingRow(smallestCard);
            updateState(ADD_CARD, playerIndexWithSmallestCard, updatingRow, smallestCard);
        }
        for (int i = 1; i < playersNumber; i++){
            int currentCard = moves.get(i).getValue();
            int currentPlayer = moves.get(i).getKey();
            ArrayList<Integer> updatingRow = getUpdatingRow(currentCard);
            if (updatingRow.size() == COLUMNS){
                updateState(CLEAR_ROW, currentPlayer, updatingRow, currentCard);
            }
            else{
                updateState(ADD_CARD, currentPlayer, updatingRow, currentCard);
            }
        }
    }

    private ArrayList<Integer> getUpdatingRow(int card) {
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
        return  res;
    }

    private void updateScore(int playerIndex, int points){
        scores.set(playerIndex, scores.get(playerIndex) + points);
    }

    void setId(int id) {
        this.id = id;
    }

    int getId(){
        return id;
    }

    int getScore() {
        return scores.get(getId());
    }

}
