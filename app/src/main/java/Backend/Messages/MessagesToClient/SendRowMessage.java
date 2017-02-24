package Backend.Messages.MessagesToClient;

import java.io.IOException;

import Backend.Server.ClientConnection;

public class SendRowMessage {

    public static void submit(ClientConnection connection) throws IOException {
        connection.getClientOutput().println("SEND_ROW");
    }

}
