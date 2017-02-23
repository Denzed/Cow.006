package Backend.Messages.MessagesToClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Backend.Client.LeaderboardRequester;
import Backend.Database.LeaderboardRecord;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class LeaderboardSentMessage {

    public static void submit(ClientConnection connection, List<LeaderboardRecord> leaderboard){
        connection.getClientOutput().println(leaderboard.size());
        for (LeaderboardRecord leaderboardRecord : leaderboard){
            connection.getClientOutput().println(leaderboardRecord.getUsername());
            connection.getClientOutput().println(leaderboardRecord.getRating());
        }
    }

    public static void receive(LeaderboardRequester client) throws IOException {
        int size = parseInt(client.getClientInput().readLine());
        List<LeaderboardRecord> leaderboard = new ArrayList<>();
        for (int i = 0; i < size; i++){
            String username = client.getClientInput().readLine();
            int rating = parseInt(client.getClientInput().readLine());
            leaderboard.add(new LeaderboardRecord(username, rating));
        }
        client.setLeaderboard(leaderboard);
    }

}
