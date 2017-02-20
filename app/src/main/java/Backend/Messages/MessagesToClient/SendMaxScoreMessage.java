package Backend.Messages.MessagesToClient;

import java.io.IOException;

import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class SendMaxScoreMessage {
    public static void submit(ClientConnection currentConnection) {
        currentConnection.getClientOutput().println("SEND_MAX_SCORE");
    }

}
