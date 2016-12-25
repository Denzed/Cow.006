package Backend;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.PriorityQueue;

import static Backend.AbstractPlayer.ROUNDS;
import static Backend.AbstractPlayer.ROWS;

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
    public enum gameTypes { SINGLEPLAYER, MULTIPLAYER }
    public Client(AbstractPlayer connectedPlayer){
        this.connectedPlayer = connectedPlayer;
        playersNumber = this.connectedPlayer.playersNumber;
    }

    public void connectToServer(gameTypes gameType) throws IOException {
        clientSocket = new Socket(gameType == gameTypes.SINGLEPLAYER ? LOCALHOST : MY_LAPTOP_HOST, PORT_NUMBER);
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

    private void run() throws IOException {
        String messageFromServer;
        ArrayList<Map.Entry<Integer, Integer>> moves = new ArrayList<>();
        while (!isClosed) {
            messageFromServer = clientInput.readLine();
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
                case "Type":
                    clientOutput.println(connectedPlayer.getClass().getSimpleName());
                    break;
                case "Queue":
                    clientOutput.println(connectedPlayer.getQueue().isEmpty());
                    break;
                case "Clear":
                    connectedPlayer.board.clear();
                    break;
                case "Cards":
                    ArrayList<Integer> hand = new ArrayList<>();
                    for (int i = 0; i < ROUNDS; i++) {
                        hand.add(Integer.parseInt(clientInput.readLine()));
                    }
                    connectedPlayer.setHand(hand);

                    ArrayList<ArrayList<Integer>> board = new ArrayList<>();
                    ArrayList<ArrayList<Integer>> currentBoard = new ArrayList<>();

                    for (int i = 0; i < ROWS; i++) {
                        int card = Integer.parseInt(clientInput.readLine());
                        board.add(new ArrayList<>(Collections.singletonList(card)));
                        currentBoard.add(new ArrayList<>(Collections.singletonList(card)));
                    }
                    connectedPlayer.setBoard(board, currentBoard);

                    int id = Integer.parseInt(clientInput.readLine());
                    connectedPlayer.setId(id);
                    break;
                case "Move": {
                    clientOutput.println(connectedPlayer.tellMove());
                    break;
                }
                case "Min": {
                    clientOutput.println(connectedPlayer.getMinOnBoard());
                    break;
                }
                case "Choose": {
                    clientOutput.println(connectedPlayer.tellChosenRow());
                    break;
                }
                case "Moves":
                    moves = new ArrayList<>();
                    ArrayDeque<Integer> cardsQueue = new ArrayDeque<>();
                    for (int i = 0; i < playersNumber; i++) {
                        int index = Integer.parseInt(clientInput.readLine());
                        int card = Integer.parseInt(clientInput.readLine());
                        moves.add(new AbstractMap.SimpleEntry<>(index, card));
                        cardsQueue.add(card);
                    }
                    connectedPlayer.setCardsQueue(cardsQueue);
                    break;
                case "Smallest":
                    boolean smallestTook = Boolean.parseBoolean(clientInput.readLine());
                    int chosenRowIndex = Integer.parseInt(clientInput.readLine());
                    connectedPlayer.playRound(smallestTook, chosenRowIndex, moves);
                    break;
                case "Score":
                    clientOutput.println(connectedPlayer.getScore());
                    break;
                case "Game over":
                    isClosed = true;
                    connectedPlayer.setGameFinished(true);
                    break;
            }
        }
    }
}