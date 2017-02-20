package Backend.Messages.MessagesToClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Backend.Client.Client;
import Backend.Player.PlayerInformation;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class GameStartedMessage {

    public static void submit(ClientConnection connection, List<PlayerInformation> playersInformations){
        connection.getClientOutput().println("GAME_STARTED");
        for (PlayerInformation playerInformation : playersInformations){
            connection.getClientOutput().println(playerInformation.getUsername());
            connection.getClientOutput().println(playerInformation.getUserID());
        }
    }

    public static void submitAll(List<ClientConnection> connections, List<PlayerInformation> playersInformations) {
        for (ClientConnection connection : connections){
            submit(connection, playersInformations);
        }
    }

    public static void receive(Client client) throws IOException {
        List<PlayerInformation> result = new ArrayList<>();
        for (int i = 0; i < client.connectedPlayer.getPlayersNumber(); i++){
            String username = client.getClientInput().readLine();
            String userID = client.getClientInput().readLine();
            result.add(new PlayerInformation(username, userID));
        }

        client.connectedPlayer.setPlayersInformations(result);
    }

}
