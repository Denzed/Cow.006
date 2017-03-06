package Backend.Messages.MessagesToServer.ResultsBuiltMessage;

import java.io.IOException;

import Backend.Client.GameClient;
import Backend.Server.ClientConnection;

public class SinglePlayFinalResultsBuiltMessage {

    public static void submit(GameClient client){
        while (!client.getConnectedPlayer().getCardsQueue().isEmpty()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            //ignore
        }

        client.getClientOutput().println("SINGLE_PLAY_FINAL_RESULTS_BUILT");
    }

    public static void receive(ClientConnection connection) throws IOException {
        connection.getClientInput().readLine();
    }

}
