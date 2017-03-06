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

import static Backend.Client.Client.GAME_PORT_NUMBER;
import static Backend.Game.GameConstants.DECK_SIZE;
import static Backend.Game.GameConstants.ROUNDS;

public class MultiPlayServer extends GameServer {

    public static void main(String[] Args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(GAME_PORT_NUMBER);
        List<Queue<ClientConnection>> buckets =
                new ArrayList<>(Collections.nCopies(DECK_SIZE / ROUNDS + 1, new ArrayDeque<>()));
        List<Queue<PlayerInformation>> infoBuckets =
                new ArrayList<>(Collections.nCopies(DECK_SIZE / ROUNDS + 1, new ArrayDeque<>()));
        Set<String> uniqueIDs = new HashSet<>();
        while (true) {
            waitForConnections(serverSocket, buckets, infoBuckets, uniqueIDs);
        }
    }

    private static synchronized void waitForConnections(
            ServerSocket serverSocket,
            List<Queue<ClientConnection>> buckets,
            List<Queue<PlayerInformation>> infoBuckets,
            Set<String> uniqueIDs) {
        System.out.print("WAITING...");
        ClientConnection playerConnection;
        int playersNumber;
        PlayerInformation playerInformation;
        try {
            playerConnection = new ClientConnection(serverSocket.accept());
            System.out.print("CONNECTED ");
            playersNumber = PlayersNumberMessage.receive(playerConnection);
            System.out.println(playersNumber);
            playerInformation = PlayerInformationMessage.receive(playerConnection);
            String userID = playerInformation.getUserID();
            if (uniqueIDs.contains(userID)){
                removeAllTheOccurrencesFromBuckets(buckets, infoBuckets, userID);
            }
            System.out.println(playerInformation.getUsername() + " " + playerInformation.getUserID());
            uniqueIDs.add(userID);

        } catch (IOException e) {
            return;
        }
        Queue<ClientConnection> bucket = buckets.get(playersNumber);
        Queue<PlayerInformation> infoBucket = infoBuckets.get(playersNumber);
        bucket.add(playerConnection);
        infoBucket.add(playerInformation);
        System.out.println("IN BUCKET:");
        for (PlayerInformation x : new ArrayList<>(infoBucket)){
            System.out.println(x.getUsername() + " " + x.getUserID());
        }
        System.out.println(bucket.size());
        if (bucket.size() >= playersNumber) {
            if (haveEnoughConnectedPlayers(new ArrayList<>(bucket), new ArrayList<>(infoBucket), playersNumber)){
                List<ClientConnection> players = new ArrayList<>();
                List<PlayerInformation> playerInformations = new ArrayList<>();
                buildPlayersList(bucket, infoBucket, playersNumber, players, playerInformations);
                startGame(new MultiPlayHandler(players, playerInformations));
            }
        }
    }

    private static void removeAllTheOccurrencesFromBuckets(List<Queue<ClientConnection>> buckets,
                                                           List<Queue<PlayerInformation>> infoBuckets,
                                                           String userID) {
        for (int i = 0; i < buckets.size(); i++){
            List<ClientConnection> x = new ArrayList<>(buckets.get(i));
            List<PlayerInformation> y = new ArrayList<>(infoBuckets.get(i));
            for (int j = 0; j < y.size(); j++){
                if (y.get(j).getUserID().equals(userID)){
                    x.remove(j);
                    y.remove(j);
                }
            }
            buckets.set(i, new ArrayDeque<>(x));
            infoBuckets.set(i, new ArrayDeque<>(y));
        }
    }

    private static boolean haveEnoughConnectedPlayers(
            List<ClientConnection> candidates, List<PlayerInformation> infoCandidates, int playersNumber) {
        for (int i = 0; i < playersNumber; i++){
            IsConnectedMessage.submit(candidates.get(i));
            try{
                IAmConnectedMessage.receive(candidates.get(i));
            } catch (Exception e) {
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