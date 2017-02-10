package com.cow006.gui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class FinalScoresActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_scores);
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        fillTable((GridView) findViewById(R.id.final_scores_table),
                  parseScores(getIntent().getStringExtra("finalScores")));
    }

    private void fillTable(GridView table, ArrayList<ArrayList<String>> data) {
        table.setNumColumns(data.get(0).size());
        data.stream()
            .reduce(new ArrayList<>(),
                    (ArrayList<String> a, ArrayList<String> b) -> {
                        a.addAll(b);
                        return a;
                    })
            .forEach((String str) -> {
                TextView tv = new TextView(this);
                tv.setText(str);
                table.addView(tv);
            });
    }

    private ArrayList<ArrayList<String>> parseScores(String scoresString) {
        ArrayList<ArrayList<String>> scores = new ArrayList<>();
        for (String lineString: scoresString.split("\n")) {
            ArrayList<String> line = new ArrayList<>();
            line.addAll(Arrays.asList(lineString.split("\n")));
            scores.add(line);
        }
        return scores;
    }

    public void goToMainMenu(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
        startActivity(returnIntent);
        super.onBackPressed();
    }
}
