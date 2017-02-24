package com.cow006.gui.tableactivities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.ViewFlipper;

import com.cow006.gui.MainMenuActivity;
import com.cow006.gui.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

public class LeaderboardActivity extends AppCompatActivity{
    public static final int LEADERBOARD_SIZE = 100;
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
        List<List<String>> table = new ArrayList<>();
        Thread t = new Thread(() -> setupLeaderboard(table));
        t.start();
        while (t.getState() != Thread.State.TERMINATED){
            try {
                sleep(100);
            } catch (InterruptedException e) {
                //
            }
        }
        Helper.fillTable((GridView) findViewById(R.id.leaderboard_table), table);
        handler.post(
                ((ViewFlipper) findViewById(
                        R.id.leaderboard_and_loading_viewflipper))::showNext);

    }

    private void setupLeaderboard(List<List<String>> table) {
        //try {
            System.out.println("HERE");
//            LeaderboardRequester leaderboardRequester = new LeaderboardRequester(LEADERBOARD_SIZE);
            table.add(new ArrayList<>(Arrays.asList("Name", "Rating")));
            table.add(new ArrayList<>(Arrays.asList("USER1", "100500")));
            table.add(new ArrayList<>(Arrays.asList("USER2", "100000")));
            table.add(new ArrayList<>(Arrays.asList("USER3", "100")));
        System.out.println(table.size());

            /*            for (LeaderboardRecord record : leaderboardRequester.requestLeaderboard()) {
                table.add(
                        Arrays.asList(record.getUsername(),
                                Integer.toString(record.getRating())));
            }
*/
        /*} catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
