package com.cow006.gui;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
    }

    @Override
    protected  void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        String leaderboard = "No connection to server!";
        // query to database for leaderboard

        // update string

        // post to the TextView
        ((TextView) findViewById(R.id.leaderboard_text_view)).setText(leaderboard);
    }
}
