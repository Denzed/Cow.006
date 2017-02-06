package Backend;

import android.util.Pair;
//import javafx.util.Pair;
import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

import static Backend.Client.ConnectionTypes.*;
import static Backend.GameConstants.*;

public class Client {

    private static final String LOCALHOST = "localhost";
    private static final String MY_LAPTOP_HOST = "192.168.210.110";
    private static final int PORT_NUMBER = 8080;
    private AbstractPlayer connectedPlayer;
    private BufferedReader clientInput = null;
    private PrintWriter clientOutput = null;
    private int playersNumber;
    private ConnectionTypes connectionType;
    private volatile boolean isClosed = false;
    private Socket clientSocket;

    public enum ConnectionTypes { SINGLEPLAYER, MULTIPLAYER, LEADERBOARD }

    public Client(AbstractPlayer connectedPlayer){
        this.connectedPlayer = connectedPlayer;
        playersNumber = this.connectedPlayer.playersNumber;
    }

    public Client(){}

    public BufferedReader getClientInput() {
        return clientInput;
    }


    public void connectToServer(ConnectionTypes connectionType) throws IOException {
        this.connectionType = connectionType;
        clientSocket = new Socket(connectionType == SINGLEPLAYER ? LOCALHOST : MY_LAPTOP_HOST, PORT_NUMBER);
        clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        run();
        if (connectionType != LEADERBOARD) {
            disconnectFromServer();
        }
    }

    public void disconnectFromServer() throws IOException {
        isClosed = true;
        clientSocket.close();
    }

    private void run() throws IOException {
        String messageFromServer;
        ArrayList<Pair<Integer, Integer>> moves = new ArrayList<>();
        while (!isClosed) {
            messageFromServer = clientInput.readLine();
            System.out.println("messageFromServer: " + messageFromServer);
            switch (messageFromServer) {
                case "Connection type":
                    clientOutput.println(connectionType);
                    if (connectionType == LEADERBOARD){
                        isClosed = true;
                    }
                    break;
                case "IsConnected":
                    clientOutput.println(connectedPlayer.isConnected());
                    break;
                case "Players":
                    clientOutput.println(connectedPlayer.playersNumber);
                    break;
                case "Remote":
                    clientOutput.println(connectedPlayer.remoteNumber);
                    break;
                case "Bots":
                    clientOutput.println(connectedPlayer.botsNumber);
                    break;
                case "Username":
                    clientOutput.println(connectedPlayer.username);
                    break;
                case "UserID":
                    clientOutput.println(connectedPlayer.userID);
                    break;
                case "Type":
                    clientOutput.println(connectedPlayer.getClass().getSimpleName());
                    break;
                case "Queue":
                    clientOutput.println(connectedPlayer.getQueue().isEmpty());
                    break;
                case "Clear":
                    connectedPlayer.setBoard(new Board(), new Board());
                    break;
                case "Cards":
                    ArrayList<Integer> hand = new ArrayList<>();
                    for (int i = 0; i < ROUNDS; i++) {
                        hand.add(Integer.parseInt(clientInput.readLine()));
                    }
                    connectedPlayer.setHand(hand);

                    Board board = new Board();
                    Board currentBoard = new Board();

                    for (int i = 0; i < ROWS; i++) {
                        int card = Integer.parseInt(clientInput.readLine());
                        board.add(new ArrayList<>(Collections.singletonList(card)));
                        currentBoard.add(new ArrayList<>(Collections.singletonList(card)));
                    }
                    connectedPlayer.setBoard(board, currentBoard);
                    break;
                case "GameID":
                    connectedPlayer.setId(Integer.parseInt(clientInput.readLine()));
                    break;
                case "Move":
                    int value = connectedPlayer.move();
                    clientOutput.println(value);
                    break;
                case "Min":
                    clientOutput.println(connectedPlayer.getMinOnBoard());
                    break;
                case "Choose":
                    clientOutput.println(connectedPlayer.setChosenRow());
                    break;
                case "Moves":
                    moves = new ArrayList<>();
                    ArrayDeque<Integer> cardsQueue = new ArrayDeque<>();
                    for (int i = 0; i < playersNumber; i++) {
                        int index = Integer.parseInt(clientInput.readLine());
                        int card = Integer.parseInt(clientInput.readLine());
                        moves.add(new Pair<>(index, card));
                        cardsQueue.add(card);
                    }
                    connectedPlayer.setCardsQueue(cardsQueue);
                    break;
                case "Smallest":
                    SmallestTakeTypes smallestTakeType = SmallestTakeTypes.valueOf(clientInput.readLine());
                    int chosenRowIndex = Integer.parseInt(clientInput.readLine());
                    connectedPlayer.playRound(smallestTakeType, chosenRowIndex, moves);
                    break;
                case "Score":
                    clientOutput.println(connectedPlayer.getScore());
                    break;
                case "Scores":
                    for (Integer score : connectedPlayer.getScores()){
                        clientOutput.println(score);
                    }
                    break;
                case "Id":
                    clientOutput.println(connectedPlayer.getId());
                    break;
                case "Results":
                    if (connectionType == SINGLEPLAYER){
                        connectedPlayer.buildFinalResultsSinglePlayer();
                    } else {
                        ArrayList<String> usernames = new ArrayList<>();
                        ArrayList<String> ratings = new ArrayList<>();
                        ArrayList<String> ratingChanges = new ArrayList<>();
                        for (int i = 0; i < playersNumber; i++){
                            usernames.add(clientInput.readLine());
                            ratings.add(clientInput.readLine());
                            ratingChanges.add(clientInput.readLine());
                        }
                        connectedPlayer.buildFinalResultsMultiPlayer(usernames, ratings, ratingChanges);
                    }
                    break;
                case "Disconnected":
                    isClosed = true;
                    connectedPlayer.setGameInterrupted();
                    break;
                case "Game over":
                    isClosed = true;
                    connectedPlayer.setGameFinished();
                    break;
            }
        }
    }
}