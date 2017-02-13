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
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        SeekBar seekBar = (SeekBar) findViewById(R.id.playerNumberSeekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(Math.max(seekBar.getProgress(), 1));
            }
        });
        super.onPostCreate(bundle);
    }

    private int getProgressById(int id) {
        return ((SeekBar) findViewById(id)).getProgress();
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra("Player count", getProgressById(R.id.playerNumberSeekBar));
        startActivity(intent);
    }
}
