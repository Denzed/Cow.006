package Backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientConnection {

    private BufferedReader clientInput;
    private PrintWriter clientOutput;
    private int playersNumber;

    ClientConnection(Socket clientSocket) throws IOException{
        clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        clientOutput.println("Players");
        playersNumber = Integer.parseInt(clientInput.readLine());
    }

    BufferedReader getClientInput() {
        return clientInput;
    }

    PrintWriter getClientOutput() {
        return clientOutput;
    }

    int getPlayersNumber() {
        return playersNumber;
    }

}
