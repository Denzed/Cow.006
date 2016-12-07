package Backend;

import java.io.*;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static Backend.AbstractPlayer.ROUNDS;
import static Backend.AbstractPlayer.ROWS;

public class Client implements Runnable {

    private static final String LOCALHOST = "localhost";
    private static final int PORT_NUMBER = 8080;

    private AbstractPlayer connectedPlayer;
    private BufferedReader clientInput = null;
    private PrintWriter clientOutput = null;
    private final int playersNumber;
    private volatile boolean isClosed = false;

    public Client(AbstractPlayer connectedPlayer){
        this.connectedPlayer = connectedPlayer;
        playersNumber = this.connectedPlayer.playersNumber;
    }

    public synchronized void connectToServer() throws IOException, InterruptedException {
        Socket clientSocket = new Socket(LOCALHOST, PORT_NUMBER);
        clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        Thread tmp = new Thread(this);
        tmp.start();

        while (!isClosed) {}

        tmp.join();

        clientSocket.close();
    }



    public void run() {

        if (isClosed)
            return;
        String messageFromServer = "";

        try {

                while (!isClosed) {
                    messageFromServer = clientInput.readLine();
                    System.out.println("messageFromServer: " + messageFromServer);
//                    System.out.println(connectedPlayer.getQueue().size() + "= size, id = " + connectedPlayer.getId());

                    switch (messageFromServer) {
                        case "Players":
                            clientOutput.println(connectedPlayer.playersNumber);
                            break;
                        case "Type":
                            clientOutput.println(connectedPlayer.getClass().getSimpleName());
                            break;
                        case "Queue":
                            System.out.println(connectedPlayer.getQueue().size() + " id = " + connectedPlayer.getId());
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
                            for (int i = 0; i < ROWS; i++) {
                                board.add(new ArrayList<>(Collections.singletonList(Integer.parseInt(clientInput.readLine()))));
                            }
                            connectedPlayer.setBoard(board);

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
                            boolean smallestTook = Boolean.parseBoolean(clientInput.readLine());
                            int chosenRowIndex = Integer.parseInt(clientInput.readLine());
                            ArrayList<Map.Entry<Integer, Integer>> moves = new ArrayList<>();
                            for (int i = 0; i < playersNumber; i++) {
                                int index = Integer.parseInt(clientInput.readLine());
                                int card = Integer.parseInt(clientInput.readLine());
                                moves.add(new AbstractMap.SimpleEntry<>(index, card));
                            }
                            System.out.println("CLIENT MOVES " + connectedPlayer.getId());

                            connectedPlayer.playRound(smallestTook, chosenRowIndex, moves);
                            System.out.println("CLIENT MOVES DONE " + connectedPlayer.getId());
                            break;
                        case "Score":
                            clientOutput.println(connectedPlayer.getScore());
                            break;
                        case "Game over":
                            isClosed = true;
                            break;
                    }
                }
        } catch(IOException e){
                e.printStackTrace();
        }
    }
}
