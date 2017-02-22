package Backend.GameHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import Backend.Database.DatabaseConnection;
import Backend.Database.DatabaseRecord;
import Backend.Messages.MessagesToClient.BuildFinalResultsMessages.BuildMultiPlayFinalResultsMessage;
import Backend.Messages.MessagesToClient.SendScoresMessage;
import Backend.Messages.MessagesToServer.ResultsBuiltMessage.MultiPlayFinalResultsBuiltMessage;
import Backend.Messages.MessagesToServer.ScoresSentMessage;
import Backend.Player.PlayerInformation;
import Backend.Server.ClientConnection;

import static Backend.Database.DatabaseConnection.DB_LOGIN;
import static Backend.Database.DatabaseConnection.DB_TABLE_NAME;
import static Backend.Database.DatabaseConnection.DB_URL_ADDRESS;
import static Backend.Database.DatabaseConnection.SECRET_PASSWORD;
import static Backend.Database.Rating.updateRatings;

public class MultiPlayHandler extends GameHandler{

    public MultiPlayHandler(List<ClientConnection> connections, List<PlayerInformation> playersInformations) {
        super(connections, playersInformations);
    }

    @Override
    protected void processResults() throws SQLException, InterruptedException, IOException, ExecutionException {
        DatabaseConnection dbConnection =
                new DatabaseConnection(DB_URL_ADDRESS, DB_TABLE_NAME, DB_LOGIN, SECRET_PASSWORD);
        List<DatabaseRecord> databaseRecords = dbConnection.requestDatabaseRecords(playersInformations);
        SendScoresMessage.submit(connections.get(0));
        List<Integer> scores = ScoresSentMessage.receive(connections.get(0));
        List<Integer> ratings = new ArrayList<>();
        List<Integer> ratingChanges = new ArrayList<>();
        updateRatings(databaseRecords, scores, ratings, ratingChanges);
        BuildMultiPlayFinalResultsMessage.submitAll(connections, ratings, ratingChanges);
        MultiPlayFinalResultsBuiltMessage.receiveAll(connections);
    }

}
