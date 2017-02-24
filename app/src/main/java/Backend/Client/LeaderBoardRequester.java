package Backend.Client;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Backend.Database.LeaderboardRecord;
import Backend.Messages.MessagesToClient.LeaderboardSentMessage;
import Backend.Messages.MessagesToServer.SendLeaderboardMessage;

public class LeaderboardRequester extends Client{

    private int leaderboardSize;
    private List<LeaderboardRecord> leaderboard;

    public LeaderboardRequester(int leaderboardSize) {
        this.leaderboardSize = leaderboardSize;
        leaderboard = new ArrayList<>();
    }

    public List<LeaderboardRecord> requestLeaderboard() throws IOException {
        connectToServer(MY_LAPTOP_HOST);
        SendLeaderboardMessage.submit(this, leaderboardSize);
        LeaderboardSentMessage.receive(this);
        return leaderboard;
    }


    public void setLeaderboard(List<LeaderboardRecord> leaderboard) {
        this.leaderboard = leaderboard;
    }
}
