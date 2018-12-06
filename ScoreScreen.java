package com.example.jayantmehra.quizzly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableRow;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.HashMap;

public class ScoreScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerStanding = (TextView) findViewById(R.id.textView);
        String s = String.format("You came in %d", 5);
        playerStanding.setText(s);
    }

    /*  Member Variables */

    private TextView playerStanding;
    private Boolean isOver;
    private ArrayList<HashMap<String, String>> rankings;
}
