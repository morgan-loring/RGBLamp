package com.example.morgan.ledcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    private String btAddress = null;
    public static String BT_ADDRESS = "device_address";

    Button devicesButton = null;
    ListView deviceList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devicesButton = findViewById(R.id.devicesButton);
        deviceList = findViewById(R.id.deviceList);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "No bluetooth adapter", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(!bluetoothAdapter.isEnabled())
            {
                Intent turnOnBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOnBT, 1);
            }
        }

        devicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevices = bluetoothAdapter.getBondedDevices();
                ArrayList list = new ArrayList();

                if(pairedDevices.size() > 0)
                {
                    for(BluetoothDevice dev : pairedDevices)
                    {
                        list.add(dev.getName() + " \n" + dev.getAddress());
                    }
                }

                final ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, list);
                deviceList.setAdapter(adapter);
                deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String viewContent = ((TextView) view).getText().toString();
                        btAddress = viewContent.substring(viewContent.length() - 17);
                        Intent transmitIntent = new Intent(MainActivity.this, Transmit.class);
                        transmitIntent.putExtra(BT_ADDRESS, btAddress);

                        startActivity(transmitIntent);
                    }
                });
            }
        });
    }
}
