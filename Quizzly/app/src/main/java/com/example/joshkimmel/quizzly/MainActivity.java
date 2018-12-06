package com.example.joshkimmel.quizzly;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import android.os.*;

import com.google.gson.JsonIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.w3c.dom.Text;


public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private String bluetoothDeviceAddress;
    private String TAG;
    private UUID serverUuid;
    private InputStream inStream = null;
    private OutputStream outStream = null;
    private byte[] buffer;

    private Handler mHandler;
    private int STATE_DISCONNECTED = 0;
    private final int STATE_CONNECTED = 1;
    private final int STATE_QUESTION_RECEIVED = 2;
    private final int  STATE_SCORE_RECEIVED = 3;
    private final int STATE_WAITING = 4;
    private final int STATE_GAMEOVER = 5;
    private int currentState;

    // for question screen
    private TextView question;
    private RadioButton optionA;
    private RadioButton optionB;
    private RadioButton optionC;
    private RadioButton optionD;
    private Button submit;
    private JSONObject currentQuestion;

    // for score screen
    private TextView place;
    private TableLayout scoreTable;
    private TextView playerName1;
    private TextView playerName2;
    private TextView playerName3;
    private TextView playerName4;
    private TextView playerScore1;
    private TextView playerScore2;
    private TextView playerScore3;
    private TextView playerScore4;
    private int numPlayers;

    // for welcome screen
    private TextView playerCreate;
    private Button playerJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentState = 0;
        numPlayers = 0;

        question = (TextView) findViewById(R.id.question1);
        optionA = (RadioButton) findViewById(R.id.buttonA);
        optionB = (RadioButton) findViewById(R.id.buttonB);
        optionC = (RadioButton) findViewById(R.id.buttonC);
        optionD = (RadioButton) findViewById(R.id.buttonD);
        submit = (Button) findViewById(R.id.buttonS);

        place = (TextView) findViewById(R.id.place);
        scoreTable = (TableLayout) findViewById(R.id.scoreTable);
        playerName1 = (TextView) findViewById(R.id.playerName1);
        playerName2 = (TextView) findViewById(R.id.playerName2);
        playerName3 = (TextView) findViewById(R.id.playerName3);
        playerName4 = (TextView) findViewById(R.id.playerName4);
        playerScore1 = (TextView) findViewById(R.id.playerScore1);
        playerScore2 = (TextView) findViewById(R.id.playerScore2);
        playerScore3 = (TextView) findViewById(R.id.playerScore3);
        playerScore4 = (TextView) findViewById(R.id.playerScore4);

        playerCreate = (TextView) findViewById(R.id.username);
        playerJoin = (Button) findViewById(R.id.submit_user);

        bluetoothDeviceAddress = getString(R.string.bluetoothDeviceAddress);
        TAG = getString(R.string.bluetoothLogTag);
        serverUuid = UUID.fromString(getString(R.string.bluetoothServerUuid));

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                bluetoothRespond((byte[])msg.obj);
            }
        };

        EnableBluetooth();
        ConnectToServer();
    }

    private void bluetoothRespond(byte[] dataIn){
        String input = new String(dataIn);
        try {
            JSONObject response = new JSONObject(input);
            String type = response.getString("type");
            switch (type) {
                // case statements
                // values must be of same type of expression
                case "question":
                    updateQuestion(response.getJSONObject("data"));
                    break;

                case "scoreUpdate":
                    updateScore(response.getJSONObject("data"));
                    break;
                default:
                    break;
            }
        }
        catch (JSONException e) {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }
    }

    protected void updateQuestion(JSONObject newQuestion) {
        optionA.setChecked(false);
        optionB.setChecked(false);
        optionC.setChecked(false);
        optionD.setChecked(false);

        try
        {
            Log.i("Testing", newQuestion.toString());
            currentQuestion = newQuestion;
            question.setText(newQuestion.getString("question"));
            String choiceA = newQuestion.getJSONArray("options").getString(0);
            optionA.setText(choiceA);
            String choiceB = newQuestion.getJSONArray("options").getString(1);
            optionB.setText(choiceB);
            String choiceC = newQuestion.getJSONArray("options").getString(2);
            optionC.setText(choiceC);
            String choiceD = newQuestion.getJSONArray("options").getString(3);
            optionD.setText(choiceD);
        }
        catch (JSONException e)
        {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }
        UpdateView(STATE_QUESTION_RECEIVED, false);
    }

    protected void updateScore (JSONObject newScore) {
        Log.i("Testing", newScore.toString());

        // Set score screen attributes
        try {
            JSONArray rankings = newScore.getJSONArray("rankings");
            numPlayers = rankings.length();
            for (int i=0; i<rankings.length(); i++) {
                JSONObject r = rankings.getJSONObject(i);
                switch (i) {
                    case 0:
                        playerName1.setText(r.get("playerName").toString());
                        playerScore1.setText(Integer.toString(r.getInt("score")));
                        break;
                    case 1:
                        playerName2.setText(r.get("playerName").toString());
                        playerScore2.setText(Integer.toString(r.getInt("score")));
                        break;
                    case 2:
                        playerName3.setText(r.get("playerName").toString());
                        playerScore3.setText(Integer.toString(r.getInt("score")));
                        break;
                    case 3:
                        playerName4.setText(r.get("playerName").toString());
                        playerScore4.setText(Integer.toString(r.getInt("score")));
                        break;
                    default:
                        break;
                }
            }
        }
        catch (JSONException e) {
            Log.e("MYAPP", "JSON error", e);
        }


        UpdateView(STATE_SCORE_RECEIVED, false);
    }

    // Method to handle incoming data read from the server
    private void OnRead (byte[] data) {
        String strData = new String(data);
        Log.i("ServerRead", strData);
    }

    private void OnConnect () {
        UpdateView(STATE_CONNECTED, false);
        //String test = "{\"type\": \"playerCreation\", \"data\": {\"playerName\": \"Test\"}}";
        //WriteToServer(test.getBytes());
    }

    // Method to enable Bluetooth on the given device
    // Call this from the main activity
    private void EnableBluetooth ()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth adapter is not available.");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            if (!bluetoothAdapter.enable()) {
                Log.e(TAG, "Could enable the Bluetooth adapter.");
                return;
            }
        }

        bluetoothDevice = bluetoothAdapter.getRemoteDevice(bluetoothDeviceAddress);
    }



    // Method to connect to the Bluetooth Server
    // Call this from the main activity
    private void ConnectToServer ()
    {
        //create and run an instance of ConnectThread
        ConnectThread tConnect = new ConnectThread();
        tConnect.start();
    }

    // Start listening to the Server over the Bluetooth socket
    // Called in the Connect thread after a successful connection, so no need to call it directly
    private void ListenToServer ()
    {
        //create and run an instance of ConnectedThread
        ConnectedThread tConnected = new ConnectedThread();
        tConnected.start();
    }

    // Method to send data to the Server
    // Call this from the main activity
    public void WriteToServer (byte[] bytes) {
        try {
            outStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
        }
    }

    // Method to end the connection with the Server
    // Call this method from the main activity
    public void EndConnection () {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }

    // Define a thread to manage connection with the target device
    private class ConnectThread extends Thread {
        public ConnectThread() {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // serverUuid is the app's UUID string, also used in the server code.
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(serverUuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            bluetoothSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                bluetoothSocket.connect();
                Log.i("Testing", "Connected");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.e("Testing", "Connection Error", connectException);
                try {
                    bluetoothSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            ListenToServer();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    // Define a thread to manage reads from the Server
    private class ConnectedThread extends Thread {
        private ConnectedThread() {
            // Get the input and output streams; using temp objects because
            // member streams are final.
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run() {
            buffer = new byte[1024]; //TODO: is this buffer size sufficient?
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            OnConnect();
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = inStream.read(buffer);

                    //TODO: method to handle new data that's in the buffer
                    Message msg = new Message();
                    msg.obj = buffer;
                    mHandler.sendMessage(msg);
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }
    }

    public void playerCreateClick(View view) {
        String pName = playerCreate.getText().toString();
        JSONObject send = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            send.put("type", "playerCreation");
            data.put("playerName", pName);
            send.put("data", data);
        }
        catch (JSONException e) {
            Log.e("MYAPP", "JSON error", e);
        }
        WriteToServer(send.toString().getBytes());
    }

    public void clickHandler(View view) {
        int currentNum = -1;
        try {
            currentNum = currentQuestion.getInt("number");
        }
        catch (JSONException e)
        {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }

        int answer = -1;
        if(optionA.isChecked())
        {
            answer = 0;
        }
        if(optionB.isChecked())
        {
            answer = 1;
        }
        if(optionC.isChecked())
        {
            answer = 2;
        }
        if(optionD.isChecked())
        {
            answer = 3;
        }

        Log.i("Testing", "Hello");

        JSONObject sendBack = new JSONObject();
        JSONObject dataBack = new JSONObject();
        try {
            sendBack.put("type", "answer");
            dataBack.put("number", currentNum);
            dataBack.put("answer", answer);
            sendBack.put("data", dataBack);
        }
        catch (JSONException e)
        {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }

        Log.i("Testing", sendBack.toString());
        WriteToServer(sendBack.toString().getBytes());

    }

    // Top-level method to execute a view transition
    private void UpdateView (int state, boolean gameOver) {
        switch (state) {
            case STATE_CONNECTED:
                ShowHideWelcomeScreen(false);
                ShowHideQuestionScreen(false);
                ShowHideScoreScreen(false);
                ShowHidePlayerCreation(true);
                break;
            case STATE_WAITING:
                ShowHidePlayerCreation(false);
                ShowHideQuestionScreen(false);
                ShowHideWaitingScreen(true);
                break;
            case STATE_QUESTION_RECEIVED:
                ShowHidePlayerCreation(false);
                ShowHideWelcomeScreen(false);
                ShowHideScoreScreen(false);
                ShowHideQuestionScreen(true);
                break;
            case STATE_SCORE_RECEIVED:
                ShowHidePlayerCreation(false);
                ShowHideWelcomeScreen(false);
                ShowHideQuestionScreen(false);
                ShowHideScoreScreen(true);
                break;
            default:
                break;
        }
    }

    private void ShowHideWelcomeScreen (boolean show) {

    }

    private void ShowHidePlayerCreation (boolean show) {
        int v = (show) ? View.VISIBLE : View.INVISIBLE;
        playerCreate.setVisibility(v);
        playerJoin.setVisibility(v);
    }

    private void ShowHideWaitingScreen (boolean show) {

    }

    private void ShowHideQuestionScreen (boolean show) {
        int v = (show) ? View.VISIBLE : View.INVISIBLE;
        question.setVisibility(v);
        optionA.setVisibility(v);
        optionB.setVisibility(v);
        optionC.setVisibility(v);
        optionD.setVisibility(v);
        submit.setVisibility(v);
    }

    private void ShowHideScoreScreen (boolean show) {
        int v = (show) ? View.VISIBLE : View.INVISIBLE;
        Log.i("Testing", "Visibility is: " + Integer.toString(v));
        //place.setVisibility(v);
        scoreTable.setVisibility(v);
        //for (int i = 0; i < numPlayers; i++) {
            //switch (i) {
                //case 0:
                    //Log.i("Testing", "Show/hide Score " + Integer.toString(numPlayers));
                    //playerName1.setVisibility(v);
                    //playerScore1.setVisibility(v);
                    //break;
                //case 1:
                    //playerName2.setVisibility(v);
                    //playerScore2.setVisibility(v);
                    //break;
                //case 2:
                    //playerName3.setVisibility(v);
                    //playerScore3.setVisibility(v);
                    //break;
                //case 3:
                    //playerName4.setVisibility(v);
                    //playerScore4.setVisibility(v);
                    //break;
                //default:
                    //break;
            //}
        //}
        playerName1.setVisibility(v);
        playerScore1.setVisibility(v);
        playerName2.setVisibility(v);
        playerScore2.setVisibility(v);
        playerName3.setVisibility(v);
        playerScore3.setVisibility(v);
        playerName4.setVisibility(v);
        playerScore4.setVisibility(v);
    }
}
