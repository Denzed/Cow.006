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

public class LeaderboardActivity extends AppCompatActivity {
    public static final int LEADERBOARD_SIZE = 100;
    List<List<String>> table = new ArrayList<>();
    Handler handler = new Handler();

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
        AsyncTask.execute(this::setupLeaderboard);
    }

    private void setupLeaderboard() {
        boolean isSuccess[] = {false}; // effectively final hack
        try {
            System.out.println("HERE");
            table.add(new ArrayList<>(Arrays.asList("Name", "Rating")));
            System.out.println(table.size());

            LeaderboardRequester leaderboardRequester = new LeaderboardRequester(LEADERBOARD_SIZE);
            for (LeaderboardRecord record : leaderboardRequester.requestLeaderboard()) {
                table.add(
                        Arrays.asList(record.getUsername(),
                                Integer.toString(record.getRating())));
            }
            isSuccess[0] = true;
        } catch (IOException e) {
            isSuccess[0] = true; // TODO: delete when leaderboard is okay
            e.printStackTrace();
        }
        handler.post(() -> {
            ViewFlipper flipper =
                    (ViewFlipper) findViewById(R.id.leaderboard_and_loading_viewflipper);
            flipper.setDisplayedChild(flipper.indexOfChild(
                    flipper.findViewById(isSuccess[0]
                            ? R.id.leaderboard_table
                            : R.id.no_connection_text_view)));
            Helper.fillTable((GridView) findViewById(R.id.leaderboard_table), table);
        });
    }
}
