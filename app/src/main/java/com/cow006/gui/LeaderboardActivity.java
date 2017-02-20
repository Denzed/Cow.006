package com.cow006.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.IOException;

import Backend.Client.Client;

public class LeaderboardActivity extends AppCompatActivity{
    public static final int LEADERBOARD_SIZE = 100;
    String leaderboard = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
    }

    @Override
    protected  void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        new Thread(() -> {
            try {
                getLeaderboard();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getLeaderboard() throws IOException {
        //TODO: implement this
        /*        Client client = new Client();
        client.connectToServer(Client.ConnectionTypes.LEADERBOARD);
        for (int i = 0; i < LEADERBOARD_SIZE; i++) {
            String line = client.getClientInput().readLine();
            leaderboard += line + ((i % 2 == 1) ? "\n" : "\t");
        }
        client.disconnectFromServer();
*/        runOnUiThread(() ->
                ((TextView) findViewById(R.id.leaderboard_text_view)).setText(leaderboard));
    }
}
