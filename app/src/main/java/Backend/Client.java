package Backend;

import android.util.Pair;

import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

import static Backend.GameConstants.*;

public class Client {

    private static final String LOCALHOST = "localhost";
    private static final String MY_LAPTOP_HOST = "192.168.210.110";

    private static final int PORT_NUMBER = 8080;

    private AbstractPlayer connectedPlayer;
    private BufferedReader clientInput = null;
    private PrintWriter clientOutput = null;
    private final int playersNumber;
    private volatile boolean isClosed = false;
    private Socket clientSocket;
    public enum GameTypes { SINGLEPLAYER, MULTIPLAYER }
    public Client(AbstractPlayer connectedPlayer){
        this.connectedPlayer = connectedPlayer;
        playersNumber = this.connectedPlayer.playersNumber;
    }

    public void connectToServer(GameTypes gameType) throws IOException {
        clientSocket = new Socket(gameType == GameTypes.SINGLEPLAYER ? LOCALHOST : MY_LAPTOP_HOST, PORT_NUMBER);
        clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        run();
        disconnectFromServer();
    }

    public void disconnectFromServer() throws IOException {
        System.out.println("DISCONNECT");
        System.out.println(clientSocket);
        isClosed = true;
        clientSocket.close();
    }

    public void setGameStarted() {
        if (!connectedPlayer.isGameStarted()) {
            connectedPlayer.setGameStarted(true);
        }
    }

    private void run() throws IOException {
        String messageFromServer;
        ArrayList<Pair<Integer, Integer>> moves = new ArrayList<>();
        while (!isClosed) {
            messageFromServer = clientInput.readLine();
            setGameStarted();
            System.out.println("messageFromServer: " + messageFromServer);
            switch (messageFromServer) {
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
                case "Move": {
                    int value = connectedPlayer.move();
                    clientOutput.println(value);
                    connectedPlayer.hand.remove(Integer.valueOf(value));
                    break;
                }
                case "Min": {
                    clientOutput.println(connectedPlayer.getMinOnBoard());
                    break;
                }
                case "Choose": {
                    clientOutput.println(connectedPlayer.setChosenRow());
                    break;
                }
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
                case "Results":
                    ArrayList<String> finalResults = new ArrayList<>();
                    for (int i = 0; i < playersNumber; i++){
                        finalResults.add(clientInput.readLine());
                        System.out.println("Current line: " + finalResults.get(finalResults.size() - 1));
                    }
                    connectedPlayer.setFinalResults(finalResults);
                case "Disconnected":
                    isClosed = true;
                    connectedPlayer.setGameInterrupted(true);
                    break;
                case "Game over":
                    isClosed = true;
                    connectedPlayer.setGameFinished(true);
                    break;
            }
        }
    }
}