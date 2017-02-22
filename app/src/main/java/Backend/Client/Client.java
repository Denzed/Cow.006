package Backend.Client;

import java.io.*;
import java.net.Socket;

import Backend.Messages.MessagesToClient.BuildFinalResultsMessages.*;
import Backend.Messages.MessagesToServer.ResultsBuiltMessage.*;
import Backend.Messages.MessagesToClient.*;
import Backend.Messages.MessagesToServer.*;
import Backend.Player.*;

import static java.util.Collections.max;

public abstract class Client {

    public static final String LOCALHOST = "localhost";
    public static final String MY_LAPTOP_HOST = "192.168.210.110";
    public static final int PORT_NUMBER = 8080;

    protected Socket clientSocket;
    protected BufferedReader clientInput;
    protected PrintWriter clientOutput;
    protected volatile boolean isClosed;


    public Client(){}

    protected void connectToServer(String host) throws IOException {
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