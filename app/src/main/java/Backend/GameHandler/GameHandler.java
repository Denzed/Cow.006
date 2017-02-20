package Backend.GameHandler;

import Backend.Game.Board;
import Backend.Game.Row;
import Backend.Messages.MessagesToClient.*;
import Backend.Messages.MessagesToServer.*;
import Backend.Player.PlayerInformation;
import Backend.Server.ClientConnection;
import Backend.Game.Turn;
//import javafx.util.Pair;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

import static Backend.Game.GameConstants.*;
import static Backend.GameHandler.GameHandler.GameFinishedReasons.GAME_OVER;
import static java.util.Collections.max;

public abstract class GameHandler {

    protected int playersNumber;
    protected List<ClientConnection> connections;
    protected List<PlayerInformation> playersInformations;
    protected ExecutorService threadPool;
    public enum GameFinishedReasons { GAME_OVER, SOMEONE_HAS_DISCONNECTED };

    public GameHandler(List<ClientConnection> connections, List<PlayerInformation> playersInformations) {
        playersNumber = connections.size();
        this.connections = connections;
        this.playersInformations = playersInformations;
    }

    public void playGame() throws InterruptedException, ExecutionException, IOException, SQLException {
        GameStartedMessage.submitAll(connections, playersInformations);
        do {
            dealCards();
            for (int i = 0; i < ROUNDS; i++) {
                playRound();
            }
        } while (!hasSomeoneBusted());
        processResults();
        GameFinishedMessage.submitAll(connections, GAME_OVER);
    }

    protected void dealCards() {
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

    protected void playRound() throws IOException, InterruptedException, ExecutionException {
        final Queue<Turn> turnsQueue = buildTurnsQueue();
        CurrentRoundMessage.submitAll(connections, turnsQueue);

        ClientConnection playerWithSmallestCard = connections.get(turnsQueue.peek().getID());
        SendRowMessage.submit(playerWithSmallestCard);
        int chosenRowIndex = RowSelectedMessage.receive(playerWithSmallestCard);
        SmallestCardTurnMessage.submitAll(connections, chosenRowIndex);
    }

    protected Queue<Turn> buildTurnsQueue() throws InterruptedException, ExecutionException {
        threadPool = Executors.newFixedThreadPool(playersNumber);
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

    protected boolean hasSomeoneBusted() throws InterruptedException, ExecutionException, IOException {
        SendMaxScoreMessage.submit(connections.get(0));
        return MaxScoreSentMessage.receive(connections.get(0)) >= STOP_POINTS;
    }

    protected abstract void processResults() throws SQLException, InterruptedException, IOException, ExecutionException;
}
