package Backend;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static Backend.AbstractPlayer.*;

class GameHandler {
    private int playersNumber;
    private ArrayList<ClientConnection> connections;
    private boolean stop = false;

    GameHandler(ArrayList<ClientConnection> connections) {
        playersNumber = connections.size();
        this.connections = connections;
    }

    void playGame() throws Exception {
        while (!stop) {
            dealCards();
            for (int i = 0; i < ROUNDS; i++) {
                playRound();
            }

            for (int i = 0; i < playersNumber; i++) {
                ClientConnection currentConnection = connections.get(i);
                currentConnection.getClientOutput().println("Score");
                stop |= Integer.parseInt(currentConnection.getClientInput().readLine()) >= STOP_POINTS;
            }

            boolean canDealOnceMore = false;
            while (!canDealOnceMore) {
                boolean isQueueEmpty = true;
                for (int i = 0; i < playersNumber; i++) {
                    ClientConnection currentConnection = connections.get(i);
                    currentConnection.getClientOutput().println("Type");
                    String type = currentConnection.getClientInput().readLine();
                    System.out.println(type);
                    if (!type.equals("LocalPlayer")) {
                        continue;
                    }
                    currentConnection.getClientOutput().println("Queue");
                    isQueueEmpty &= Boolean.parseBoolean(currentConnection.getClientInput().readLine());
                }
                canDealOnceMore = isQueueEmpty;
            }

            for (int i = 0; i < playersNumber; i++) {
                connections.get(i).getClientOutput().println("Clear");
            }

        }

        for (int i = 0; i < playersNumber; i++) {
            connections.get(i).getClientOutput().println("Game over");
        }
    }

    private void dealCards() {
        System.out.println("DEALING");
        ArrayList<Integer> deck = new ArrayList<>();
        for (int i = 0; i < DECK_SIZE; i++){
            deck.add(i + 1);
        }
        Collections.shuffle(deck);

        for (ClientConnection currentConnection : connections) {
            int i = connections.indexOf(currentConnection);
            currentConnection.getClientOutput().println("Cards");
            for (int j = i * ROUNDS; j < (i + 1) * ROUNDS; j++) {
                currentConnection.getClientOutput().println(deck.get(j));
            }
            for (int j = ROUNDS * playersNumber; j < ROUNDS * playersNumber + ROWS; j++) {
                currentConnection.getClientOutput().println(deck.get(j));
            }
            currentConnection.getClientOutput().println(i);}
    }

    private void playRound() throws IOException, InterruptedException, ExecutionException {
        ExecutorService threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<Map.Entry<Integer, Integer>>> tasksForMoves = new ArrayList<>();
        for (final ClientConnection currentConnection: connections) {
            tasksForMoves.add(new Callable<Map.Entry<Integer, Integer>>() {
                @Override
                public Map.Entry<Integer, Integer> call() throws IOException{
                    currentConnection.getClientOutput().println("Move");
                    int value = Integer.parseInt(currentConnection.getClientInput().readLine());
                    return new AbstractMap.SimpleEntry<>(connections.indexOf(currentConnection), value);
                }
            });
        }
        final ArrayList<Map.Entry<Integer, Integer>> moves = new ArrayList<>();
        for (Future<Map.Entry<Integer, Integer>> taskForMove : threadPool.invokeAll(tasksForMoves)){
            moves.add(taskForMove.get());
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


        threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<Void>> tasksForMovesSequence = new ArrayList<>();
        for (final ClientConnection currentConnection : connections) {
            tasksForMovesSequence.add(new Callable<Void>() {
                @Override
                public Void call(){
                    currentConnection.getClientOutput().println("Moves");
                    for (Map.Entry<Integer, Integer> move : moves) {
                        currentConnection.getClientOutput().println(move.getKey());
                        currentConnection.getClientOutput().println(move.getValue());
                    }
                    return null;
                }
            });
        }
        threadPool.invokeAll(tasksForMovesSequence);

        connections.get(0).getClientOutput().println("Min");
        int minOnBoard = Integer.parseInt(connections.get(0).getClientInput().readLine());
        int smallestCard = moves.get(0).getValue();

        threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<Void>> tasksForSmallestCardInfo = new ArrayList<>();
        if (minOnBoard > smallestCard) {
            int playerIndexWithSmallestCard = moves.get(0).getKey();
            connections.get(playerIndexWithSmallestCard).getClientOutput().println("Choose");
            final boolean smallestTook = true;
            final int chosenRowIndex = Integer.parseInt(connections.get(playerIndexWithSmallestCard).getClientInput().readLine());
            for (final ClientConnection currentConnection : connections) {
                tasksForSmallestCardInfo.add(new Callable<Void>() {
                    @Override
                    public Void call(){
                        currentConnection.getClientOutput().println("Smallest");
                        currentConnection.getClientOutput().println(smallestTook);
                        currentConnection.getClientOutput().println(chosenRowIndex);
                        return null;
                    }
                });
            }
        } else {
            for (final ClientConnection currentConnection : connections) {
                tasksForSmallestCardInfo.add(new Callable<Void>() {
                    @Override
                    public Void call(){
                        currentConnection.getClientOutput().println("Smallest");
                        currentConnection.getClientOutput().println(false);
                        currentConnection.getClientOutput().println(-1);
                        return null;
                    }
                });
            }
        }
        threadPool.invokeAll(tasksForSmallestCardInfo);
    }

}
