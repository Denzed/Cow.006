package Backend;

import java.io.IOException;
import java.util.*;

import static Backend.AbstractPlayer.*;

class GameHandler {
    private int playersNumber;
    private List<ClientThread> connections;

    GameHandler(List<ClientThread> connections) {

        playersNumber = connections.size();
        this.connections = connections;
        for (int i = 0; i < playersNumber; i++) {
            ////System.out.println(connections.get(i));
        }
        ////System.out.println("HANDLER CREATED");
    }

    void playGame() throws IOException {

        dealCards();
        boolean stop = false;
        while (!stop) {
            dealCards();
            for (int i = 0; i < ROUNDS; i++) {
                playRound();
            }
            for (int i = 0; i < playersNumber; i++){
                synchronized (connections.get(i)){
                    ClientThread currentConnection = connections.get(i);
                    currentConnection.clientOutput.println("Score");
                    //System.out.println("Score");
                    stop |= Integer.parseInt(currentConnection.clientInput.readLine()) >= STOP_POINTS;
                }
            }
        }
        ////System.out.println("???");
        for (int i = 0; i < playersNumber; i++){
            ////System.out.println("GAME OVER");
            synchronized (connections.get(i)) {
                //System.out.println("Game over");
                connections.get(i).clientOutput.println("Game over");
            }
        }
    }

    private void dealCards(){
        ArrayList<Integer> deck = new ArrayList<>();
        for (int i = 0; i < DECK_SIZE; i++){
            deck.add(i + 1);
        }
        Collections.shuffle(deck);

        for (int i = 0; i < playersNumber; i++){
            synchronized (connections.get(i)) {
                ClientThread currentConnection = connections.get(i);
                //System.out.println("Cards");
                currentConnection.clientOutput.println("Cards");
                for (int j = i * ROUNDS; j < (i + 1) * ROUNDS; j++){
                    currentConnection.clientOutput.println(deck.get(j));
                }
                for (int j = ROUNDS * playersNumber; j < ROUNDS * playersNumber + ROWS; j++){
                    currentConnection.clientOutput.println(deck.get(j));
                }
                currentConnection.clientOutput.println(i);
            }
        }
    }

    private synchronized void playRound() throws IOException {
        ArrayList<Map.Entry<Integer, Integer>> moves = new ArrayList<>();
        for (int i = 0; i < playersNumber; i++) {
            synchronized (connections.get(i)) {
                ClientThread currentConnection = connections.get(i);
                //System.out.println("Move");
                currentConnection.clientOutput.println("Move");
                int value = Integer.parseInt(currentConnection.clientInput.readLine());
                moves.add(new AbstractMap.SimpleEntry<>(i, value));
            }
        }
        Collections.sort(moves, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                if (o1.getValue() > o2.getValue()){
                    return 1;
                }
                if (o1.getValue() < o2.getValue()){
                    return -1;
                }
                return 0;
            }
        });

        //System.out.println("Min");
        connections.get(0).clientOutput.println("Min");
        int minOnBoard = Integer.parseInt(connections.get(0).clientInput.readLine());
        int smallestCard = moves.get(0).getValue();
        boolean smallestTook = false;
        int chosenRowIndex = -1;
        if (minOnBoard > smallestCard) {
            int playerIndexWithSmallestCard = moves.get(0).getKey();
            //System.out.println("Choose");
            connections.get(playerIndexWithSmallestCard).clientOutput.println("Choose");
            chosenRowIndex = Integer.parseInt(connections.get(playerIndexWithSmallestCard).clientInput.readLine());
            smallestTook = true;
        }

        for (int i = 0; i < playersNumber; i++) {
            synchronized (connections.get(i)) {
                ClientThread currentConnection = connections.get(i);
                //System.out.println("Moves\n" + smallestTook + "\n" + chosenRowIndex);
                currentConnection.clientOutput.println("Moves\n" + smallestTook + "\n" + chosenRowIndex);
            }
        }
        for (int i = 0; i < playersNumber; i++) {
            synchronized (connections.get(i)) {
                ClientThread currentConnection = connections.get(i);
                for (int j = 0; j < playersNumber; j++) {
                    //System.out.println(moves.get(j).getKey() + "\n" + moves.get(j).getValue());
                    currentConnection.clientOutput.println(moves.get(j).getKey() + "\n" + moves.get(j).getValue());
                }
            }
        }
    }
}
