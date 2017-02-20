package Backend.Messages.MessagesToClient;

import java.io.IOException;

import Backend.Messages.MessagesToServer.RowSelectedMessage;
import Backend.Server.ClientConnection;

import static java.lang.Integer.parseInt;

public class SendRowMessage {

    public static void submit(ClientConnection connection) throws IOException {
        connection.getClientOutput().println("SEND_ROW");
    }

}
