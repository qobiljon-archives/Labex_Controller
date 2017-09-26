package org.labex.controller;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize() {
        byte_text = (EditText) findViewById(R.id.byte_text);
        acti_text = (EditText) findViewById(R.id.byte_action);
        controlPanel = findViewById(R.id.controlPanel);
        connectButton = (Button) findViewById(R.id.connectButton);
    }

    // region VARIABLES
    private EditText byte_text;
    private EditText acti_text;
    private View controlPanel;
    private Button connectButton;
    // endregion

    public void bluetoothConnectClick(View view) {
        if (connectButton.getText().toString().equals(getString(R.string.connectLabex))) {
            // IF BLUETOOTH IS DISABLED, AND CANNOT CONNECT
            LabexConnection.btAdapter = BluetoothAdapter.getDefaultAdapter();

            if (!LabexConnection.btAdapter.isEnabled()) {
                Intent btOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(btOn, 0);
                //Log.e("MYERR", "Bluetooth is disabled. Requesting to turn it on.");
                return;
            }

            connectBluetooth();
        } else if (connectButton.getText().toString().equals(getString(R.string.disconnectLabex))) {
            if (LabexConnection.closeConnection()) {
                connectButton.setText(getString(R.string.connectLabex));
                controlPanel.setVisibility(View.INVISIBLE);
            } else {
                Toast.makeText(getApplicationContext(), "Labex disconnected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void connectBluetooth() {
        connectButton.setText(getString(R.string.connectingLabex));
        connectButton.setEnabled(false);

        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                LabexConnection.initConnection(getApplicationContext());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (LabexConnection.labexConnected()) {
                            connectButton.setText(getString(R.string.disconnectLabex));
                            controlPanel.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), "Labex connected", Toast.LENGTH_SHORT).show();
                        } else {
                            connectButton.setText(getString(R.string.connectLabex));
                            Toast.makeText(getApplicationContext(), "Labex couldn't connect", Toast.LENGTH_SHORT).show();
                            // just in case
                            if (LabexConnection.labexSocket != null && LabexConnection.labexSocket.isConnected())
                                try {
                                    LabexConnection.labexSocket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }

                        connectButton.setEnabled(true);
                    }
                });
            }
        });
    }

    public void openController(View view) {
        Intent controllerIntent = new Intent(getApplicationContext(), ControllerActivity.class);
        startActivity(controllerIntent);
    }

    public void manualSend(View v) {
        String action = acti_text.getText().toString();
        String data = byte_text.getText().toString();

        if (action.length() == 0 || data.length() == 0)
            return;

        if (!LabexConnection.sendAction(Byte.parseByte(action), Byte.parseByte(data), true))
            Toast.makeText(this, "Error while sending data", Toast.LENGTH_SHORT).show();

        //Log.e("SENT", String.format("%s %s", action, data));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            connectBluetooth();
        }
    }
}
