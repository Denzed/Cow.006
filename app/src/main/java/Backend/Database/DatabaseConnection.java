package Backend.Database;

import Backend.Server.ClientConnection;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DatabaseConnection {
/*
    public static final String SECRET_PASSWORD = "";
    private String URLAddress;
    private String tableName;
    private String login;
    private String password;

    public DatabaseConnection(String URLAddress, String tableName, String login, String password) {
        this.URLAddress = URLAddress;
        this.tableName = tableName;
        this.login = login;
        this.password = password;
    }

    public Connection connectToDatabase() throws SQLException {
        return DriverManager.getConnection(URLAddress, login, password);
    }

    public List<DatabaseRecord> requestLeaderBoard(ClientConnection connection) throws SQLException {
        Connection dbConnection = connectToDatabase();
        return buildLeaderBoard(dbConnection);
    }

    private List<DatabaseRecord> buildLeaderBoard(Connection dbConnection) throws SQLException {
        executeQuery(dbConnection,)
        String query = "SELECT username, rating FROM Information ORDER BY rating DESC LIMIT 100";
        Statement statement = dbConnection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()){
            String username = resultSet.getString("username");
            connection.getClientOutput().println(username);
            int rating = resultSet.getInt("rating");
            connection.getClientOutput().println(rating);
        }
        statement.close();
        dbConnection.close();

        return null;
    }

    public static List<DatabaseRecord> getPlayersInformation(int playersNumber, List<ClientConnection> connections) throws InterruptedException, ExecutionException {
        ExecutorService threadPool = Executors.newFixedThreadPool(playersNumber);
        List<Callable<DatabaseRecord>> tasksForDatabaseRecords = new ArrayList<>();
        for (final ClientConnection currentConnection : connections) {
            tasksForDatabaseRecords.add(() -> {
                currentConnection.getClientOutput().println("UserID");
                String userID = currentConnection.getClientInput().readLine();
                currentConnection.getClientOutput().println("Username");
                String username = currentConnection.getClientInput().readLine();
                currentConnection.getClientOutput().println("Score");
                int score = Integer.parseInt(currentConnection.getClientInput().readLine());
                int rating = getRating(userID, username);
                int played = getPlayed(userID);
                return new DatabaseRecord(userID, username, rating, played, score);
            });
        }
        List<DatabaseRecord> playersInformation = new ArrayList<>();
        for (Future <DatabaseRecord> taskforDatabaseRecord : threadPool.invokeAll(tasksForDatabaseRecords)) {
            playersInformation.add(taskforDatabaseRecord.get());
        }
        threadPool.shutdown();
        return playersInformation;
    }

    private static int getRating(String userID, String username) throws SQLException {
        Connection dataBaseConnection = DriverManager.getConnection(
                    "jdbc:mysql://sql11.freemysqlhosting.net/sql11157079", "sql11157079", SECRET_PASSWORD);
        String query = "INSERT IGNORE INTO Information (userID, username) "
                + "VALUES ('" + userID + "', '" + username + "');";
        System.out.println("query = " + query);
        final Statement statement = dataBaseConnection.createStatement();
        statement.execute(query);
        query = "SELECT rating FROM Information WHERE userID='" + userID + "';";
        System.out.println("query = " + query);

        ResultSet resultSet = statement.executeQuery(query);
        int rating = 0;
        while (resultSet.next()){
            rating = resultSet.getInt("rating");
        }
        statement.close();
        dataBaseConnection.close();
        return rating;
    }

    private static int getPlayed(String userID) throws SQLException {
        Connection dataBaseConnection = DriverManager.getConnection(
                "jdbc:mysql://sql11.freemysqlhosting.net/sql11157079", "sql11157079", SECRET_PASSWORD);
        final Statement statement = dataBaseConnection.createStatement();
        String query = "SELECT played FROM Information WHERE userID='" + userID + "';";
        System.out.println("query = " + query);

        ResultSet resultSet = statement.executeQuery(query);
        int played = 0;
        while (resultSet.next()) {
            played = resultSet.getInt("played");
        }
        statement.close();
        dataBaseConnection.close();
        return played;
    }
*/
}

