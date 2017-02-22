package Backend.Messages.MessagesToServer;

import java.io.IOException;

import Backend.Client.Client;
import Backend.Server.ClientConnection;

public class IAmConnectedMessage {

    public static void submit(Client client){
        client.getClientOutput().println("I_AM_CONNECTED");
    }

    public static void receive(ClientConnection connection) throws IOException {
        connection.getClientInput().readLine();
    }
}
