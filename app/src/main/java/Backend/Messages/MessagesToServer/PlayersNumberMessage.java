package Backend.Messages.MessagesToServer;

import java.io.IOException;

import Backend.Client.GameClient;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class PlayersNumberMessage{

    public static void submit(GameClient client, int playersNumber) {
        client.getClientOutput().println(playersNumber);
    }
    public static int receive(ClientConnection connection) throws IOException {
        return parseInt(connection.getClientInput().readLine());
    }

}
