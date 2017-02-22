package Backend.Messages.MessagesToServer;

import java.io.IOException;

import Backend.Client.Client;
import Backend.Client.GameClient;
import Backend.Player.PlayerInformation;
import Backend.Server.ClientConnection;

public class PlayerInformationMessage {

    public static void submit(GameClient client, PlayerInformation playerInformation){
        client.getClientOutput().println(playerInformation.getUsername());
        client.getClientOutput().println(playerInformation.getUserID());
    }

    public static PlayerInformation receive(ClientConnection connection) throws IOException {
        String username = connection.getClientInput().readLine();
        String userID = connection.getClientInput().readLine();
        return new PlayerInformation(username, userID);
    }

}
