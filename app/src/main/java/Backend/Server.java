package Backend;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

import static Backend.AbstractPlayer.DECK_SIZE;
import static Backend.AbstractPlayer.ROUNDS;

public class Server {

    private static ServerSocket serverSocket = null;
    private static ArrayList<ArrayList<ClientConnection>> connections =
            new ArrayList<>(Collections.nCopies(DECK_SIZE / ROUNDS + 1, new ArrayList<ClientConnection>()));
    private static final int PORT_NUMBER = 8080;
    enum GameTypes { SINGLEPLAYER, MULTIPLAYER};

    public static void main(String[] Args) throws IOException {
        serverSocket = new ServerSocket(PORT_NUMBER);

        //I think it's OK to have infinite loop here because it's a server
        while (true) {
            System.out.println("BEFORE WAIT");
            waitForConnections();
            System.out.println("AFTER WAIT");
        }
    }

    private static synchronized void waitForConnections() throws IOException {
        System.out.print("TRYING...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("CONNECTED");

        ClientConnection connection = new ClientConnection(clientSocket);
        final int playersNumber = connection.getPlayersNumber();
        connections.get(playersNumber).add(connection);
        if (connections.get(playersNumber).size() >= playersNumber) {
            boolean haveEnoughConnectedPlayers = true;
            ArrayList<ClientConnection> candidates = connections.get(playersNumber);

            for (int i = 0; i < playersNumber; i++) {
                try {
                    candidates.get(i).getClientOutput().println("IsConnected");
                    candidates.get(i).getClientInput().readLine();
                } catch (IOException e) {
                    candidates.remove(i);
                    haveEnoughConnectedPlayers = false;
                    break;
                }
            }
            if (!haveEnoughConnectedPlayers) {
                return;
            }

            final ArrayList<ClientConnection> players = new ArrayList<>();
            while (players.size() < playersNumber) {
                players.add(candidates.get(0));
                candidates.remove(0);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new GameHandler(players, playersNumber == 1 ? GameTypes.SINGLEPLAYER : GameTypes.MULTIPLAYER).playGame();
                        System.out.println("GAME PLAYED");
                    } catch (Exception e) {
                        System.out.println("EXCEPTION!!!");
                        for (ClientConnection currentConnection : players) {
                            try {
                                currentConnection.getClientOutput().println("Game over");
                            } catch (Exception e2) {
                                //just ignore
                            }
                        }
                        System.out.println("HANDLED!!!");
                    }
                    System.out.println("PLAYED");
                    return;

                }
            }).start();
            System.out.println("GAME GAME PLAYED");
        }
        System.out.println("GAME GAME GAME PLAYED");

    }

}

