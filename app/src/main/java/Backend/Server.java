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
    static volatile boolean haveCorrectPlayersNumber = false;
    static int CONNECTIONS_NUMBER;
    static int REMOTE_NUMBER;
    static int BOTS_NUMBER;
    private static List<ClientThread> connections = Collections.synchronizedList(new ArrayList<ClientThread>());
    private static final int PORT_NUMBER = 8080;

    public static void main(String[] Args) throws IOException {
        while (true) {
            getConnections();
            connections.clear();
        }
    }

    private static void getConnections() throws IOException{
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Socket clientSocket = serverSocket.accept();
        ClientThread connection = new ClientThread(clientSocket);
        System.out.println("CONNECTED");
        connections.add(connection);
        connection.start();

        while (!haveCorrectPlayersNumber){}

        while (connections.size() < REMOTE_NUMBER) {
            waitForConnections();
        }

        boolean canConnectBots = false;
        while (!canConnectBots){
            boolean ok = (connections.size() == REMOTE_NUMBER);
            for (ClientThread currentConnection : connections){
                ok &= currentConnection.clientInput != null && currentConnection.clientOutput != null;
            }
            canConnectBots = ok;
        }

        for (int i = 0; i < BOTS_NUMBER; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new Client(new Bot(REMOTE_NUMBER, BOTS_NUMBER)).connectToServer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        while (connections.size() < CONNECTIONS_NUMBER) {
            waitForConnections();
        }

        boolean canCreateHandler = false;
        while (!canCreateHandler){
            System.out.println(connections.size());
            boolean ok = (connections.size() == CONNECTIONS_NUMBER);
            for (ClientThread currentConnection : connections){
                ok &= currentConnection.clientInput != null && currentConnection.clientOutput != null;
            }
            canCreateHandler = ok;
        }

        new GameHandler(connections).playGame();
        serverSocket.close();
    }

    private static void waitForConnections() {
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
            clientOutput.println("Connections");
            CONNECTIONS_NUMBER = Integer.parseInt(clientInput.readLine());
            clientOutput.println("Remote");
            REMOTE_NUMBER = Integer.parseInt(clientInput.readLine());
            clientOutput.println("Bots");
            BOTS_NUMBER = Integer.parseInt(clientInput.readLine());
            haveCorrectPlayersNumber = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}