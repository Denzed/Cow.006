package Backend.Messages.MessagesToClient;

import Backend.Server.ClientConnection;

public class SendCardMessage {

    public static void submit(ClientConnection connection){
        connection.getClientOutput().println("SEND_CARD");
    }

}
