package com.example.ryoki.quizzly;

import android.os.TestLooperManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.telephony.RadioAccessSpecifier;
import android.widget.RadioButton;
import android.widget.TextView;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        question = (TextView) findViewById(R.id.question);
        question.setText("Question 1");
        optionA = (RadioButton) findViewById(R.id.buttonA);
        optionB = (RadioButton) findViewById(R.id.buttonB);
        optionC = (RadioButton) findViewById(R.id.buttonC);
        optionD = (RadioButton) findViewById(R.id.buttonD);
    }

    protected void bluetoothRespond(byte[] dataIn){
        String input = new String(dataIn);
        try {
            JSONObject response = new JSONObject(input);
            String type = response.getString("type");
            switch (type) {
                // case statements
                // values must be of same type of expression
                case "question":
                    updateQuestion(response.get("data"));
                    break;

                case "scoreUpdate":
                    updateScore(getContentResolver());
                    break;
                default:
                    break;
            }
        }
        catch (JSONException e) {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }
    }

    protected void updateQuestion(Object newQuestion) {
        question.setText(newQuestion["question"]);
        optionA.setText(newQuestion["options"][0]);
        optionB.setText(newQuestion["options"][1]);
        optionC.setText(newQuestion["options"][2]);
        optionD.setText(newQuestion["options"][3]);

    }

    protected void updateScore(Object newScore){

    }

    private TextView question;
    private RadioButton optionA;
    private RadioButton optionB;
    private RadioButton optionC;
    private RadioButton optionD;
    private String title;

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
    }


}

