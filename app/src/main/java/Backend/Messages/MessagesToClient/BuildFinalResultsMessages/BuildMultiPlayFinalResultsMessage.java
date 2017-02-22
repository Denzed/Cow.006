package Backend.Messages.MessagesToClient.BuildFinalResultsMessages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Backend.Client.Client;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class BuildMultiPlayFinalResultsMessage {

    public static void submit(ClientConnection connection, List<Integer> ratings, List<Integer> ratingChanges){
        connection.getClientOutput().println("BUILD_MULTI_PLAY_FINAL_RESULTS");
        connection.getClientOutput().println(ratings.size());
        for (int i = 0; i < ratings.size(); i++){
            connection.getClientOutput().println(ratings.get(i));
            connection.getClientOutput().println(ratingChanges.get(i));
        }
    }

    public static void submitAll(List<ClientConnection> connections, List<Integer> ratings, List<Integer> ratingChanges){
        for (ClientConnection connection : connections){
            BuildMultiPlayFinalResultsMessage.submit(connection, ratings, ratingChanges);
        }
    }

    public static void receive(Client client) throws IOException {
        List<String> ratings = new ArrayList<>();
        List<String> ratingChanges = new ArrayList<>();
        int size = parseInt(client.getClientInput().readLine());
        for (int i = 0; i < size; i++){
            ratings.add(client.getClientInput().readLine());
            ratingChanges.add(client.getClientInput().readLine());
        }
        client.getConnectedPlayer().buildMultiPlayFinalResults(ratings, ratingChanges);
    }

}
