package Backend.Server;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;

import Backend.GameHandler.MultiPlayHandler;
import Backend.Messages.MessagesToClient.IsConnectedMessage;
import Backend.Messages.MessagesToServer.IAmConnectedMessage;
import Backend.Messages.MessagesToServer.PlayerInformationMessage;
import Backend.Messages.MessagesToServer.PlayersNumberMessage;
import Backend.Player.PlayerInformation;

import static Backend.Game.GameConstants.DECK_SIZE;
import static Backend.Game.GameConstants.ROUNDS;

public class MultiPlayServer extends GameServer {

    private static final int PORT_NUMBER = 8080;

    public static void main(String[] Args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
        List<Queue<ClientConnection>> buckets =
                new ArrayList<>(Collections.nCopies(DECK_SIZE / ROUNDS + 1, new ArrayDeque<>()));
        List<Queue<PlayerInformation>> infoBuckets =
                new ArrayList<>(Collections.nCopies(DECK_SIZE / ROUNDS + 1, new ArrayDeque<>()));
        while (true) {
            waitForConnections(serverSocket, buckets, infoBuckets);
        }
    }

    private static synchronized void waitForConnections(
            ServerSocket serverSocket,
            List<Queue<ClientConnection>> buckets,
            List<Queue<PlayerInformation>> infoBuckets) throws IOException {
        ClientConnection playerConnection = new ClientConnection(serverSocket.accept());

        int playersNumber = PlayersNumberMessage.receive(playerConnection);
        PlayerInformation playerInformation = PlayerInformationMessage.receive(playerConnection);
        Queue<ClientConnection> bucket = buckets.get(playersNumber);
        Queue<PlayerInformation> infoBucket = infoBuckets.get(playersNumber);
        bucket.add(playerConnection);
        infoBucket.add(playerInformation);

        if (bucket.size() >= playersNumber) {
            if (haveEnoughConnectedPlayers(new ArrayList<>(bucket), new ArrayList<>(infoBucket), playersNumber)){
                List<ClientConnection> players = new ArrayList<>();
                List<PlayerInformation> playerInformations = new ArrayList<>();
                buildPlayersList(bucket, infoBucket, playersNumber, players, playerInformations);
                startGame(new MultiPlayHandler(players, playerInformations));
            }
        }
    }

    private static boolean haveEnoughConnectedPlayers(
            List<ClientConnection> candidates, List<PlayerInformation> infoCandidates, int playersNumber) {
        for (int i = 0; i < playersNumber; i++){
            IsConnectedMessage.submit(candidates.get(i));
            try{
                IAmConnectedMessage.receive(candidates.get(i));
            } catch (IOException e) {
                candidates.remove(i);
                infoCandidates.remove(i);
                return false;
            }
        }
        return true;
    }

    private static void buildPlayersList(
            Queue<ClientConnection> bucket, Queue<PlayerInformation> infoBucket, int playersNumber,
            List<ClientConnection> players, List<PlayerInformation> playerInformations) {
        while (players.size() < playersNumber) {
            players.add(bucket.poll());
            playerInformations.add(infoBucket.poll());
        }
    }

}