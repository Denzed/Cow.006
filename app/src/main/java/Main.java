import Backend.*;

import java.io.IOException;

public class Main {
    public static void main(String[] Args){
        new Thread(() -> new Client(new Player()).connectToServer()).start();
    }
}
