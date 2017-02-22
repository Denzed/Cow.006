package Backend.Messages.MessagesToServer.ResultsBuiltMessage;

import java.io.IOException;
import java.util.List;

import Backend.Client.Client;
import Backend.Server.ClientConnection;

public class MultiPlayFinalResultsBuiltMessage {

    public static void submit(Client client){
        client.getClientOutput().println("MULTI_PLAY_FINAL_RESULTS_BUILT");
    }

    public static void receive(ClientConnection connection) throws IOException {
        connection.getClientInput().readLine();
    }

    public static void receiveAll(List<ClientConnection> connections) throws IOException {
        for (ClientConnection connection : connections){
            SinglePlayFinalResultsBuiltMessage.receive(connection);
        }
    }
}
