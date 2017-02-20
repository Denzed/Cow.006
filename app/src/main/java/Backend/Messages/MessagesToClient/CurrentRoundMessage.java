package Backend.Messages.MessagesToClient;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Backend.Client.Client;
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
        ExecutorService threadPool = Executors.newFixedThreadPool(connections.size());
        List<Callable<Void>> tasksForTurnsSequence = new ArrayList<>();
        for (final ClientConnection currentConnection : connections) {
            tasksForTurnsSequence.add(() -> {
                CurrentRoundMessage.submit(currentConnection, turnsQueue);
                return null;
            });
        }
        try {
            threadPool.invokeAll(tasksForTurnsSequence);
        } catch (InterruptedException e) {
            //ignore
        }
        threadPool.shutdown();
    }

    public static void receive(Client client) throws IOException {
        Queue<Turn> turnsQueue = new ArrayDeque<>();
        Deque<Integer> cardsQueue = new ArrayDeque<>();
        for (int i = 0; i < client.connectedPlayer.getPlayersNumber(); i++){
            int card = parseInt(client.getClientInput().readLine());
            cardsQueue.add(card);
            int ID = parseInt(client.getClientInput().readLine());
            turnsQueue.add(new Turn(card, ID));
        }

        client.connectedPlayer.setCardsQueue(cardsQueue);
        client.connectedPlayer.setTurnsQueue(turnsQueue);
    }

}
