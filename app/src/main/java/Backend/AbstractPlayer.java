package Backend;

import javafx.util.Pair;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static Backend.AbstractPlayer.updateStateTypes.*;

abstract class AbstractPlayer {
    static final int CARD_PENALTY[] = {0,
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
    static final int COLUMNS = 5;
    static final int STOP_POINTS = 66;
    enum updateStateTypes { ADD_CARD, CLEAR_ROW }


    List<Integer> scores = Arrays.asList(0, 0, 0, 0);
    ArrayList<Integer> hand;
    ArrayList<ArrayList<Integer>> board;

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

        System.out.println("st = " + smallestTook + " cri = " + chosenRowIndex);
        for (Pair <Integer, Integer> tmp : moves){
            System.out.println("move: " + tmp);
        }
        if (smallestTook) {
            ArrayList<Integer> chosenRow = board.get(chosenRowIndex);
            updateState(CLEAR_ROW, moves.get(0).getKey(), chosenRow, moves.get(0).getValue());
        }
        else {
            updateState(ADD_CARD, moves.get(0).getKey(), getUpdatingRow(moves.get(0).getValue()), moves.get(0).getValue());
        }

        for (int i = 1; i < 4; i++){
            int currentCard = moves.get(i).getValue();
            System.out.println("Player #" + moves.get(i).getKey() + " turn, card = " + currentCard);

            ArrayList<Integer> updatingRow = getUpdatingRow(currentCard);
            if (updatingRow.size() == COLUMNS){
                updateState(CLEAR_ROW, moves.get(i).getKey(), updatingRow, currentCard);
            }
            else{
                updateState(ADD_CARD, moves.get(i).getKey(), updatingRow, currentCard);
            }
        }
        System.out.print("SCORES: ");
        for (int i = 0; i < 4; i++)
            System.out.print(scores.get(i) + " ");
        System.out.println();

    }
/*
        System.out.println("SCORES: ");
        for (AbstractPlayer cur : players){
            System.out.print(cur.getScore() + " ");
        }
        System.out.println();
*/


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

        System.out.println("BOARD:");
        for (ArrayList<Integer> l : board){
            for (Integer x : l){
                System.out.print(x + " ");
            }
            System.out.println();
        }

    }

    static int getRowPoints(ArrayList<Integer> row) {
        int res = 0;
        for (Integer card : row){
            res += CARD_PENALTY[card];
        }
        return  res;
    }

    void updateScore(int playerIndex, int points){
        scores.set(playerIndex, scores.get(playerIndex) + points);
    }

}
