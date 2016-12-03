package Backend;

import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

public class Client implements Runnable {

    String host = "localhost";
    int portNumber = 2222;

    private AbstractPlayer connectedPlayer;
    public Client(AbstractPlayer connectedPlayer){
        this.connectedPlayer = connectedPlayer;
    }
    private Socket clientSocket = null;
    private BufferedReader clientInput = null;
    private PrintWriter clientOutput = null;

    boolean isClosed = false;

    public void connectToServer() {
        System.out.println("CONNECTING");
        try {
            clientSocket = new Socket(host, portNumber);
            clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (clientSocket != null && clientInput != null && clientOutput != null) {
            try {
                new Thread(this).start();
                while (!isClosed) {}

                clientInput.close();
                clientOutput.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("IOException: " + e);
                e.printStackTrace();

            }
        }

    }

    public void run() {

        String messageFromServer = "";

        try {
            while (!(messageFromServer = clientInput.readLine()).equals("Game over")) {
                System.out.println("messageFromServer: " + messageFromServer);
                if (messageFromServer.equals("Type")){
                    System.out.println(connectedPlayer.getClass().getSimpleName());
                    clientOutput.println(connectedPlayer.getClass().getSimpleName());
                } else if (messageFromServer.equals("Cards")){
                        System.out.println("SERVER WANTS DO DEAL US THE CARDS");
                    ArrayList<Integer> hand = new ArrayList<>();
                    for (int i = 0; i < 10; i++){
                        hand.add(Integer.parseInt(clientInput.readLine()));
                    }
                    connectedPlayer.setHand(hand);

                    ArrayList<ArrayList<Integer>> board = new ArrayList<>();
                    for (int i = 0; i < 4; i++){
                        board.add(new ArrayList<>(Collections.singletonList(Integer.parseInt(clientInput.readLine()))));
                    }
                    connectedPlayer.setBoard(board);
                    System.out.println("BOARD:");
                    for (ArrayList<Integer> al : board){
                        for (Integer x : al){
                            System.out.print(x + " ");
                        }
                        System.out.println();
                    }
                } else if (messageFromServer.equals("Move")){
//                    System.out.println(connectedPlayer.tellMove());
                    int x = connectedPlayer.tellMove();
                    clientOutput.println(x);
                } else if (messageFromServer.equals("Min")){
//                    System.out.println(connectedPlayer.getMinOnBoard());
                    int x = connectedPlayer.getMinOnBoard();
                    clientOutput.println(x);

                } else if (messageFromServer.equals("Choose")){
//                    System.out.println(connectedPlayer.tellChosenRow());
                    int x = connectedPlayer.tellChosenRow();
                    clientOutput.println(x);
                } else if (messageFromServer.equals("Moves")){
                    boolean smallestTook = Boolean.parseBoolean(clientInput.readLine());
                    System.out.println("smallestTook = " + smallestTook);
                    int chosenRowIndex = Integer.parseInt(clientInput.readLine());
                    System.out.println("chosenRowIndex = " + chosenRowIndex);
                    ArrayList<Pair<Integer, Integer>> moves = new ArrayList<>();

                    for (int i = 0; i < 4; i++){
                        int index = Integer.parseInt(clientInput.readLine());
                        int card = Integer.parseInt(clientInput.readLine());
                        moves.add(new Pair<>(index, card));
                    }
                    connectedPlayer.playRound(smallestTook, chosenRowIndex, moves);
                }
                System.out.println("Iterated in Client.run()");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        isClosed = true;
    }
}