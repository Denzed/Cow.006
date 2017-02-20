package com.cow006.gui.game;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.cow006.gui.R;
import com.cow006.gui.tableactivities.FinalScoresActivity;

import java.io.IOException;

import Backend.Client.Client;
import Backend.Player.PlayerInformation;
import Backend.Server.SinglePlayServer;

import static Backend.Client.Client.LOCALHOST;
import static Backend.Client.Client.MY_LAPTOP_HOST;

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
        botLevel = intent.getIntExtra("Bot level", 5);
        username = intent.getStringExtra("username");
        userID = intent.getStringExtra("userID");
        if (players == 0) {
            startSingleGameServer();
        }
    }

    private void startSingleGameServer() {
        new Thread(() -> {
            try {
                SinglePlayServer.main(new String[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);

        GameView gw = (GameView) findViewById(R.id.game_view);

        final LocalPlayer lp = new LocalPlayer(gw, players + bots + 1, new PlayerInformation(username, userID));
        System.out.println("PLAYERS = " + players + " BOTS = " + bots + " PARAMETER = " + (players + bots + 1));
        gw.setPlayer(lp);

        localClient = new Client(lp);
        new Thread(() -> {
            try {
                localClient.requestGame(players == 0 ? LOCALHOST : MY_LAPTOP_HOST);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
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
