package Backend;

import android.util.Pair;

import java.io.IOException;
import java.io.PrintWriter;
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

            //while !!!
            while (!canDealOnceMore()){}

            for (int i = 0; i < playersNumber; i++) {
                connections.get(i).getClientOutput().println("Clear");
            }

        }
        proccessResults();
        for (int i = 0; i < playersNumber; i++) {
            connections.get(i).getClientOutput().println("Game over");
        }
    }

    private void proccessResults() throws Exception{
        if (gameType == GameTypes.MULTIPLAYER) {
            askRatingsFromDatabase();
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
            //this is the index(id) of real player
            int id = connections.indexOf(currentConnection);
            buildFinalResults(currentConnection, scores, id);
        }
    }

    private void buildFinalResults(ClientConnection currentConnection, ArrayList<Integer> scores, int id) throws IOException {
        currentConnection.getClientOutput().println("Results");
        ArrayList<Pair<String, Integer>> finalResults = new ArrayList<>();
        for (int i = 0; i < playersNumber; i++){
            String finalResult = "";
            if (i == id){
                finalResult += "YOU - ";
            } else {
                connections.get(i).getClientOutput().println("Id");
                finalResult += "Opponent #" + connections.get(i).getClientInput().readLine() + " - ";
            }
            int score = scores.get(i);
            finalResult += score;
            finalResults.add(new Pair<>(finalResult, score));
        }

        Collections.sort(finalResults, (p1, p2) -> p1.second - p2.second);
        for (Pair<String, Integer> s : finalResults){
            currentConnection.getClientOutput().println(s.first);
        }
    }

    private void askScores(ArrayList<Integer> scores) throws IOException {
        for (int i = 0; i < playersNumber; i++) {
            connections.get(i).getClientOutput().println("Score");
            scores.add(Integer.parseInt(connections.get(i).getClientInput().readLine()));
        }

    }

    //TODO: Refactor this method
    private void askRatingsFromDatabase() throws Exception {
        threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<GameResult>> tasksForGameResults = new ArrayList<>();
        System.out.println("CONNECTED TO DATABASE");

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

    private boolean canDealOnceMore() throws IOException {
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
        return isQueueEmpty;
    }

    private void dealCards() {
        ArrayList<Integer> deck = new ArrayList<>();
        for (int i = 1; i <= DECK_SIZE; i++){
            deck.add(i);
        }
        Collections.shuffle(deck);

        for (ClientConnection currentConnection : connections) {
            int i = connections.indexOf(currentConnection);
            PrintWriter currentOutput = currentConnection.getClientOutput();
            currentOutput.println("Cards");
            for (int j = i * ROUNDS; j < (i + 1) * ROUNDS; j++) {
                currentOutput.println(deck.get(j));
            }
            for (int j = ROUNDS * playersNumber; j < ROUNDS * playersNumber + ROWS; j++) {
                currentOutput.println(deck.get(j));
            }
            currentOutput.println(i);
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

        Collections.sort(moves, (o1, o2) -> o1.second - o2.second);
    }

    private void tellMovesSequence(ArrayList<Pair<Integer, Integer>> moves) throws InterruptedException {
        threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<Void>> tasksForMovesSequence = new ArrayList<>();
        for (final ClientConnection currentConnection : connections) {
            tasksForMovesSequence.add(() -> {
                currentConnection.getClientOutput().println("Moves");
                for (Pair<Integer, Integer> move : moves) {
                    currentConnection.getClientOutput().println(move.first);
                    currentConnection.getClientOutput().println(move.second);
                }
                return null;
            });
        }
        threadPool.invokeAll(tasksForMovesSequence);
    }

    private void tellAboutSmallestCardMove(ArrayList<Pair<Integer, Integer>> moves) throws InterruptedException, IOException {

        connections.get(0).getClientOutput().println("Min");
        int minOnBoard = Integer.parseInt(connections.get(0).getClientInput().readLine());
        int smallestCard = moves.get(0).second;

        threadPool = Executors.newFixedThreadPool(playersNumber);
        ArrayList<Callable<Void>> tasksForSmallestCardInfo = new ArrayList<>();
        if (minOnBoard > smallestCard) {
            tellAboutSmallestTake(moves, tasksForSmallestCardInfo);
        } else {
            tellAboutSmallestNotTake(tasksForSmallestCardInfo);
        }
        threadPool.invokeAll(tasksForSmallestCardInfo);
    }

    private void tellAboutSmallestTake(ArrayList<Pair<Integer, Integer>> moves,
                                       ArrayList<Callable<Void>> tasksForSmallestCardInfo) throws IOException {
        int playerIndexWithSmallestCard = moves.get(0).first;
        connections.get(playerIndexWithSmallestCard).getClientOutput().println("Choose");
        final int chosenRowIndex = Integer.parseInt(connections.get(playerIndexWithSmallestCard).getClientInput().readLine());

        for (final ClientConnection currentConnection : connections) {
            tasksForSmallestCardInfo.add(() -> {
                currentConnection.getClientOutput().println
                        ("Smallest\n"
                                + SmallestTakeTypes.SMALLEST_TAKE.toString() + "\n"
                                + chosenRowIndex);
                return null;
            });
        }
    }

    private void tellAboutSmallestNotTake(ArrayList<Callable<Void>> tasksForSmallestCardInfo) {
        for (final ClientConnection currentConnection : connections) {
            tasksForSmallestCardInfo.add(() -> {
                currentConnection.getClientOutput().println
                        ("Smallest\n"
                                + SmallestTakeTypes.SMALLEST_NOT_TAKE.toString() + "\n"
                                + -1);
                return null;
            });
        }
    }



}
