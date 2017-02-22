package Backend.Client;

import java.io.*;
import java.net.Socket;

import Backend.Messages.MessagesToClient.BuildFinalResultsMessages.*;
import Backend.Messages.MessagesToServer.ResultsBuiltMessage.*;
import Backend.Messages.MessagesToClient.*;
import Backend.Messages.MessagesToServer.*;
import Backend.Player.*;

import static java.util.Collections.max;

public class Client {

    public static final String LOCALHOST = "localhost";
    public static final String MY_LAPTOP_HOST = "192.168.210.110";
    public static final int PORT_NUMBER = 8080;

    private AbstractPlayer connectedPlayer;
    private Socket clientSocket;
    private BufferedReader clientInput;
    private PrintWriter clientOutput;
    private volatile boolean isClosed;

    public Client(AbstractPlayer connectedPlayer){
        this.connectedPlayer = connectedPlayer;
        isClosed = true;
    }

    public void requestGame(String host) throws IOException {
        connectToServer(host);
        if (connectedPlayer instanceof Player) {
            PlayersNumberMessage.submit(this, connectedPlayer.getPlayersNumber());
        }
        PlayerInformationMessage.submit(this, connectedPlayer.getPlayerInformation());
        isClosed = false;
        receiveAndSubmitMessagesAboutGameSession();
    }

    private void connectToServer(String host) throws IOException {
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

    private void receiveAndSubmitMessagesAboutGameSession() throws IOException {
        String messageType;
        while (!isClosed) {
            messageType = clientInput.readLine();
            System.out.println("Thread: " + Thread.currentThread().getName() + "\tmessageType: " + messageType);
            switch (messageType) {
                case "BUILD_MULTI_PLAY_FINAL_RESULTS":
                    BuildMultiPlayFinalResultsMessage.receive(this);
                    MultiPlayFinalResultsBuiltMessage.submit(this);
                    break;
                case "BUILD_SINGLE_PLAY_FINAL_RESULTS":
                    BuildSinglePlayFinalResultsMessage.receive(this);
                    SinglePlayFinalResultsBuiltMessage.submit(this);
                    break;
                case "CURRENT_ROUND":
                    CurrentRoundMessage.receive(this);
                    break;
                case "DEAL_STARTED":
                    DealStartedMessage.receive(this);
                    break;
                case "GAME_FINISHED":
                    isClosed = true;
                    GameFinishedMessage.receive(this);
                    break;
                case "GAME_STARTED":
                    GameStartedMessage.receive(this);
                    break;
                case "IS_CONNECTED":
                    IAmConnectedMessage.submit(this);
                case "SEND_CARD":
                    CardSelectedMessage.submit(this, connectedPlayer.chooseCard());
                    break;
                case "SEND_MAX_SCORE":
                    MaxScoreSentMessage.submit(this, max(connectedPlayer.getScores()));
                    break;
                case "SEND_ROW":
                    RowSelectedMessage.submit(this, connectedPlayer.chooseRow());
                    break;
                case "SEND_SCORES":
                    ScoresSentMessage.submit(this, connectedPlayer.getScores());
                    break;
                case "SMALLEST_CARD_TURN":
                    SmallestCardTurnMessage.receive(this);
                    break;
            }
        }
    }

    public void disconnectFromServer() throws IOException {
        isClosed = true;
        clientSocket.close();
    }

    public AbstractPlayer getConnectedPlayer() {
        return connectedPlayer;
    }

    public BufferedReader getClientInput() {
        return clientInput;
    }

    public PrintWriter getClientOutput() {
        return clientOutput;
    }


}