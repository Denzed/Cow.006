package Backend.Messages.MessagesToServer;

import java.io.IOException;

import Backend.Client.Client;
import Backend.Client.GameClient;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class MaxScoreSentMessage {

    public static void submit(GameClient client, int score){
        client.getClientOutput().println(score);
    }

    public static int receive(ClientConnection currentConnection) throws IOException {
        return parseInt(currentConnection.getClientInput().readLine());
    }

}
