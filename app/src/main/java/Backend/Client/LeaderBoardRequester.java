package Backend.Client;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Backend.Database.LeaderBoardRecord;
import Backend.Messages.MessagesToClient.LeaderBoardSentMessage;
import Backend.Messages.MessagesToServer.SendLeaderBoardMessage;

public class LeaderBoardRequester extends Client{

    private int leaderboardSize;
    private List<LeaderBoardRecord> leaderBoard;

    public LeaderBoardRequester(int leaderboardSize) {
        this.leaderboardSize = leaderboardSize;
        leaderBoard = new ArrayList<>();
    }

    public List<LeaderBoardRecord> requestLeaderBoard() throws IOException {
        List<LeaderBoardRecord> leaderbord = new ArrayList<>();
        connectToServer(MY_LAPTOP_HOST);
        SendLeaderBoardMessage.submit(this, leaderboardSize);
        LeaderBoardSentMessage.receive(this);
        return leaderbord;
    }


    public void setLeaderBoard(List<LeaderBoardRecord> leaderBoard) {
        this.leaderBoard = leaderBoard;
    }
}
