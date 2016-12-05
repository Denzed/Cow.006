import Backend.*;

import java.io.IOException;

public class Main {
    public static void main(String[] Args) throws IOException {
        new Client(new Player(4)).connectToServer();
    }
}
