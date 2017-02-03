package com.cow006.gui;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.IOException;
import Backend.Bot;
import Backend.Client;
import Backend.Server;

public class GameActivity extends AppCompatActivity {
    private int players;
    private int bots;
    private int botLevel;
    private Client localClient;
    private String username;
    private String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        players = intent.getIntExtra("Player count", 0);
        bots = intent.getIntExtra("Bot count", 0);
        System.out.println("PLAYERS = " + players + " BOTS = " + bots);
        botLevel = intent.getIntExtra("Bot level", 5);
        username = intent.getStringExtra("username");
        userID = intent.getStringExtra("userID");
        if (players == 0){
            new Thread(() -> {
                    try {
                        System.out.println("SERVER CREATED");
                        Server.main(new String[0]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
        }
        else{
            username = intent.getStringExtra("username");
            userID = intent.getStringExtra("userID");
            System.out.println("username = " + username + ";userID = " + userID);
        }
    }

    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);

        GameView gw = (GameView) findViewById(R.id.game_view);
        final GameView.LocalPlayer lp;
        if (players == 0){
            lp = gw.new LocalPlayer(players + 1, bots);
        }
        else{
            lp = gw.new LocalPlayer(players + 1, bots, username, userID);
        }
        localClient = new Client(lp);
        if (players == 0) {
            new Thread(() -> {
                    try {
                        localClient.connectToServer(Client.GameTypes.SINGLEPLAYER);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            for (int i = 0; i < bots; i++) {
                new Thread(() -> {
                        try {
                            new Client(new Bot(1, bots)).connectToServer(Client.GameTypes.SINGLEPLAYER);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
            }
        } else {
            new Thread(() -> {
                    try {
                        System.out.println("username = " + username + ";userID = " + userID);
                        localClient.connectToServer(Client.GameTypes.MULTIPLAYER);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
        }

        gw.setPlayer(lp);
    }

    @Override
    public void onBackPressed() {
        System.out.println("USER LEFT THE GAME!");
        // do something useful
        try {
            localClient.disconnectFromServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    public void goToResults(String finalScores) {
        Intent intent = new Intent(this, FinalScoresActivity.class);
        intent.putExtra("finalScores", finalScores);
        startActivity(intent);
        finish();
    }
}
