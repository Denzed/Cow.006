package Backend.GameHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import Backend.Messages.MessagesToClient.BuildFinalResultsMessages.BuildSinglePlayFinalResultsMessage;
import Backend.Messages.MessagesToServer.ResultsBuiltMessage.SinglePlayFinalResultsBuiltMessage;
import Backend.Player.PlayerInformation;
import Backend.Server.ClientConnection;


public class SinglePlayHandler extends GameHandler{
    public SinglePlayHandler(List<ClientConnection> connections, List<PlayerInformation> playerInformations){
        super(connections, playerInformations);
    }

    @Override
    protected void processResults() throws SQLException, InterruptedException, IOException, ExecutionException {
        BuildSinglePlayFinalResultsMessage.submitAll(connections);
        for (ClientConnection connection : connections) {
            SinglePlayFinalResultsBuiltMessage.receive(connection);
        }
    }
}
