package Backend.Messages.MessagesToClient;

import Backend.Server.ClientConnection;

public class IsConnectedMessage {

    public static void submit(ClientConnection connection){
        connection.getClientOutput().println("IS_CONNECTED");
    }

}
