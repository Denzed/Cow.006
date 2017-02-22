package Backend.Messages.MessagesToServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Backend.Client.Client;
import Backend.Client.GameClient;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class ScoresSentMessage {

    public static void submit(GameClient client, List<Integer> scores){
        client.getClientOutput().println(scores.size());
        for (Integer x : scores){
            client.getClientOutput().println(x);
        }
    }

    public static List<Integer> receive(ClientConnection connection) throws IOException {
        List<Integer> scores = new ArrayList<>();
        int size = parseInt(connection.getClientInput().readLine());
        for (int i = 0; i < size; i++){
            scores.add(parseInt(connection.getClientInput().readLine()));
        }
        return scores;
    }

}
