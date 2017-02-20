package Backend.Messages.MessagesToClient;

import java.io.IOException;
import java.util.List;

import Backend.Client.Client;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class SmallestCardTurnMessage {

    public static void submit(ClientConnection connection, int chosenRowIndex){
        connection.getClientOutput().println("SMALLEST_CARD_TURN");
        connection.getClientOutput().println(chosenRowIndex);
    }

    public static void submitAll(List<ClientConnection> connections, int chosenRowIndex) {
        for (ClientConnection connection : connections){
            submit(connection, chosenRowIndex);
        }
    }

    public static void receive(Client client) throws IOException {
        int chosenRowIndex = parseInt(client.getClientInput().readLine());
        client.connectedPlayer.buildBoardModificationsQueue(chosenRowIndex);
    }

}
