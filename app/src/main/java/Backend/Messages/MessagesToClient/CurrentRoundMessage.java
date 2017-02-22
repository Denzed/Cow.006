package Backend.Messages.MessagesToClient;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

import Backend.Client.Client;
import Backend.Client.GameClient;
import Backend.Server.ClientConnection;
import Backend.Game.Turn;

import static java.lang.Integer.parseInt;

public class CurrentRoundMessage {

    public static void submit(ClientConnection connection, Queue<Turn> turns){
        connection.getClientOutput().println("CURRENT_ROUND");
        for (Turn turn : turns){
            connection.getClientOutput().println(turn.getCard());
            connection.getClientOutput().println(turn.getID());
        }
    }

    public static void submitAll(List<ClientConnection> connections, Queue<Turn> turnsQueue) {
        for (final ClientConnection currentConnection : connections) {
            CurrentRoundMessage.submit(currentConnection, turnsQueue);
        }
    }

    public static void receive(GameClient client) throws IOException {
        Queue<Turn> turnsQueue = new ArrayDeque<>();
        Deque<Integer> cardsQueue = new ArrayDeque<>();
        for (int i = 0; i < client.getConnectedPlayer().getPlayersNumber(); i++){
            int card = parseInt(client.getClientInput().readLine());
            cardsQueue.add(card);
            int ID = parseInt(client.getClientInput().readLine());
            turnsQueue.add(new Turn(card, ID));
        }

        client.getConnectedPlayer().setCardsQueue(cardsQueue);
        client.getConnectedPlayer().setTurnsQueue(turnsQueue);
    }

}
