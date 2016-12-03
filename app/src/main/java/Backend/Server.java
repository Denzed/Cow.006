package Backend;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static Backend.Server.*;
public class Server {

    // The server socket.
    static ServerSocket serverSocket = null;
    Socket clientSocket = null;
    static final int CONNECTIONS_NUMBER = 4;
//    ClientThread[] threads = new ClientThread[CONNECTIONS_NUMBER];
    static List<ClientThread> connections = Collections.synchronizedList(new ArrayList<ClientThread>());

    static int remote = 0;
    static final int REMOTE_NUMBER = 1;
    static int portNumber = 2222;

    static List<BufferedReader> clientInput = new ArrayList<>();
    static List<PrintWriter> clientOutput = new ArrayList<>();

    public static void main(String[] Args){
        getConnections();
    }
    public static void getConnections() {
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.err.println("IOException: " + e);
            e.printStackTrace();
        }
        for (int i = 0; i < CONNECTIONS_NUMBER - REMOTE_NUMBER; i++)
            new Thread(() -> new Client(new Bot()).connectToServer()).start();

        while (connections.size() < CONNECTIONS_NUMBER) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientThread connection = new ClientThread(clientSocket);
                connections.add(connection);
                connection.start();
            } catch (IOException e) {
                System.err.println("IOException: " + e);
                e.printStackTrace();
            }
        }
    }
}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * chat room this thread informs also all the clients about that and terminates.
 */
class ClientThread extends Thread {

    Socket clientSocket;
    BufferedReader stdIn = null;
    BufferedReader clientInput = null;
    PrintWriter clientOutput = null;
    GameHandler gameHandler;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
            System.out.println(clientInput + " " + clientOutput + " " + connections.size());
            if (connections.size() == CONNECTIONS_NUMBER){
                new GameHandler(connections).playGame();
            }
//            clientOutput.println("Type");

        } catch (IOException e) {
            System.out.println("IOException: " + e);
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            stdIn.close();
            clientInput.close();
            clientOutput.close();
            clientSocket.close();
            connections.remove(this);
        } catch (IOException e) {
            System.err.println("IOException: " + e);
            e.printStackTrace();
        }
    }
}