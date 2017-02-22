package Backend.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import Backend.Client.Client;
import Backend.Player.Bot;
import Backend.GameHandler.SinglePlayHandler;
import Backend.Messages.MessagesToServer.PlayerInformationMessage;
import Backend.Messages.MessagesToServer.PlayersNumberMessage;
import Backend.Player.PlayerInformation;

import static Backend.Client.Client.LOCALHOST;
import static Backend.Client.Client.PORT_NUMBER;

public class SinglePlayServer extends GameServer{

    public static void main(String[] Args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
        List<ClientConnection> connections = new ArrayList<>();
        List<PlayerInformation> playersInformations = new ArrayList<>();
        ClientConnection playerConnection = new ClientConnection(serverSocket.accept());

        connections.add(playerConnection);
        int botsNumber = PlayersNumberMessage.receive(playerConnection) - 1;
        PlayerInformation playerInformation = PlayerInformationMessage.receive(playerConnection);
        playersInformations.add(playerInformation);

        requestBots(botsNumber);
        for (int i = 0; i < botsNumber; i++){
            ClientConnection botConnection = new ClientConnection(serverSocket.accept());
            connections.add(botConnection);
            playersInformations.add(PlayerInformationMessage.receive(botConnection));
        }

        startGame(new SinglePlayHandler(connections, playersInformations));
        serverSocket.close();
    }
    
    private static void requestBots(int botsNumber) {
        for (int i = 0; i < botsNumber; i++){
            new Thread(() -> {
                try {
                    new Client(new Bot(botsNumber + 1)).requestGame(LOCALHOST);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

}
