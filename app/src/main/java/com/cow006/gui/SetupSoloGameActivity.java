package com.cow006.gui;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;

public class SetupSoloGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_solo_game);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra("Player count", 0);
        intent.putExtra("Bot count", ((SeekBar) findViewById(R.id.botNumberSeekBar)).getProgress());
        intent.putExtra("Bot level", ((SeekBar) findViewById(R.id.botLevelSeekBar)).getProgress());
        startActivity(intent);
    }
}
