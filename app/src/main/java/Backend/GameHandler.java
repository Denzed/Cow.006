package Backend;

import java.io.IOException;
import java.sql.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.*;
import Backend.Server.GameTypes;


import static Backend.AbstractPlayer.*;
import static Backend.GameResult.recalc;

public class GameHandler {
    public static String SECRET_PASSWORD = "";

    private int playersNumber;
    private ArrayList<ClientConnection> connections;
    private boolean stop = false;
    private GameTypes gameType;
    private ExecutorService threadPool;

    GameHandler(ArrayList<ClientConnection> connections, GameTypes gameType) {
        playersNumber = connections.size();
        this.connections = connections;
        this.gameType = gameType;
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
                    if (type.equals("Bot")) {
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
        if (gameType == GameTypes.MULTIPLAYER) {
            threadPool = Executors.newFixedThreadPool(playersNumber);
            ArrayList<Callable<GameResult>> tasksForGameResults = new ArrayList<>();
            System.out.println("CONNECTED TO DATABASE");

            for (final ClientConnection currentConnection : connections) {
                tasksForGameResults.add(new Callable<GameResult>() {
                    @Override
                    public GameResult call() throws Exception {
                        currentConnection.getClientOutput().println("UserID");
                        String userID = currentConnection.getClientInput().readLine();
                        currentConnection.getClientOutput().println("Username");
                        String username = currentConnection.getClientInput().readLine();
                        currentConnection.getClientOutput().println("Score");
                        int score = Integer.parseInt(currentConnection.getClientInput().readLine());
                        System.out.println(userID + " " + score);
                        final Connection dataBaseConnection = DriverManager.getConnection(
                                "jdbc:mysql://sql7.freemysqlhosting.net:3306/sql7150701", "sql7150701", SECRET_PASSWORD);
                        String query = "INSERT IGNORE INTO sql7150701.Information (userID, username) "
                        + "VALUES ('" + userID + "', '" + username + "');";
                        System.out.println("query = " + query);
                        final Statement statement = dataBaseConnection.createStatement();
                        statement.execute(query);
                        query = "SELECT rating, played FROM sql7150701.Information WHERE userID='" + userID + "';";
                        System.out.println("query = " + query);
                        ResultSet resultSet = statement.executeQuery(query);
                        int rating = 0;
                        int gamesPlayed = 0;
                        while (resultSet.next()){
                            rating = resultSet.getInt("rating");
                            gamesPlayed = resultSet.getInt("played");
                        }
                        System.out.println(userID + " " + score + " " + rating + " " + gamesPlayed);
                        statement.close();
                        dataBaseConnection.close();
                        return new GameResult(userID, username, score, rating, gamesPlayed);
                    }
                });
            }
            System.out.println("DONE");
            final ArrayList<GameResult> gameResults = new ArrayList<>();
            for (Future<GameResult> taskForGameResult : threadPool.invokeAll(tasksForGameResults)){
                try{
                    gameResults.add(taskForGameResult.get());
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
            ArrayList<Integer> oldRatings = new ArrayList<>();
            for (GameResult gameResult : gameResults){

                oldRatings.add(gameResult.getRating());
                System.out.println(gameResult.getUserID() + " " + gameResult.getRating() + " " + gameResult.getPoints() + gameResult.getGamesPlayed());
            }
            System.out.println();
            recalc(gameResults);


            for (GameResult gameResult : gameResults){
                final Connection dataBaseConnection = DriverManager.getConnection(
                        "jdbc:mysql://sql7.freemysqlhosting.net:3306/sql7150701", "sql7150701", SECRET_PASSWORD);
                final Statement statement = dataBaseConnection.createStatement();
                String query = "UPDATE Information SET rating='"
                        + gameResult.getRating()
                        + "', played='"
                        + gameResult.getGamesPlayed()
                        + "' WHERE userID='" + gameResult.getUserID() + "'";
                statement.execute(query);
                statement.close();
                dataBaseConnection.close();

                System.out.println(gameResult.getUserID() + " " + gameResult.getRating() + " " + gameResult.getPoints() + gameResult.getGamesPlayed());
            }
            System.out.println();

            for (int i = 0; i < playersNumber; i++){
                connections.get(i).getClientOutput().println("Results");
                for (int j = 0; j < playersNumber; j++){
                    String finalResult = "";
                    finalResult += gameResults.get(j).getUsername();
                    finalResult += "\t" + gameResults.get(j).getRating();
                    finalResult += "\t(";
                    if (gameResults.get(j).getDelta() > 0){
                        finalResult += "+";
                    }
                    finalResult += gameResults.get(j).getDelta();
                    finalResult += ")";
                    connections.get(i).getClientOutput().println(finalResult);
                }
            }
        }

        System.out.println("BEFORE GAME OVER");
        for (int i = 0; i < playersNumber; i++) {
            connections.get(i).getClientOutput().println("Game over");
        }
        System.out.println("AFTER GAME OVER");
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
        threadPool = Executors.newFixedThreadPool(playersNumber);
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
