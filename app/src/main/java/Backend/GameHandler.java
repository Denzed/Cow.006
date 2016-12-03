package Backend;

import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static Backend.AbstractPlayer.*;

public class GameHandler {
/*    private static final int CARD_PENALTY[] = {0,
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
*/
    private int playersNumber;
    private ArrayList<ArrayList<Integer>> board;
    private ArrayList<AbstractPlayer> players;
    List<ClientThread> connections;

    static final int PORT_NUMBER = 2222;

    public GameHandler(List<ClientThread> connections) {

        playersNumber = connections.size();
        this.connections = connections;
        for (int i = 0; i < playersNumber; i++) {
            System.out.println(connections.get(i));
        }
        System.out.println("HANDLER CREATED");
    }

    public void playGame() throws IOException {

        dealCards();
        for (int i = 0; i < ROUNDS; i++){
            playRound();
            System.out.println("ROUND PLAYED");
        }
        System.out.println("???");
        for (int i = 0; i < playersNumber; i++){
            System.out.println("GAME OVER");
            synchronized (connections.get(i)) {
                connections.get(i).clientOutput.println("Game over");
            }
        }
        /*        boolean stop = false;
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
*/    }

    private void dealCards(){
        ArrayList<Integer> deck = new ArrayList<>();
        for (int i = 0; i < DECK_SIZE; i++){
            deck.add(i + 1);
        }
        Collections.shuffle(deck);

        for (int i = 0; i < playersNumber; i++){
            synchronized (connections.get(i)){
                connections.get(i).clientOutput.println("Cards");
                for (int j = i * ROUNDS; j < (i + 1) * ROUNDS; j++){
                    connections.get(i).clientOutput.println(deck.get(j));
                }
                for (int j = ROUNDS * playersNumber; j < ROUNDS * playersNumber + ROWS; j++){
                    connections.get(i).clientOutput.println(deck.get(j));
                }
            }
        }

/*        for (int j = ROUNDS * playersNumber; j < ROUNDS * playersNumber + ROWS; j++){
            board.add(new ArrayList<>(Collections.singletonList(deck.get(j))));
*/
    }

    private synchronized void playRound() throws IOException {
        ArrayList<Pair<Integer, Integer>> moves = new ArrayList<>();
        for (int i = 0; i < playersNumber; i++) {
            synchronized (connections.get(i)) {
                connections.get(i).clientOutput.println("Move");
                int value = Integer.parseInt(connections.get(i).clientInput.readLine());
                moves.add(new Pair<>(i, value));
            }
        }
        Collections.sort(moves, Comparator.comparing(Pair::getValue));

        System.out.println("MOVES:");
        for (Pair p : moves) {
            System.out.println(p.getKey() + " " + p.getValue());
        }

        connections.get(0).clientOutput.println("Min");
        int minOnBoard = Integer.parseInt(connections.get(0).clientInput.readLine());


        int smallestCard = moves.get(0).getValue();
        boolean smallestTook = false;
        int chosenRowIndex = -1;
        if (minOnBoard > smallestCard) {
            System.out.println("Player #" + moves.get(0).getKey() + " needs to choose row to take, card = " + smallestCard);
            connections.get(moves.get(0).getKey()).clientOutput.println("Choose");
            chosenRowIndex = Integer.parseInt(connections.get(moves.get(0).getKey()).clientInput.readLine());
            System.out.println("INDEX = " + chosenRowIndex);
//            ArrayList<Integer> chosenRow = board.get(chosenRowIndex);
//            updateState(CLEAR_ROW, choosingPlayer, chosenRow, smallestCard);
            smallestTook = true;
//            while (true){}
        }
        for (int i = 0; i < playersNumber; i++) {
            synchronized (connections.get(i)) {
                connections.get(i).clientOutput.println("Moves");
                connections.get(i).clientOutput.println(smallestTook);
                connections.get(i).clientOutput.println(chosenRowIndex);
            }
        }
        for (int i = 0; i < playersNumber; i++) {
            synchronized (connections.get(i)) {
                for (int j = 0; j < playersNumber; j++) {
                    connections.get(i).clientOutput.println(moves.get(j).getKey());
                    connections.get(i).clientOutput.println(moves.get(j).getValue());
                }
            }
        }
    }
}
