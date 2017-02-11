package com.cow006.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import Backend.Client;


public class LeaderboardActivity extends AppCompatActivity{
    String leaderboard = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
    }

    @Override
    protected  void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        Thread t = new Thread(() -> {
            System.out.println("LEADERBOARD:\n" + leaderboard);
            try {
                Client client = new Client();
                client.connectToServer(Client.ConnectionTypes.LEADERBOARD);

                for (int i = 0; i < 10; i++) {
                    String line = client.getClientInput().readLine();
                    System.out.println(line);
                    leaderboard += line + ((i % 2 == 1) ? "\n" : "\t");
                }
                client.disconnectFromServer();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
        while (t.getState() != Thread.State.TERMINATED) {
            try {
                System.out.println(t.getState());
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }

        ((TextView) findViewById(R.id.leaderboard_text_view)).setText(leaderboard);
    }

}
