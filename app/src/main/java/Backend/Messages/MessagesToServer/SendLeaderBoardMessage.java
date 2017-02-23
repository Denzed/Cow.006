package Backend.Messages.MessagesToServer;

import java.io.IOException;

import Backend.Client.LeaderboardRequester;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class SendLeaderboardMessage {

    public static void submit(LeaderboardRequester client, int leaderboardSize){
        client.getClientOutput().println(leaderboardSize);
    }

    public static int receive(ClientConnection connection) throws IOException {
        return parseInt(connection.getClientInput().readLine());
    }
}
