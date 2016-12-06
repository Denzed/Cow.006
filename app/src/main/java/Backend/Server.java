package Backend;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static Backend.Server.CONNECTIONS_NUMBER;


public class Server {

    private static ServerSocket serverSocket = null;
    static volatile int CONNECTIONS_NUMBER = 1;
//    static volatile int CONNECTIONS_NUMBER = 4;

    static List<ClientThread> connections = Collections.synchronizedList(new ArrayList<ClientThread>());
    private static final int REMOTE_NUMBER = 1;
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
        connections.add(connection);
        connection.start();
        while (CONNECTIONS_NUMBER == 1){}

        while (connections.size() < CONNECTIONS_NUMBER) {
            try {
/*                Socket clientSocket = serverSocket.accept();
                ClientThread connection = new ClientThread(clientSocket);
*/
                clientSocket = serverSocket.accept();
                connection = new ClientThread(clientSocket);
                connections.add(connection);
                connection.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        boolean canCreateHandler = false;
        while (!canCreateHandler){
            boolean ok = (connections.size() == CONNECTIONS_NUMBER);
            for (ClientThread currentConnection : connections){
                ok &= currentConnection.clientInput != null && currentConnection.clientOutput != null;
            }
            canCreateHandler = ok;
        }

        new GameHandler(connections).playGame();
        serverSocket.close();
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

            clientOutput.println("Players");
            CONNECTIONS_NUMBER=Integer.parseInt(clientInput.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}