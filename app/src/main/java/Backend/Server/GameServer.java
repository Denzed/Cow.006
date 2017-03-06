package Backend.Server;

import Backend.GameHandler.GameHandler;
import Backend.Messages.MessagesToClient.GameFinishedMessage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

abstract class GameServer {

    static void startGame(GameHandler gameHandler) {
        new Thread(() -> {
            try {
                gameHandler.playGame();
            } catch (Exception e) {
                GameFinishedMessage.submitAll(
                        gameHandler.getConnections(), GameHandler.GameFinishedReasons.SOMEONE_HAS_DISCONNECTED);
            }
        }).start();
    }

}
