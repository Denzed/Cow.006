package com.cow006.gui;

import android.content.Intent;
import android.graphics.Canvas;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.Arrays;

import Backend.AbstractPlayer;
import Backend.Bot;
import Backend.Client;
import Backend.Player;
import Backend.Server;

public class GameActivity extends AppCompatActivity {
    private int bots;
    private int botLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        bots = intent.getIntExtra("Bot count", 5);
        botLevel = intent.getIntExtra("Bot level", 5);
/*        new Thread(new Runnable() {
            public void run()
            {
                try {
                    Server.main(new String[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
*/
    }

    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        GameView gw = (GameView) findViewById(R.id.game_view);
        GameView.LocalPlayer lp = gw.new LocalPlayer(2, 2);

        new Thread(new Runnable() {
            public void run() {
                try {
                    new Client(lp).connectToServer();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        gw.setPlayer(lp);
    }

}
