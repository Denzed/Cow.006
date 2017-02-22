package Backend.Messages.MessagesToServer;

import java.io.IOException;

import Backend.Client.GameClient;
import Backend.Client.LeaderBoardRequester;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class SendLeaderBoardMessage {

    public static void submit(LeaderBoardRequester client, int leaderboardSize){
        client.getClientOutput().println(leaderboardSize);
    }

    public static int receive(ClientConnection connection) throws IOException {
        return parseInt(connection.getClientInput().readLine());
    }
}
