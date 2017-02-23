package Backend.GameHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Backend.Game.Board;
import Backend.Game.Row;
import Backend.Game.Turn;
import Backend.Messages.MessagesToClient.CurrentRoundMessage;
import Backend.Messages.MessagesToClient.DealStartedMessage;
import Backend.Messages.MessagesToClient.GameFinishedMessage;
import Backend.Messages.MessagesToClient.GameStartedMessage;
import Backend.Messages.MessagesToClient.SendCardMessage;
import Backend.Messages.MessagesToClient.SendMaxScoreMessage;
import Backend.Messages.MessagesToClient.SendRowMessage;
import Backend.Messages.MessagesToClient.SmallestCardTurnMessage;
import Backend.Messages.MessagesToServer.CardSelectedMessage;
import Backend.Messages.MessagesToServer.MaxScoreSentMessage;
import Backend.Messages.MessagesToServer.RowSelectedMessage;
import Backend.Player.PlayerInformation;
import Backend.Server.ClientConnection;

import static Backend.Game.GameConstants.DECK_SIZE;
import static Backend.Game.GameConstants.ROUNDS;
import static Backend.Game.GameConstants.ROWS;
import static Backend.Game.GameConstants.STOP_POINTS;
import static Backend.GameHandler.GameHandler.GameFinishedReasons.GAME_OVER;

//import javafx.util.Pair;

public abstract class GameHandler {

    private int playersNumber;
    protected List<ClientConnection> connections;
    List<PlayerInformation> playerInformations;

    public List<ClientConnection> getConnections() {
        return connections;
    }

    public enum GameFinishedReasons { GAME_OVER, SOMEONE_HAS_DISCONNECTED }

    GameHandler(List<ClientConnection> connections, List<PlayerInformation> playerInformations) {
        playersNumber = connections.size();
        this.connections = connections;
        this.playerInformations = playerInformations;
    }

    public void playGame() throws InterruptedException, ExecutionException, IOException, SQLException {
        GameStartedMessage.submitAll(connections, playerInformations);
        do {
            dealCards();
            for (int i = 0; i < ROUNDS; i++) {
                playRound();
            }
        } while (!hasSomeoneBusted());
        processResults();
        GameFinishedMessage.submitAll(connections, GAME_OVER);
    }

    private void dealCards() {
        List<Integer> deck = new ArrayList<>();
        for (int i = 1; i <= DECK_SIZE; i++){
            deck.add(i);
        }
        Collections.shuffle(deck);

        for (ClientConnection currentConnection : connections) {
            int i = connections.indexOf(currentConnection);
            List<Integer> hand = new ArrayList<>();
            for (int j = i * ROUNDS; j < (i + 1) * ROUNDS; j++) {
                hand.add(deck.get(j));
            }
            Board board = new Board();
            for (int j = ROUNDS * playersNumber; j < ROUNDS * playersNumber + ROWS; j++) {
                Row row = new Row();
                row.add(deck.get(j));
                board.add(row);
            }
            DealStartedMessage.submit(currentConnection, hand, board);
        }
    }

    private void playRound() throws IOException, InterruptedException, ExecutionException {
        final Queue<Turn> turnsQueue = buildTurnsQueue();
        CurrentRoundMessage.submitAll(connections, turnsQueue);

        ClientConnection playerWithSmallestCard = connections.get(turnsQueue.peek().getID());
        SendRowMessage.submit(playerWithSmallestCard);
        int chosenRowIndex = RowSelectedMessage.receive(playerWithSmallestCard);
        SmallestCardTurnMessage.submitAll(connections, chosenRowIndex);
    }

    private Queue<Turn> buildTurnsQueue() throws InterruptedException, ExecutionException {
        ExecutorService threadPool = Executors.newFixedThreadPool(playersNumber);
        List<Callable<Turn>> tasksForTurns = new ArrayList<>();
        List<Turn> turns = new ArrayList<>();

        for (final ClientConnection currentConnection: connections) {
            tasksForTurns.add(() -> {
                SendCardMessage.submit(currentConnection);
                return new Turn(
                        CardSelectedMessage.receive(currentConnection),
                        connections.indexOf(currentConnection));
            });
        }

        for (Future<Turn> taskForTurn : threadPool.invokeAll(tasksForTurns)){
            turns.add(taskForTurn.get());
        }
        threadPool.shutdown();

        Collections.sort(turns, (t1, t2) -> t1.getCard() - t2.getCard());
        return new ArrayDeque<>(turns);
    }

    private boolean hasSomeoneBusted() throws InterruptedException, ExecutionException, IOException {
        SendMaxScoreMessage.submit(connections.get(0));
        return MaxScoreSentMessage.receive(connections.get(0)) >= STOP_POINTS;
    }

    protected abstract void processResults() throws SQLException, InterruptedException, IOException, ExecutionException;
}
