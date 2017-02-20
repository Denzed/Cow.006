package Backend.GameHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import Backend.Player.PlayerInformation;
import Backend.Server.ClientConnection;

public class MultiPlayHandler extends GameHandler{

    public MultiPlayHandler(List<ClientConnection> connections, List<PlayerInformation> playersInformations) {
        super(connections, playersInformations);
    }

    @Override
    protected void processResults() throws SQLException, InterruptedException, IOException, ExecutionException {
/*        List<DatabaseRecord> playersInformation = getPlayersInformation(playersNumber, connections);
        updateRatings(playersInformation);
        tellAllClients("Results");
        for (DatabaseRecord playerInformation : playersInformation) {
            tellAllClients(playerInformation.getUsername());
            tellAllClients(String.valueOf(playerInformation.getRating()));
            int ratingChange = playerInformation.getRatingChange();
            tellAllClients("(" + (ratingChange > 0 ? '+' : "") + ratingChange + ")");
        }
  */
    }

}
