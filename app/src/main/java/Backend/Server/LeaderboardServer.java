package Backend.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;

import Backend.Database.DatabaseConnection;
import Backend.Messages.MessagesToClient.LeaderboardSentMessage;
import Backend.Messages.MessagesToServer.SendLeaderboardMessage;

import static Backend.Client.Client.LEADERBOARD_PORT_NUMBER;
import static Backend.Database.DatabaseConnection.*;

public class LeaderboardServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(LEADERBOARD_PORT_NUMBER);
        while (true){
            waitForConnections(serverSocket);
        }
    }

    private static synchronized void waitForConnections(ServerSocket serverSocket) throws IOException {
        System.out.print("WAITING...");
        ClientConnection connection = new ClientConnection(serverSocket.accept());
        System.out.println("CONNECTED");
        DatabaseConnection dbConnection = new DatabaseConnection(DB_URL_ADDRESS, DB_TABLE_NAME, DB_LOGIN, SECRET_PASSWORD);
        try{
            int leaderbordSize = SendLeaderboardMessage.receive(connection);
            LeaderboardSentMessage.submit(connection, dbConnection.requestLeaderboard(leaderbordSize));
        } catch (SQLException e) {
            //TODO
            e.printStackTrace();
        }
    }
}
