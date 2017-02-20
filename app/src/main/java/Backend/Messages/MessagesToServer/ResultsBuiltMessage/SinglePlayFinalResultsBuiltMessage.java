package Backend.Messages.MessagesToServer.ResultsBuiltMessage;

import java.io.IOException;

import Backend.Client.Client;
import Backend.Server.ClientConnection;

public class SinglePlayFinalResultsBuiltMessage {

    public static void submit(Client client){
        client.getClientOutput().println("SINGLE_PLAY_FINAL_RESULTS_BUILT");
    }

    public static void receive(ClientConnection connection) throws IOException {
        connection.getClientInput().readLine();
    }

}