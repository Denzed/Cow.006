package Backend.Messages.MessagesToServer;

import java.io.IOException;

import Backend.Client.Client;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class CardSelectedMessage {

    public static void submit(Client client, int card){
        client.getClientOutput().println(card);
    }

    public static int receive(ClientConnection connection) throws IOException {
        return parseInt(connection.getClientInput().readLine());
    }

}
