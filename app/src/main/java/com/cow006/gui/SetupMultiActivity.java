package com.cow006.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;

public class SetupMultiActivity extends AppCompatActivity {
    private int TOTAL_PLAYERS = 10;

    private class UpdateSum implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int players = getProgressById(R.id.playerNumberSeekBar),
                bots = getProgressById(R.id.botNumberSeekBar);
            System.out.println("SETUP PLAYERS = " + players + " BOTS = " + bots);
            if (players + bots + 1 > TOTAL_PLAYERS) {
                if (seekBar == findViewById(R.id.playerNumberSeekBar)) {
                    int newProgress = TOTAL_PLAYERS - 1 - players;
                    ((SeekBar) findViewById(R.id.botNumberSeekBar)).setProgress(newProgress);
                } else if (seekBar == findViewById(R.id.botNumberSeekBar)) {
                    int newProgress = TOTAL_PLAYERS - 1 - bots;
                    ((SeekBar) findViewById(R.id.playerNumberSeekBar)).setProgress(newProgress);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_multi);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        UpdateSum changeListener = new UpdateSum();
        SeekBar sb = (SeekBar) findViewById(R.id.playerNumberSeekBar);
        if (sb != null) {
            sb.setOnSeekBarChangeListener(changeListener);
        }
        sb = (SeekBar) findViewById(R.id.botNumberSeekBar);
        if (sb != null) {
            sb.setOnSeekBarChangeListener(changeListener);
        }

    }

    private int getProgressById(int id) {
        return ((SeekBar) findViewById(id)).getProgress();
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("Player count", getProgressById(R.id.playerNumberSeekBar));
        intent.putExtra("Bot count", getProgressById(R.id.botNumberSeekBar));
        intent.putExtra("Bot level", getProgressById(R.id.botLevelSeekBar));
        startActivity(intent);
    }
}
