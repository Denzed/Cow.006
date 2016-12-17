package Backend;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static Backend.AbstractPlayer.*;
import static java.lang.Boolean.TRUE;

class GameHandler {
    private int playersNumber;
    private List<ClientThread> connections;
    private boolean stop = false;

    GameHandler(List<ClientThread> connections) {
        playersNumber = connections.size();
        this.connections = connections;
    }

    void playGame() throws Exception {
        while (!stop) {
            dealCards();
            for (int i = 0; i < ROUNDS; i++) {
                playRound();
            }

            for (int i = 0; i < playersNumber; i++){
                ClientThread currentConnection = connections.get(i);
                currentConnection.clientOutput.println("Score");
                stop |= Integer.parseInt(currentConnection.clientInput.readLine()) >= STOP_POINTS;
            }

            boolean canDealOnceMore = false;
            while (!canDealOnceMore) {
                boolean isQueueEmpty = true;
                for (int i = 0; i < playersNumber; i++) {
                    ClientThread currentConnection = connections.get(i);
                    currentConnection.clientOutput.println("Type");
                    String type = currentConnection.clientInput.readLine();
                    System.out.println(type);
                    if (!type.equals("LocalPlayer")){
                        continue;
                    }
                    currentConnection.clientOutput.println("Queue");
                    isQueueEmpty &= Boolean.parseBoolean(currentConnection.clientInput.readLine());
                }
                canDealOnceMore = isQueueEmpty;
            }

            for (int i = 0; i < playersNumber; i++){
                connections.get(i).clientOutput.println("Clear");
            }

        }
        for (int i = 0; i < playersNumber; i++){
            connections.get(i).clientOutput.println("Game over");
        }
    }

    private void dealCards(){
        System.out.println("DEALING");
        ArrayList<Integer> deck = new ArrayList<>();
        for (int i = 0; i < DECK_SIZE; i++){
            deck.add(i + 1);
        }
        Collections.shuffle(deck);

        for (ClientThread currentConnection : connections){
            int i = connections.indexOf(currentConnection);
            currentConnection.clientOutput.println("Cards");
            for (int j = i * ROUNDS; j < (i + 1) * ROUNDS; j++){
                currentConnection.clientOutput.println(deck.get(j));
            }
            for (int j = ROUNDS * playersNumber; j < ROUNDS * playersNumber + ROWS; j++){
                currentConnection.clientOutput.println(deck.get(j));
            }
            currentConnection.clientOutput.println(i);}
    }

    private void playRound() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Future<Map.Entry<Integer, Integer>>> futureMoves = new ArrayList<>();
        for (final ClientThread currentConnection: connections) {
            futureMoves.add(es.submit(new Callable<Map.Entry<Integer, Integer>>() {
                @Override
                public Map.Entry<Integer, Integer> call() throws IOException{
                    currentConnection.clientOutput.println("Move");
                    int value = Integer.parseInt(currentConnection.clientInput.readLine());
                    return new AbstractMap.SimpleEntry<>(connections.indexOf(currentConnection), value);
                }
            }));
        }
        final ArrayList<Map.Entry<Integer, Integer>> moves = new ArrayList<>();
        for (Future<Map.Entry<Integer, Integer>> f : futureMoves){
            moves.add(f.get());
        }
        Collections.sort(moves, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                if (o1.getValue() > o2.getValue()) {
                    return 1;
                }
                if (o1.getValue() < o2.getValue()) {
                    return -1;
                }
                return 0;
            }
        });


        es = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Future<Boolean>> futureMovesSequence = new ArrayList<>();
        for (final ClientThread currentConnection : connections) {
            futureMovesSequence.add(es.submit(new Callable<Boolean>() {
                @Override
                public Boolean call(){
                currentConnection.clientOutput.println("Moves");
                    for (int j = 0; j < playersNumber; j++) {
                        currentConnection.clientOutput.println(moves.get(j).getKey() + "\n" + moves.get(j).getValue());
                    }
                    return TRUE;
                }
            }));
        }

        for (Future <Boolean> f : futureMovesSequence){
            f.get();
        }

        connections.get(0).clientOutput.println("Min");
        int minOnBoard = Integer.parseInt(connections.get(0).clientInput.readLine());
        int smallestCard = moves.get(0).getValue();

        es = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Future<Boolean>> futureSmallestCardInfo = new ArrayList<>();
        if (minOnBoard > smallestCard) {
            int playerIndexWithSmallestCard = moves.get(0).getKey();
            connections.get(playerIndexWithSmallestCard).clientOutput.println("Choose");
            final boolean smallestTook = true;
            final int chosenRowIndex = Integer.parseInt(connections.get(playerIndexWithSmallestCard).clientInput.readLine());
            for (final ClientThread currentConnection : connections) {
                futureSmallestCardInfo.add(es.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call(){
                        currentConnection.clientOutput.println("Smallest");
                        currentConnection.clientOutput.println(smallestTook);
                        currentConnection.clientOutput.println(chosenRowIndex);
                        return TRUE;
                    }
                }));
            }
        } else {
            for (final ClientThread currentConnection : connections) {
                futureSmallestCardInfo.add(es.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call(){
                        currentConnection.clientOutput.println("Smallest");
                        currentConnection.clientOutput.println(false);
                        currentConnection.clientOutput.println(-1);
                        return TRUE;
                    }
                }));
            }
        }

        for (Future <Boolean> f : futureSmallestCardInfo){
            f.get();
        }
    }
}
