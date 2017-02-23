package Backend.Messages.MessagesToServer;

import java.io.IOException;

import Backend.Client.GameClient;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class RowSelectedMessage {

    public static void submit(GameClient client, int row){
        client.getClientOutput().println(row);
    }

    public static int receive(ClientConnection connection) throws IOException {
        return parseInt(connection.getClientInput().readLine());
    }

}
