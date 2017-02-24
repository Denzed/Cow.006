package com.cow006.gui.tableactivities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.cow006.gui.MainMenuActivity;
import com.cow006.gui.R;

import java.io.IOException;
import java.util.List;

import Backend.Client.LeaderboardRequester;
import Backend.Database.LeaderboardRecord;

public class LeaderboardActivity extends AppCompatActivity{
    public static final int LEADERBOARD_SIZE = 100;
    List<List<String>> leaderboard; //TODO List<LeaderboardRecord>
    Handler handler = new Handler(); //TODO Really need this???


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
        startActivity(returnIntent);
        super.onBackPressed();
    }

    @Override
    protected  void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        new Thread(this::setupLeaderboard).run();
    }

    private void setupLeaderboard() {
        try {
            LeaderboardRequester leaderboardRequester = new LeaderboardRequester(LEADERBOARD_SIZE);
            List<LeaderboardRecord> leaderboardAsList = leaderboardRequester.requestLeaderboard();
            // TODO: delete the line after implementation of the above
            throw new IOException("Placeholder for probable IOException in code below" +
                    "until it is implemented");
            /* TODO: uncomment after implementation of the above
            Helper.fillTable((GridView) findViewById(R.id.leaderboard_table), leaderboard);
            handler.post(
                    ((ViewFlipper) findViewById(
                            R.id.leaderboard_and_loading_viewflipper))::showNext);
            */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
