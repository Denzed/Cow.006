package Backend.Messages.MessagesToClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Backend.Client.LeaderBoardRequester;
import Backend.Database.LeaderBoardRecord;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class LeaderBoardSentMessage {

    public static void submit(ClientConnection connection, List<LeaderBoardRecord> leaderboard){
        connection.getClientOutput().println(leaderboard.size());
        for (LeaderBoardRecord leaderBoardRecord : leaderboard){
            connection.getClientOutput().println(leaderBoardRecord.getUsername());
            connection.getClientOutput().println(leaderBoardRecord.getRating());
        }
    }

    public static void receive(LeaderBoardRequester client) throws IOException {
        int size = parseInt(client.getClientInput().readLine());
        List<LeaderBoardRecord> leaderBoard = new ArrayList<>();
        for (int i = 0; i < size; i++){
            String username = client.getClientInput().readLine();
            int rating = parseInt(client.getClientInput().readLine());
            leaderBoard.add(new LeaderBoardRecord(username, rating));
        }
        client.setLeaderBoard(leaderBoard);
    }

}
