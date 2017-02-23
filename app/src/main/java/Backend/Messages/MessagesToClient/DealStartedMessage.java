package Backend.Messages.MessagesToClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Backend.Client.GameClient;
import Backend.Game.Board;
import Backend.Game.Row;
import Backend.Server.ClientConnection;

import static Backend.Game.GameConstants.ROUNDS;
import static Backend.Game.GameConstants.ROWS;
import static java.lang.Integer.parseInt;

public class DealStartedMessage {

    public static void submit(ClientConnection connection, List<Integer> hand, Board board){
        connection.getClientOutput().println("DEAL_STARTED");
        for (Integer card : hand){
            connection.getClientOutput().println(card);
        }
        for (List<Integer> row : board){
            for (Integer card : row){
                connection.getClientOutput().println(card);
            }
        }
    }

    public static void receive(GameClient client) throws IOException {
        while (!client.getConnectedPlayer().getCardsQueue().isEmpty()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }

        List<Integer> hand = new ArrayList<>();
        for (int i = 0; i < ROUNDS; i++){
            hand.add(parseInt(client.getClientInput().readLine()));
        }

        Board board = new Board();
        Board currentBoard = new Board();
        for (int i = 0; i < ROWS; i++){
            int card = parseInt(client.getClientInput().readLine());
            Row row = new Row();
            row.add(card);
            board.add(row);

            row = new Row();
            row.add(card);
            currentBoard.add(row);
        }

        client.getConnectedPlayer().setHand(hand);
        client.getConnectedPlayer().setBoard(board);
        client.getConnectedPlayer().setCurrentBoard(currentBoard);
    }

}
