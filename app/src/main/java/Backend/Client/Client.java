package Backend.Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import Backend.Messages.MessagesToClient.BuildFinalResultsMessages.BuildSinglePlayFinalResultsMessage;
import Backend.Messages.MessagesToServer.ResultsBuiltMessage.SinglePlayFinalResultsBuiltMessage;
import Backend.Player.AbstractPlayer;
import Backend.Messages.MessagesToClient.*;
import Backend.Messages.MessagesToServer.*;
import Backend.Player.Player;

import static java.util.Collections.max;

public class Client {

    public static final String LOCALHOST = "localhost";
    public static final String MY_LAPTOP_HOST = "192.168.210.110";
    public static final int PORT_NUMBER = 8080;
    public AbstractPlayer connectedPlayer;
    protected Socket clientSocket;
    protected BufferedReader clientInput;
    protected PrintWriter clientOutput;
    private volatile boolean isClosed = false;

    public Client(AbstractPlayer connectedPlayer){
        this.connectedPlayer = connectedPlayer;
    }

    public void requestGame(String host) throws IOException {
        connectToServer(host);
        if (connectedPlayer instanceof Player) {
            PlayersNumberMessage.submit(this, connectedPlayer.getPlayersNumber());
        }
        PlayerInformationMessage.submit(this, connectedPlayer.getPlayerInformation());
        recieveAndSubmitMessagesAboutGameSession();
    }

    public void connectToServer(String host) throws IOException {
        clientSocket = new Socket(host, PORT_NUMBER);
        clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        while (clientInput == null || clientOutput == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }
    }

    public BufferedReader getClientInput() {
        return clientInput;
    }

    public PrintWriter getClientOutput() {
        return clientOutput;
    }

    public void disconnectFromServer() throws IOException {
        isClosed = true;
        clientSocket.close();
    }

    protected void recieveAndSubmitMessagesAboutGameSession() throws IOException {
        String messageType;
        while (!isClosed) {
            messageType = clientInput.readLine();
            System.out.println("Thread: " + Thread.currentThread().getName() + "\tmessageType: " + messageType);
            switch (messageType) {
                case "GAME_STARTED":
                    GameStartedMessage.receive(this);
                    break;
                case "DEAL_STARTED":
                    DealStartedMessage.receive(this);
                    break;
                case "SEND_CARD":
                    CardSelectedMessage.submit(this, connectedPlayer.chooseCard());
                    break;
                case "CURRENT_ROUND":
                    CurrentRoundMessage.receive(this);
                    break;
                case "SEND_ROW":
                    RowSelectedMessage.submit(this, connectedPlayer.chooseRow());
                    break;
                case "SMALLEST_CARD_TURN":
                    SmallestCardTurnMessage.receive(this);
                    break;
                case "SEND_MAX_SCORE":
                    MaxScoreSentMessage.submit(this, max(connectedPlayer.getScores()));
                    break;
                case "GAME_FINISHED":
                    isClosed = true;
                    GameFinishedMessage.receive(this);
                    break;
               case "BUILD_SINGLE_PLAY_FINAL_RESULTS":
                   BuildSinglePlayFinalResultsMessage.receive(this);
                   SinglePlayFinalResultsBuiltMessage.submit(this);
                   break;
            }
        }
    }

}