package com.cow006.gui.game;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.cow006.gui.MainMenuActivity;
import com.cow006.gui.R;
import com.cow006.gui.tableactivities.FinalScoresActivity;

import java.io.IOException;

import Backend.Client.GameClient;
import Backend.Player.PlayerInformation;
import Backend.Server.SinglePlayServer;

import static Backend.Client.Client.GAME_PORT_NUMBER;
import static Backend.Client.Client.LOCALHOST;
import static Backend.Client.Client.MY_LAPTOP_HOST;

public class GameActivity extends AppCompatActivity {
    private int players;
    private int bots;
    private GameClient localClient;
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
                //ignore
            }
        }).start();
    }

    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);

        GameView gw = (GameView) findViewById(R.id.game_view);

        final LocalPlayer lp = new LocalPlayer(gw, players + bots + 1, new PlayerInformation(username, userID));
        gw.setPlayer(lp);

        localClient = new GameClient(lp);
        new Thread(() -> {
            try {
                localClient.requestGame(players == 0 ? LOCALHOST : MY_LAPTOP_HOST, GAME_PORT_NUMBER);
            } catch (IOException e) {
                //ignore
            }
        }).start();
    }


    @Override
    public void onBackPressed() {

        try {
            localClient.disconnectFromServer();
        } catch (Exception e) {
            //ignore
        }
        super.onBackPressed();
    }

    public void goToResults(String finalScores) {
        Intent intent = new Intent(this, FinalScoresActivity.class);
        intent.putExtra("finalScores", finalScores);
        startActivity(intent);
        finish();
    }

    public void goToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}
