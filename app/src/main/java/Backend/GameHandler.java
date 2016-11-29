package Backend;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import static Backend.GameHandler.updateStateTypes.*;


public class GameHandler {
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
    private static final int DECK_SIZE = 104;
    private static final int ROUNDS = 10;
    static final int ROWS = 4;
    private static final int COLUMNS = 5;
    private static final int STOP_POINTS = 66;
    enum updateStateTypes { ADD_CARD, CLEAR_ROW }

    private int playersNumber;
    private ArrayList<ArrayList<Integer>> board;
    private ArrayList<AbstractPlayer> players;

    public GameHandler(Player owner, int botsNumber){
        this.playersNumber = 1 + botsNumber;
        players = new ArrayList<>();
        players.add(owner);
        for (int i = 1; i < playersNumber; i++){
            players.add(new Bot(this));
        }

        board = new ArrayList<>();
        for (int i = 0; i < ROWS; i++){
            board.add(new ArrayList<>());
        }
    }
    private void dealCards(){
        ArrayList<Integer> deck = new ArrayList<>();
        for (int i = 0; i < DECK_SIZE; i++){
            deck.add(i + 1);
        }
        Collections.shuffle(deck);

        for (int i = 0; i < playersNumber; i++){
            players.get(i).setHand(new ArrayList<>(deck.subList(i * ROUNDS, (i + 1) * ROUNDS)));
        }

        for (int i = 0; i < ROWS; i++){
            board.get(i).add(deck.get(ROUNDS * playersNumber + i));
        }

        //DEBUG
        System.out.println("HANDS:");

        for (AbstractPlayer ap : players){
            for (int x : ap.hand){
                System.out.print(x + " ");
            }
            System.out.println();
        }

        System.out.println("BOARD:");
        for (ArrayList<Integer> l : board){
            System.out.println(l.get(0));
        }

    }

    public void playGame() {

        boolean stop = false;
        while (!stop) {
            board.forEach(ArrayList::clear);

            dealCards();
            for (int i = 0; i < ROUNDS; i++) {
                playRound();
            }
            for (AbstractPlayer currentPlayer : players) {
                stop |= currentPlayer.getScore() >= STOP_POINTS;
            }
        }
    }

    private void playRound() {
        ArrayList<Pair<Integer, Integer>> moves = new ArrayList<>();
        for (int i = 0; i < playersNumber; i++){
            AbstractPlayer currentPlayer = players.get(i);
            moves.add(new Pair<>(i, currentPlayer.tellMove()));
        }
        Collections.sort(moves, Comparator.comparing(Pair::getValue));

        System.out.println("MOVES:");
        for (Pair p : moves) {
            System.out.println(p.getKey() + " " + p.getValue());
        }


        int minOnBoard = DECK_SIZE;
        for (ArrayList<Integer> cur : board){
            minOnBoard = Math.min(minOnBoard, cur.get(cur.size() - 1));
        }

        int smallestCard = moves.get(0).getValue();
        int smallestTook = 0;
        if (minOnBoard > smallestCard){
            System.out.println("Player #" + moves.get(0).getKey() + " needs to choose row to take, card = " + smallestCard);

            AbstractPlayer choosingPlayer = players.get(moves.get(0).getKey());
            int chosenRowIndex = choosingPlayer.tellChosenRow();
            ArrayList<Integer> chosenRow = board.get(chosenRowIndex);
            updateState(CLEAR_ROW, choosingPlayer, chosenRow, smallestCard);
            smallestTook = 1;
        }

        for (int i = smallestTook; i < playersNumber; i++){
            AbstractPlayer currentPlayer = players.get(moves.get(i).getKey());
            int currentCard = moves.get(i).getValue();
            System.out.println("Player #" + moves.get(i).getKey() + " turn, card = " + currentCard);

            ArrayList<Integer> updatingRow = getUpdatingRow(currentCard);
            if (updatingRow.size() == COLUMNS){
                updateState(CLEAR_ROW, currentPlayer, updatingRow, currentCard);
            }
            else{
                updateState(ADD_CARD, currentPlayer, updatingRow, currentCard);
            }
        }

        System.out.println("SCORES: ");
        for (AbstractPlayer cur : players){
            System.out.print(cur.getScore() + " ");
        }
        System.out.println();

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

    private void updateState(updateStateTypes type, AbstractPlayer p, ArrayList <Integer> row, int card) {
        if (type == CLEAR_ROW) {
            p.updateScore(getRowPoints(row));
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

    ArrayList<ArrayList<Integer>> getBoard() {
        return board;
    }
}
