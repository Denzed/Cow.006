import Backend.*;

public class Main {
    public static void main(String[] Args) throws Exception {
        final int opponents = 3;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        new Client(new Player(1 + opponents)).connectToServer();
                        break;
                    } catch (Exception e) {

                    }
                }

            }
        }).start();

        for (int i = 0; i < opponents; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        new Client(new Bot(1 + opponents)).connectToServer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }

    }
}
