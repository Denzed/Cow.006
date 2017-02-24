package com.cow006.gui.tableactivities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.ViewFlipper;

import com.cow006.gui.MainMenuActivity;
import com.cow006.gui.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import Backend.Client.LeaderboardRequester;
import Backend.Database.LeaderboardRecord;

import static java.util.Arrays.asList;

public class LeaderboardActivity extends AppCompatActivity{
    public static final int LEADERBOARD_SIZE = 5;
    List<List<String>> leaderboard; //TODO List<LeaderboardRecord>
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
        //setupLeaderboard();
        Thread t = new Thread(this::setupLeaderboard);
        t.start();
        while (t.getState() != Thread.State.TERMINATED){
            try {
                System.out.println(t.getState());
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }
        System.out.println("HERE I AM");
        Helper.fillTable((GridView) findViewById(R.id.leaderboard_table), leaderboard);
        handler.post(
                ((ViewFlipper) findViewById(
                        R.id.leaderboard_and_loading_viewflipper))::showNext);


    }

    private void setupLeaderboard() {
        try {
            LeaderboardRequester leaderboardRequester = new LeaderboardRequester(LEADERBOARD_SIZE);
            List<LeaderboardRecord> leaderboardAsList = leaderboardRequester.requestLeaderboard();
            leaderboard = new ArrayList<>();
            for (LeaderboardRecord leaderboardRecord : leaderboardAsList){
                System.out.println(leaderboardRecord.getUsername() + "\t" + leaderboardRecord.getRating());
                leaderboard.add(asList(leaderboardRecord.getUsername(), String.valueOf(leaderboardRecord.getRating())));
            }
            // TODO: delete the line after implementation of the above
            //throw new IOException("Placeholder for probable IOException in code below" + "until it is implemented");
            // TODO: uncomment after implementation of the above
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
