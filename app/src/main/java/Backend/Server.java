package Backend;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

import static Backend.AbstractPlayer.DECK_SIZE;
import static Backend.AbstractPlayer.ROUNDS;

public class Server {

    private static ServerSocket serverSocket = null;
    private static ArrayList<ArrayDeque<ClientThread>> connections =
            new ArrayList<>(Collections.nCopies(DECK_SIZE / ROUNDS + 1, new ArrayDeque<ClientThread>()));
    private static final int PORT_NUMBER = 5222;

    public static void main(String[] Args) throws IOException {
        serverSocket = new ServerSocket(PORT_NUMBER);
        while (true) {
            waitForConnections();
        }
    }

    private static synchronized void waitForConnections() {
        try {
            Socket clientSocket = serverSocket.accept();
            System.out.println("CONNECTED");
            ClientThread connection = new ClientThread(clientSocket);
            connection.start();
            while (connection.playersNumber == 0) {}

            connections.get(connection.playersNumber).add(connection);
            if (connections.get(connection.playersNumber).size() >= connection.playersNumber){
                ArrayDeque<ClientThread> deque = connections.get(connection.playersNumber);
                final ArrayList<ClientThread> players = new ArrayList<>();
                for (int i = 0; i < connection.playersNumber; i++){
                    players.add(deque.poll());
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new GameHandler(players).playGame();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
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