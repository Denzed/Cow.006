package Backend.Database;

import Backend.Player.PlayerInformation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DatabaseConnection {

    public static final String DB_URL_ADDRESS = "jdbc:mysql://sql11.freemysqlhosting.net/sql11157079";
    public static final String DB_TABLE_NAME = "Information";
    public static final String DB_LOGIN = "sql11157079";
    public static final String SECRET_PASSWORD = ""; //ask me: daniilplyushchenko@gmail.com

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

    private Connection connectToDatabase() throws SQLException {
        return DriverManager.getConnection(URLAddress, login, password);
    }

    private ResultSet executeQuery(Connection dbConnection, String query) throws SQLException {
        Statement statement = dbConnection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        statement.close();
        return resultSet;
    }

    public List<LeaderBoardRecord> requestLeaderBoard(int leaderbordSize) throws SQLException {
        Connection dbConnection = connectToDatabase();
        String query = "SELECT username, rating FROM " + tableName + " ORDER BY rating DESC LIMIT " + leaderbordSize;
        ResultSet resultSet = executeQuery(dbConnection, query);
        dbConnection.close();
        return buildLeaderBoard(resultSet);
    }

    private List<LeaderBoardRecord> buildLeaderBoard(ResultSet resultSet) throws SQLException {
        List<LeaderBoardRecord> result = new ArrayList<>();
        while (resultSet.next()){
            String username = resultSet.getString("username");
            int rating = resultSet.getInt("rating");
            result.add(new LeaderBoardRecord(username, rating));
        }
        return result;
    }

    public List<DatabaseRecord> requestDatabaseRecords(List<PlayerInformation> playersInformations)
            throws InterruptedException, ExecutionException, SQLException {
        List<DatabaseRecord> result = new ArrayList<>();
        Connection dbConnection = connectToDatabase();
        insertAbsentPlayersIntoDatabase(dbConnection, playersInformations);
        for (PlayerInformation playerInformation : playersInformations){
            String query = "SELECT rating, played FROM" + tableName + " WHERE userID='" + playerInformation.getUserID() + "';";
            buildDatabaseRecord(playersInformations, executeQuery(dbConnection, query));
        }
        dbConnection.close();
        return result;
    }

    private List<DatabaseRecord> buildDatabaseRecord(
            List<PlayerInformation> playersInformations, ResultSet resultSet) throws SQLException {
        List<DatabaseRecord> results = new ArrayList<>();
        for (PlayerInformation playerInformation : playersInformations){
            String username = playerInformation.getUsername();
            String userID = playerInformation.getUserID();
            int rating = resultSet.getInt("rating");
            int played = resultSet.getInt("played");
            results.add(new DatabaseRecord(userID, username, rating, played));
        }
        return results;
    }

    private void insertAbsentPlayersIntoDatabase(Connection dbConnection, List<PlayerInformation> playersInformations) throws SQLException {
        for (PlayerInformation playerInformation : playersInformations){
            String query = "INSERT IGNORE INTO Information (userID, username) "
                    + "VALUES ('" + playerInformation.getUserID() + "', '" + playerInformation.getUsername() + "');";
            executeQuery(dbConnection, query);
        }
    }

}

