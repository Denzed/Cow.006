package Backend;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.*;

import static Backend.Database.Database.tellLeaderBoard;
import static Backend.GameConstants.*;

public class Server {

    private static ServerSocket serverSocket = null;
    private static ArrayList<ArrayList<ClientConnection>> connections =
            new ArrayList<>(Collections.nCopies(DECK_SIZE / ROUNDS + 1, new ArrayList<ClientConnection>()));
    private static final int PORT_NUMBER = 8080;

    public static void main(String[] Args) throws IOException {
        serverSocket = new ServerSocket(PORT_NUMBER);

        //I think it's OK to have infinite loop here because it's a server
        while (true) {
            waitForConnections();
        }
    }

    private static synchronized void waitForConnections() throws IOException {
        Socket clientSocket = serverSocket.accept();
        ClientConnection connection = new ClientConnection(clientSocket);
        Client.ConnectionTypes connectionType = connection.getConnectionType();

        if (connectionType == Client.ConnectionTypes.LEADERBOARD) {
            try {
                tellLeaderBoard(connection);
            } catch (SQLException e) {
                //ignore
            }
            return;
        }

        int playersNumber = connection.getPlayersNumber();
        connections.get(playersNumber).add(connection);
        if (connections.get(playersNumber).size() >= playersNumber) {
            ArrayList<ClientConnection> candidates = connections.get(playersNumber);
            if (haveEnoughConnectedPlayers(candidates, playersNumber)){
                final ArrayList<ClientConnection> players = new ArrayList<>();
                buildPlayersList(candidates, playersNumber, players);
                startNewGame(players);
            }
        }
    }

    private static void startNewGame(ArrayList<ClientConnection> players) {
        new Thread(() -> {
            try {
                new GameHandler(players, Client.ConnectionTypes.SINGLEPLAYER).playGame();
            } catch (Exception e) {
                System.out.println("EXCEPTION!!!");
                e.printStackTrace();
                for (ClientConnection currentConnection : players) {
                    try {
                        currentConnection.getClientOutput().println("Disconnected");
                    } catch (Exception e2) {
                        //just ignore
                    }
                }
            }
        }).start();
    }

    private static boolean haveEnoughConnectedPlayers(ArrayList<ClientConnection> candidates, int playersNumber) {
        for (int i = 0; i < playersNumber; i++) {
            try {
                candidates.get(i).getClientOutput().println("IsConnected");
                candidates.get(i).getClientInput().readLine();
            } catch (IOException e) {
                candidates.remove(i);
                return false;
            }
        }
        return true;
    }

    private static void buildPlayersList(ArrayList<ClientConnection> candidates, int playersNumber, ArrayList<ClientConnection> players) {
        while (players.size() < playersNumber) {
            players.add(candidates.get(0));
            candidates.remove(0);
        }
    }

}

