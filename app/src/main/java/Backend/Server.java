package Backend;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

import static Backend.AbstractPlayer.DECK_SIZE;
import static Backend.AbstractPlayer.ROUNDS;

public class Server {

    public static ServerSocket serverSocket = null;
    private static ArrayList<ArrayList<ClientThread>> connections =
            new ArrayList<>(Collections.nCopies(DECK_SIZE / ROUNDS + 1, new ArrayList<ClientThread>()));
    private static final int PORT_NUMBER = 5222;

    public static void main(String[] Args) throws IOException {
        serverSocket = new ServerSocket(PORT_NUMBER);
        while (true) {
            System.out.println("BEFORE WAIT");
            waitForConnections();
            System.out.println("AFTER WAIT");
        }
    }

    private static synchronized void waitForConnections() {
        try {
            System.out.print("TRYING...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("CONNECTED");
            ClientThread connection = new ClientThread(clientSocket);
            connection.start();
            while (connection.playersNumber == 0) {}

            connections.get(connection.playersNumber).add(connection);
            if (connections.get(connection.playersNumber).size() >= connection.playersNumber){
                boolean haveEnoughConnectedPlayers = true;
                ArrayList<ClientThread> candidates = connections.get(connection.playersNumber);
                for (int i = 0; i < connection.playersNumber; i++){
                    try{
                        candidates.get(i).clientOutput.println("IsConnected");
                        Boolean.parseBoolean(candidates.get(i).clientInput.readLine());
                    }
                    catch (IOException e){
                        candidates.remove(i);
                        haveEnoughConnectedPlayers = false;
                        break;
                    }
                }
                if (!haveEnoughConnectedPlayers){
                    return;
                }

                final ArrayList<ClientThread> players = new ArrayList<>();
                while (players.size() < connection.playersNumber){
                    players.add(candidates.get(0));
                    candidates.remove(0);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new GameHandler(players).playGame();
                        } catch (Exception e) {
                            System.out.println("EXCEPTION!!!");
                            for (ClientThread currentConnection : players){
                                try{
                                    currentConnection.clientOutput.println("Game over");
                                }
                                catch (Exception e2){
                                    //just ignore
                                }
                            }
                            System.out.println("HANDLED!!!");
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class ClientThread extends Thread {

    private Socket clientSocket;
    BufferedReader clientInput = null;
    PrintWriter clientOutput = null;
    volatile int playersNumber = 0;

    ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
            clientOutput.println("Players");
            playersNumber = Integer.parseInt(clientInput.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}