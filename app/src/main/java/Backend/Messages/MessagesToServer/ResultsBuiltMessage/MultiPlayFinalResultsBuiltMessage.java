package Backend.Messages.MessagesToServer.ResultsBuiltMessage;

import java.io.IOException;
import java.util.List;

import Backend.Client.GameClient;
import Backend.Server.ClientConnection;

public class MultiPlayFinalResultsBuiltMessage {

    public static void submit(GameClient client){
        while (!client.getConnectedPlayer().getCardsQueue().isEmpty()){
            try {
                System.out.println("WAITING FOR DRAWING");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }
        try {
            System.out.println("FINAL WAITING FOR DRAWING");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            //ignore
        }

        client.getClientOutput().println("MULTI_PLAY_FINAL_RESULTS_BUILT");
    }

    public static void receive(ClientConnection connection) throws IOException {
        connection.getClientInput().readLine();
    }

    public static void receiveAll(List<ClientConnection> connections) throws IOException {
        for (ClientConnection connection : connections){
            MultiPlayFinalResultsBuiltMessage.receive(connection);
        }
    }
}
