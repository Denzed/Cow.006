package com.cow006.gui;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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
        setContentView(R.layout.game_view);

        Intent intent = getIntent();
        bots = intent.getIntExtra("Bot count", 5);
        botLevel = intent.getIntExtra("Bot level", 5);

        // TODO: launch game handler and get information from it
    }
}
