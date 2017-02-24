package Backend.Messages.MessagesToClient;

import Backend.Server.ClientConnection;

public class SendMaxScoreMessage {

    public static void submit(ClientConnection currentConnection) {
        currentConnection.getClientOutput().println("SEND_MAX_SCORE");
    }

}
