package Backend.Messages.MessagesToClient.BuildFinalResultsMessages;

import java.util.List;

import Backend.Client.Client;
import Backend.Server.ClientConnection;

public class BuildSinglePlayFinalResultsMessage {

    public static void submit(ClientConnection connection){
        connection.getClientOutput().println("BUILD_SINGLE_PLAY_FINAL_RESULTS");
    }

    public static void receive(Client client){
        client.connectedPlayer.buildSinglePlayFinalResults();
    }

    public static void submitAll(List<ClientConnection> connections) {
        for (ClientConnection connection : connections){
            BuildSinglePlayFinalResultsMessage.submit(connection);
        }
    }
}
