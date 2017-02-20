package com.cow006.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;

import com.cow006.gui.game.GameActivity;

public class SetupMultiActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_multi);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        SeekBar seekBar = (SeekBar) findViewById(R.id.playerNumberSeekBar);
        seekBar.setOnSeekBarChangeListener(new DisallowZeroSeekBarChangeListener());
        super.onPostCreate(bundle);
    }

    private int getProgressById(int id) {
        return ((SeekBar) findViewById(id)).getProgress();
    }

    public void startGame(View view) {
        startActivity(new Intent(this, GameActivity.class)
                .putExtras(getIntent())
                .putExtra("Player count", getProgressById(R.id.playerNumberSeekBar)));
    }
}
