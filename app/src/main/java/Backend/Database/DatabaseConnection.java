package Backend.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import Backend.Player.PlayerInformation;

import javax.xml.crypto.Data;

public class DatabaseConnection {

    //ask me: daniilplyushchenko@gmail.com
    public static final String DB_URL_ADDRESS = "";
    public static final String DB_TABLE_NAME = "";
    public static final String DB_LOGIN = "";
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

    private Connection connectToDatabase() throws SQLException {
        return DriverManager.getConnection(URLAddress, login, password);
    }

    public List<LeaderboardRecord> requestLeaderboard(int leaderbordSize) throws SQLException {
        Connection dbConnection = connectToDatabase();
        String query = "SELECT username, rating FROM " + tableName + " ORDER BY rating DESC LIMIT " + leaderbordSize;

        Statement statement = dbConnection.createStatement();
        List<LeaderboardRecord> result = buildLeaderboard(statement.executeQuery(query));
        statement.close();

        dbConnection.close();
        return result;
    }

    private List<LeaderboardRecord> buildLeaderboard(ResultSet resultSet) throws SQLException {
        List<LeaderboardRecord> result = new ArrayList<>();
        while (resultSet.next()) {
            String username = resultSet.getString("username");
            int rating = resultSet.getInt("rating");
            result.add(new LeaderboardRecord(username, rating));
        }
        return result;
    }

    public List<DatabaseRecord> requestDatabaseRecords(List<PlayerInformation> playerInformations)
            throws InterruptedException, ExecutionException, SQLException {
        List<DatabaseRecord> result = new ArrayList<>();
        Connection dbConnection = connectToDatabase();
        insertAbsentPlayersIntoDatabase(dbConnection, playerInformations);
        for (PlayerInformation playerInformation : playerInformations) {
            String query = "SELECT rating, played FROM " + tableName + " WHERE userID='" + playerInformation.getUserID() + "';";
            System.out.println(query);
            Statement statement = dbConnection.createStatement();
            result.add(buildDatabaseRecord(playerInformation, statement.executeQuery(query)));
            statement.close();
        }
        dbConnection.close();
        return result;
    }

    private DatabaseRecord buildDatabaseRecord(
            PlayerInformation playerInformation, ResultSet resultSet) throws SQLException {
        String username = playerInformation.getUsername();
        String userID = playerInformation.getUserID();
        int rating = 0;
        int played = 0;
        while (resultSet.next()) {
            rating = resultSet.getInt("rating");
            played = resultSet.getInt("played");
        }
        return new DatabaseRecord(userID, username, rating, played);
    }

    private void insertAbsentPlayersIntoDatabase(Connection dbConnection, List<PlayerInformation> playerInformations) throws SQLException {
        for (PlayerInformation playerInformation : playerInformations) {
            String query = "INSERT IGNORE INTO Information (userID, username) "
                    + "VALUES ('" + playerInformation.getUserID() + "', '" + playerInformation.getUsername() + "');";
            System.out.println(query);
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        }
    }

    void submitUpdatedRatingsToDatabase(List<DatabaseRecord> databaseRecords)
            throws SQLException {
        Connection dbConnection = connectToDatabase();
        for (DatabaseRecord databaseRecord : databaseRecords) {
            String query = "UPDATE " + tableName
                    + " SET rating='" + databaseRecord.getRating() + "', played='" + databaseRecord.getPlayed()
                    + "' WHERE userID='" + databaseRecord.getUserID() + "'";
            System.out.println(query);
            Statement statement = dbConnection.createStatement();
            statement.execute(query);
            statement.close();
        }
        dbConnection.close();
    }

}
