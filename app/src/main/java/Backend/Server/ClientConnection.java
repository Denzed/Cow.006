package Backend.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection {

    private BufferedReader clientInput;
    private PrintWriter clientOutput;

    ClientConnection(Socket clientSocket) {
        try {
            clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            //ignore
        }
    }

    public BufferedReader getClientInput() {
        return clientInput;
    }

    public PrintWriter getClientOutput() {
        return clientOutput;
    }

 }