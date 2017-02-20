package com.cow006.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;

import com.cow006.gui.game.GameActivity;

public class SetupSoloGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_solo_game);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        SeekBar seekBar = (SeekBar) findViewById(R.id.botNumberSeekBar);
        seekBar.setOnSeekBarChangeListener(new DisallowZeroSeekBarChangeListener());
        super.onPostCreate(bundle);
    }


    public void startGame(View view) {
        startActivity(new Intent(this, GameActivity.class)
                .putExtras(getIntent())
                .putExtra("Player count", 0)
                .putExtra("Bot count",
                        ((SeekBar) findViewById(R.id.botNumberSeekBar)).getProgress())
                .putExtra("Bot level",
                        ((SeekBar) findViewById(R.id.botLevelSeekBar)).getProgress()));
    }
}
