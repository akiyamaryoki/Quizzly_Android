package com.example.joshkimmel.quizzly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

    }
    protected void submituser()
    {
        JSONObject obj = new JSONObject();
        private TextView username = (TextView) findViewById(R.id.username);
        try {
            obj.put("type","playerCreation");
            obj.put("playerName",username.getText());
        }
        catch (JSONException e) {

            e.printStackTrace();
        }
    }
}
