package Backend.Messages.MessagesToClient;

import Backend.Server.ClientConnection;

public class SendScoresMessage {

    public static void submit(ClientConnection connection){
        connection.getClientOutput().println("SEND_SCORES");
    }

}

