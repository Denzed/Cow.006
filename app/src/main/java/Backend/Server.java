package Backend;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static Backend.Server.*;
public class Server {

    private static ServerSocket serverSocket = null;
    static final int CONNECTIONS_NUMBER = 4;
    static List<ClientThread> connections = Collections.synchronizedList(new ArrayList<ClientThread>());
    private static final int REMOTE_NUMBER = 1;
    private static final int PORT_NUMBER = 2222;

    public static void main(String[] Args) throws IOException {
        getConnections();
    }
    private static void getConnections() throws IOException{
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < CONNECTIONS_NUMBER - REMOTE_NUMBER; i++)
            new Thread(new Runnable() {
                public void run()
                {
                    try {
                        new Client(new Bot(4)).connectToServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        while (connections.size() < CONNECTIONS_NUMBER) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientThread connection = new ClientThread(clientSocket);
                connections.add(connection);
                connection.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


class ClientThread extends Thread {

    private Socket clientSocket;
    BufferedReader clientInput = null;
    PrintWriter clientOutput = null;

    ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
            if (connections.size() == CONNECTIONS_NUMBER){
                new GameHandler(connections).playGame();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}