package Backend.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

import Backend.Client.GameClient;
import Backend.Player.Bot;
import Backend.GameHandler.SinglePlayHandler;
import Backend.Messages.MessagesToServer.PlayerInformationMessage;
import Backend.Messages.MessagesToServer.PlayersNumberMessage;
import Backend.Player.PlayerInformation;

import static Backend.Client.Client.GAME_PORT_NUMBER;
import static Backend.Client.Client.LOCALHOST;
import static java.lang.String.valueOf;

public class SinglePlayServer extends GameServer{

    public static void main(String[] Args) throws IOException {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(GAME_PORT_NUMBER);
            List<ClientConnection> connections = new ArrayList<>();
            List<PlayerInformation> playerInformations = new ArrayList<>();
            ClientConnection playerConnection = new ClientConnection(serverSocket.accept());

            connections.add(playerConnection);
            int botsNumber = PlayersNumberMessage.receive(playerConnection) - 1;
            PlayerInformation playerInformation = PlayerInformationMessage.receive(playerConnection);
            playerInformations.add(playerInformation);

            requestBots(botsNumber);
            for (int i = 0; i < botsNumber; i++) {
                ClientConnection botConnection = new ClientConnection(serverSocket.accept());
                connections.add(botConnection);
                playerInformations.add(PlayerInformationMessage.receive(botConnection));
            }

            startGame(new SinglePlayHandler(connections, playerInformations));
        } finally {
            if (serverSocket != null){
                serverSocket.close();
            }
        }
    }
    
    private static void requestBots(int botsNumber) {
        List<String> botIDs = new ArrayList<>();
        while (botIDs.size() < botsNumber){
            String botID = valueOf(new Random().nextInt(1000));
            if (!botIDs.contains(botID)) {
                botIDs.add(botID);
            }
        }

        for (int i = 0; i < botsNumber; i++){
            String botID = botIDs.get(i);
            new Thread(() -> {
                try {
                    new GameClient(new Bot(botsNumber + 1, new PlayerInformation("Bot #" + botID,""))).requestGame(LOCALHOST, GAME_PORT_NUMBER);
                } catch (IOException e) {
                    //ignore
                }
            }).start();
        }
    }

}
