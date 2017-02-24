package Backend.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class Client {

    public static final String LOCALHOST = "localhost";
    public static final String MY_LAPTOP_HOST = "192.168.210.110";
    public static final int PORT_NUMBER = 8080;

    Socket clientSocket;
    BufferedReader clientInput;
    PrintWriter clientOutput;
    volatile boolean isClosed;

    public Client(){}

    void connectToServer(String host) throws IOException {
        clientSocket = new Socket(host, PORT_NUMBER);
        clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        while (clientInput == null || clientOutput == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }
    }

    public void disconnectFromServer() throws IOException {
        isClosed = true;
        clientSocket.close();
    }

    public BufferedReader getClientInput() {
        return clientInput;
    }

    public PrintWriter getClientOutput() {
        return clientOutput;
    }


}