package Backend;

import android.util.Pair;
import Backend.Database.DatabaseRecord;
//import javafx.util.Pair;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;

import static Backend.Database.Database.getPlayersInformation;
import static Backend.GameConstants.*;
import static Backend.Rating.updateRatings;

class GameHandler {

    private final Client.ConnectionTypes gameType;
    private final int playersNumber;
    private final ArrayList<ClientConnection> connections;
    private ExecutorService threadPool;

    GameHandler(ArrayList<ClientConnection> connections, Client.ConnectionTypes gameType) {
        playersNumber = connections.size();
        this.connections = connections;
        this.gameType = gameType;
    }

    void playGame() throws InterruptedException, ExecutionException, IOException, SQLException {
        tellIds();
        while (!hasSomeoneBusted()) {
            dealCards();
            for (int i = 0; i < ROUNDS; i++) {
                playRound();
            }
            drawAllCardsBeforeNewDeal();
            tellAllClients("Clear");
        }
        processResults();
        tellAllClients("Game over");
    }


    private void tellIds() throws InterruptedException {
        threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<Void>> tasksForId = new ArrayList<>();
        for (final ClientConnection currentConnection : connections){
            tasksForId.add(() -> {
                currentConnection.getClientOutput().println("GameID");
                currentConnection.getClientOutput().println(connections.indexOf(currentConnection));
                return null;
            });
        }
        threadPool.invokeAll(tasksForId);
        threadPool.shutdown();
    }


    private boolean hasSomeoneBusted() throws InterruptedException, ExecutionException {
        threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<Boolean>> tasksForScore = new ArrayList<>();

        for (final ClientConnection currentConnection : connections){
            tasksForScore.add(() -> {
                currentConnection.getClientOutput().println("Score");
                return Integer.parseInt(currentConnection.getClientInput().readLine()) >= STOP_POINTS;
            });
        }

        boolean result = false;
        for (Future<Boolean> taskForScore : threadPool.invokeAll(tasksForScore)){
            result |= taskForScore.get();
        }
        threadPool.shutdown();
        return result;
    }


    private void dealCards() {
        ArrayList<Integer> deck = new ArrayList<>();
        for (int i = 1; i <= DECK_SIZE; i++){
            deck.add(i);
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
        }
    }


    private void playRound() throws IOException, InterruptedException, ExecutionException {
        final ArrayList<Pair<Integer, Integer>> moves = new ArrayList<>();
        buildMovesSequence(moves);
        tellMovesSequence(moves);
        tellAboutSmallestCardMove(moves);
    }

    private void buildMovesSequence(ArrayList<Pair<Integer, Integer>> moves) throws InterruptedException, ExecutionException {
        threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<Pair<Integer, Integer>>> tasksForMoves = new ArrayList<>();

        for (final ClientConnection currentConnection: connections) {
            tasksForMoves.add(() -> {
                currentConnection.getClientOutput().println("Move");
                int value = Integer.parseInt(currentConnection.getClientInput().readLine());
                return new Pair<>(connections.indexOf(currentConnection), value);
            });
        }

        for (Future<Pair<Integer, Integer>> taskForMove : threadPool.invokeAll(tasksForMoves)){
            moves.add(taskForMove.get());
        }
        threadPool.shutdown();

        Collections.sort(moves, (o1, o2) -> o1.second - o2.second);
    }

    private void tellMovesSequence(ArrayList<Pair<Integer, Integer>> moves) throws InterruptedException {
        threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<Void>> tasksForMovesSequence = new ArrayList<>();
        for (final ClientConnection currentConnection : connections) {
            tasksForMovesSequence.add(() -> {
                currentConnection.getClientOutput().println("Moves");
                for (Pair<Integer, Integer> move : moves) {
                    currentConnection.getClientOutput().println(move.first + "\n" + move.second);
                }
                return null;
            });
        }
        threadPool.invokeAll(tasksForMovesSequence);
        threadPool.shutdown();
    }

    private void tellAboutSmallestCardMove(ArrayList<Pair<Integer, Integer>> moves) throws InterruptedException, IOException {
//      need to ask any player
//      that's why I'm asking "connections.get(0)"
        connections.get(0).getClientOutput().println("Min");
        int minOnBoard = Integer.parseInt(connections.get(0).getClientInput().readLine());
        int smallestCard = moves.get(0).second;
        if (minOnBoard > smallestCard) {
            int playerIndexWithSmallestCard = moves.get(0).first;
            connections.get(playerIndexWithSmallestCard).getClientOutput().println("Choose");
            final int chosenRowIndex = Integer.parseInt(connections.get(playerIndexWithSmallestCard).getClientInput().readLine());
            tellAllClients("Smallest\n" + SmallestTakeTypes.SMALLEST_TAKE.toString() + "\n" + chosenRowIndex);
        } else {
            tellAllClients("Smallest\n" + SmallestTakeTypes.SMALLEST_NOT_TAKE.toString() + "\n" + -1);
        }
    }

    //sorry for such a long name; this method is for drawing all the cards before new deal
    //(I mean, player is getting new cards before he see how the last round was played)
    private void drawAllCardsBeforeNewDeal() throws IOException, InterruptedException {
        threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<Void>> tasks = new ArrayList<>();

        for (final ClientConnection currentConnection : connections){
            tasks.add(() -> {
                currentConnection.getClientOutput().println("Type");
                String type = currentConnection.getClientInput().readLine();
                if (type.equals("Bot")) {
                    return null;
                }

                //just check if all the cards are drawn every second
                boolean isQueueEmpty = false;
                while (!isQueueEmpty) {
                    currentConnection.getClientOutput().println("Queue");
                    isQueueEmpty = Boolean.parseBoolean(currentConnection.getClientInput().readLine());
                    TimeUnit.SECONDS.sleep(1);
                }
                return null;
            });
        }
        threadPool.invokeAll(tasks);
        threadPool.shutdown();
    }


    private void tellAllClients(String message) throws InterruptedException {
        threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<Void>> tasksForClear = new ArrayList<>();

        for (final ClientConnection currentConnection : connections){
            tasksForClear.add(() -> {
                currentConnection.getClientOutput().println(message);
                return null;
            });
        }
        threadPool.invokeAll(tasksForClear);
        threadPool.shutdown();
    }


    private void processResults() throws SQLException, InterruptedException, IOException, ExecutionException {
        if (gameType == Client.ConnectionTypes.SINGLEPLAYER){
            for (ClientConnection currentConnection : connections){
                currentConnection.getClientOutput().println("Type");
                String type = currentConnection.getClientInput().readLine();
                if (type.equals("Bot")){
                    continue;
                }
                currentConnection.getClientOutput().println("Results");
            }
        } else {
            ArrayList<DatabaseRecord> playersInformation = getPlayersInformation(playersNumber, connections);
            updateRatings(playersInformation);
            tellAllClients("Results");
            for (DatabaseRecord playerInformation : playersInformation) {
                tellAllClients(playerInformation.getUsername());
                tellAllClients(String.valueOf(playerInformation.getRating()));
                int ratingChange = playerInformation.getRatingChange();
                tellAllClients("(" + (ratingChange > 0 ? '+' : "") + ratingChange + ")");
            }
        }
    }

}
