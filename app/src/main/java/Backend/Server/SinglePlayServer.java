package Backend.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import Backend.Client.GameClient;
import Backend.Player.Bot;
import Backend.GameHandler.SinglePlayHandler;
import Backend.Messages.MessagesToServer.PlayerInformationMessage;
import Backend.Messages.MessagesToServer.PlayersNumberMessage;
import Backend.Player.PlayerInformation;

import static Backend.Client.Client.GAME_PORT_NUMBER;
import static Backend.Client.Client.LOCALHOST;

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
        for (int i = 0; i < botsNumber; i++){
            new Thread(() -> {
                try {
                    new GameClient(new Bot(botsNumber + 1)).requestGame(LOCALHOST, GAME_PORT_NUMBER);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

}
