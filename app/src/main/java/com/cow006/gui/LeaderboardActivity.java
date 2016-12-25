package com.cow006.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
    }

    @Override
    protected  void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        String leaderboard = "";
        // query to database for leaderboard
//        String username = getIntent().getStringExtra("username");
/*        final Connection dataBaseConnection;
        try {
            dataBaseConnection = DriverManager.getConnection(
                    "jdbc:mysql://sql7.freemysqlhosting.net:3306/sql7150701", "sql7150701", SECRET_PASSWORD);
            String query = "SELECT username, rating FROM sql7150701.Information ORDER BY rating DESC LIMIT 5";
            System.out.println("query = " + query);
            final Statement statement = dataBaseConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            ArrayList<String> usernames = new ArrayList<>();
            ArrayList<Integer> ratings = new ArrayList<>();
            while (resultSet.next()){
                usernames.add(resultSet.getString("username"));
                ratings.add(resultSet.getInt("rating"));
            }
            statement.close();
            dataBaseConnection.close();
            for (int i = 1; i <= usernames.size(); i++){
                leaderboard += i;
                leaderboard += " " + usernames.get(i);
                leaderboard += " " + ratings.get(i);
                leaderboard += "\n";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
*/
        // update string

        // post to the TextView
        ((TextView) findViewById(R.id.leaderboard_text_view)).setText(leaderboard);
    }
}
