package Backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection {

    private BufferedReader clientInput;
    private PrintWriter clientOutput;
    private Client.ConnectionTypes connectionType;
    private int playersNumber;

    ClientConnection(Socket clientSocket) throws IOException{
        clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        clientOutput.println("Connection type");
        connectionType = Client.ConnectionTypes.valueOf(clientInput.readLine());
        if (connectionType != Client.ConnectionTypes.LEADERBOARD){
            clientOutput.println("Players");
            playersNumber = Integer.parseInt(clientInput.readLine());
        }
    }

    public BufferedReader getClientInput() {
        return clientInput;
    }

    public PrintWriter getClientOutput() {
        return clientOutput;
    }

    public Client.ConnectionTypes getConnectionType() {
        return connectionType;
    }

    public int getPlayersNumber() {
        return playersNumber;
    }
}
