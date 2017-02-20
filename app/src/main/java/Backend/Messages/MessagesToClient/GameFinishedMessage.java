package Backend.Messages.MessagesToClient;

import java.io.IOException;
import java.util.List;

import Backend.Client.Client;
import Backend.GameHandler.GameHandler;
import Backend.Server.ClientConnection;

public class GameFinishedMessage {

    public static void submit(ClientConnection connection, GameHandler.GameFinishedReasons reason){
        connection.getClientOutput().println("GAME_FINISHED");
        connection.getClientOutput().println(reason);
    }

    public static void submitAll(List<ClientConnection> connections, GameHandler.GameFinishedReasons reason) {
        for (ClientConnection connection : connections){
            submit(connection, reason);
        }
    }

    public static void receive(Client client) throws IOException {
        while (!client.connectedPlayer.getCardsQueue().isEmpty()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }

        GameHandler.GameFinishedReasons reason =  GameHandler.GameFinishedReasons.valueOf(client.getClientInput().readLine());
        if (reason == GameHandler.GameFinishedReasons.GAME_OVER){
            client.connectedPlayer.setGameFinished();
        } else {
            client.connectedPlayer.setGameInterrupted();
        }
    }

}
