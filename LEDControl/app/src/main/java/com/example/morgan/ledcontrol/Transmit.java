package com.example.morgan.ledcontrol;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skydoves.colorpickerpreference.ColorEnvelope;
import com.skydoves.colorpickerpreference.ColorListener;
import com.skydoves.colorpickerpreference.ColorPickerView;

import java.io.IOException;
import java.util.UUID;

public class Transmit extends AppCompatActivity {

    private String btAddress;
    private ProgressDialog progress;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;

    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final char COLOR_START = 'C';
    private final char ON_START = 'O';
    private final char OFF_START = 'F';
    private final char TERMINATOR = '!';

    Button updateButton = null;
    Button onButton = null;
    Button offButton = null;
    EditText redText = null;
    EditText greenText = null;
    EditText blueText = null;
    ColorPickerView colorPicker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmit);

        Intent pastIntent = getIntent();
        btAddress = pastIntent.getStringExtra(MainActivity.BT_ADDRESS);

        updateButton = findViewById(R.id.updateButton);
        onButton = findViewById(R.id.onButton);
        offButton = findViewById(R.id.offButton);
        redText = findViewById(R.id.redText);
        greenText = findViewById(R.id.greenText);
        blueText = findViewById(R.id.blueText);
        colorPicker = findViewById(R.id.colorPicker);

        new ConnectBT().execute();

        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLed();
            }
        });

        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLed();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateColor(colorPicker.getColorRGB());
            }
        });

        colorPicker.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(ColorEnvelope colorEnvelope) {
                if (bluetoothSocket.isConnected()) {
                    int[] colors = colorEnvelope.getColorRGB();
                    updateColor(colors);
                    redText.setText(Integer.toString(colors[0]));
                    greenText.setText(Integer.toString(colors[1]));
                    blueText.setText(Integer.toString(colors[2]));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            bluetoothSocket.close();
        } catch (IOException ex) {

        }
    }

    private void turnOffLed() {
        if (bluetoothSocket != null) {
            try {
                String cmd = Character.toString(OFF_START) + Character.toString(TERMINATOR);
                bluetoothSocket.getOutputStream().write(cmd.getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void turnOnLed() {
        if (bluetoothSocket != null) {
            try {
                String cmd = Character.toString(ON_START) + Character.toString(TERMINATOR);
                bluetoothSocket.getOutputStream().write(cmd.getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void updateColor(int[] colors) {
        if (bluetoothSocket != null) {
            try {
                byte bytes[] = new byte[5];

                bytes[0] = COLOR_START;
                bytes[1] = new Integer(colors[0]).byteValue();
                bytes[2] = new Integer(colors[1]).byteValue();
                bytes[3] = new Integer(colors[2]).byteValue();
                bytes[4] = TERMINATOR;

                bluetoothSocket.getOutputStream().write(bytes);
                bluetoothSocket.getInputStream().read();
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void msg(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(Transmit.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (bluetoothSocket == null || !isBtConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = bluetoothAdapter.getRemoteDevice(btAddress);//connects to the device's address and checks if it's available
                    bluetoothSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetoothSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
    }
}
