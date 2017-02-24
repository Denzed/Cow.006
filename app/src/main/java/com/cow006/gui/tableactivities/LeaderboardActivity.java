package com.cow006.gui.tableactivities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.ViewFlipper;

import com.cow006.gui.MainMenuActivity;
import com.cow006.gui.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Backend.Client.LeaderboardRequester;
import Backend.Database.LeaderboardRecord;

public class LeaderboardActivity extends AppCompatActivity{
    public static final int LEADERBOARD_SIZE = 100;
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
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                setupLeaderboard();
                return null;
            }
        }.execute();
    }

    private void setupLeaderboard() {
        try {
            LeaderboardRequester leaderboardRequester = new LeaderboardRequester(LEADERBOARD_SIZE);
            List<List<String>> table = new ArrayList<>();
            table.add(Arrays.asList("Name", "Rating"));
            for (LeaderboardRecord record : leaderboardRequester.requestLeaderboard()) {
                table.add(
                        Arrays.asList(record.getUsername(),
                                Integer.toString(record.getRating())));
            }
            Helper.fillTable((GridView) findViewById(R.id.leaderboard_table), table);
            handler.post(
                    ((ViewFlipper) findViewById(
                            R.id.leaderboard_and_loading_viewflipper))::showNext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
