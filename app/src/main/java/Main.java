import Backend.*;

import java.io.IOException;

public class Main {
    public static void main(String[] Args){
        new Thread(() -> {
            try {
                new Client(new Player(4)).connectToServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
