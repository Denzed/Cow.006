package Backend.Messages.MessagesToServer;

import java.io.IOException;

import Backend.Client.GameClient;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class CardSelectedMessage {

    public static void submit(GameClient client, int card){
        System.out.println("SUBMITTED..." + card);
        client.getClientOutput().println(card);
    }

    public static int receive(ClientConnection connection) throws IOException {
        System.out.println("RECEIVING...");
        return parseInt(connection.getClientInput().readLine());
    }

}
