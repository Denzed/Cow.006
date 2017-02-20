package com.cow006.gui.tableactivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;

import com.cow006.gui.MainMenuActivity;
import com.cow006.gui.R;


public class FinalScoresActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_scores);
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        Helper.fillTable((GridView) findViewById(R.id.final_scores_table),
                Helper.parseStringToTable(getIntent().getStringExtra("finalScores")));
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
        startActivity(returnIntent);
        super.onBackPressed();
    }
}
