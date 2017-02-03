package Backend;

import android.util.Pair;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;

import Backend.Server.GameTypes;

import static Backend.GameConstants.*;
import static Backend.GameResult.recalc;

public class GameHandler {
    public static String SECRET_PASSWORD = "";

    private int playersNumber;
    private ArrayList<ClientConnection> connections;
    private GameTypes gameType;
    private ExecutorService threadPool;

    GameHandler(ArrayList<ClientConnection> connections, GameTypes gameType) {
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
        proccessResults();
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

    private void proccessResults() throws SQLException, InterruptedException, IOException, ExecutionException {
        if (gameType == GameTypes.MULTIPLAYER) {
            getRatingsFromDatabase();
        } else {
            ArrayList<Integer> scores = new ArrayList<>();
            askScores(scores);
            tellFinalResults(scores);
        }

    }

    private void tellFinalResults(ArrayList<Integer> scores) throws IOException {
        for (ClientConnection currentConnection : connections){
            //skip all the bots cuz bots don't have a screen to show results
            currentConnection.getClientOutput().println("Type");
            String type = currentConnection.getClientInput().readLine();
            if (type.equals("Bot")){
                continue;
            }

            ArrayList<Pair<String, Integer>> finalResults
                    = buildFinalResults(currentConnection, scores, connections.indexOf(currentConnection));
            for (Pair<String, Integer> s : finalResults){
                currentConnection.getClientOutput().println(s.first);
            }

        }
    }

    private ArrayList<Pair<String, Integer>> buildFinalResults(ClientConnection connection,
                                                               ArrayList<Integer> scores, int id) throws IOException {
        connection.getClientOutput().println("Results");
        ArrayList<Pair<String, Integer>> finalResults = new ArrayList<>();
        for (int i = 0; i < playersNumber; i++){
            ClientConnection currentConnection = connections.get(i);
            String finalResult = "";
            if (i == id){
                finalResult += "YOU - ";
            } else {
                currentConnection.getClientOutput().println("Id");
                finalResult += "Opponent #" + connections.get(i).getClientInput().readLine() + " - ";
            }
            int score = scores.get(i);
            finalResult += score;
            finalResults.add(new Pair<>(finalResult, score));
        }
        Collections.sort(finalResults, (p1, p2) -> p1.second - p2.second);
        return finalResults;
    }

    private void askScores(ArrayList<Integer> scores) throws IOException {
        for (int i = 0; i < playersNumber; i++) {
            ClientConnection currentConnection = connections.get(i);
            currentConnection.getClientOutput().println("Score");
            scores.add(Integer.parseInt(currentConnection.getClientInput().readLine()));
        }
    }

    //TODO: Refactor this method
    private void getRatingsFromDatabase() throws InterruptedException, SQLException, ExecutionException {
        ArrayList<Callable<GameResult>> tasksForGameResults = new ArrayList<>();
        buildGameResults(tasksForGameResults);
        final ArrayList<GameResult> gameResults = new ArrayList<>();
        for (Future<GameResult> taskForGameResult : threadPool.invokeAll(tasksForGameResults)){
            gameResults.add(taskForGameResult.get());
        }
        threadPool.shutdown();

        ArrayList<Integer> oldRatings = new ArrayList<>();
        for (GameResult gameResult : gameResults){

            oldRatings.add(gameResult.getRating());
            System.out.println(gameResult.getUserID() + " " + gameResult.getRating() + " " + gameResult.getPoints() + gameResult.getGamesPlayed());
        }
        System.out.println();
        recalc(gameResults);

        updateRatings(gameResults);
        tellFinalResultMultiplayer(gameResults);
    }

    private void tellFinalResultMultiplayer(ArrayList<GameResult> gameResults) {
        for (int i = 0; i < playersNumber; i++){
            connections.get(i).getClientOutput().println("Results");
            for (int j = 0; j < playersNumber; j++){
                String finalResult = "";
                finalResult += gameResults.get(j).getUsername();
                finalResult += "\t" + gameResults.get(j).getPoints();
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

    private void updateRatings(ArrayList<GameResult> gameResults) throws SQLException {
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


    }

    private void buildGameResults(ArrayList<Callable<GameResult>> tasksForGameResults) {
        threadPool = Executors.newFixedThreadPool(playersNumber);
        for (final ClientConnection currentConnection : connections) {
            tasksForGameResults.add(() -> {
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
            });
        }
        System.out.println("DONE");
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

}
