package com.cmpe243.canstertest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static String EXTRA_ADDRESS = "device_address";

    Button searchButton, onOffButton;
    TextView statusBluetooth;
    ListView listViewPaired, listViewNewDevices;
    DeviceListAdapter deviceListAdapter;

    ArrayList<BluetoothDevice> newDevicesArrayAdapter;
    ArrayList<BluetoothDevice> pairedDevicesArrayAdapter;

    BluetoothDevice mBTDevice=null;
    BluetoothAdapter bluetoothAdapter=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton=(Button) findViewById(R.id.searchButton);
        onOffButton =(Button) findViewById(R.id.onOffButton);
        statusBluetooth =(TextView) findViewById(R.id.statusBluetooth);
        listViewPaired =(ListView)findViewById(R.id.listViewPaired);
        //listViewNewDevices =(ListView)findViewById(R.id.list_new);

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        pairedDevicesArrayAdapter =new ArrayList<>();
        newDevicesArrayAdapter = new ArrayList<>();

        listViewPaired.setOnItemClickListener(mDeviceClickListener);
//        listViewNewDevices.setOnItemClickListener(mDeviceClickListener);

        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, BTIntent);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        bluetoothStart();
//        listViewPaired.setAdapter(null);
//        pairedDevicesArrayAdapter.clear();
//        doBluetoothDiscovery();
//        statusBluetooth.setText("BLUETOOTH DISCOVERY");
    }

    public void onOffButtonClicked(View view) {
        Log.d(TAG, "ON/OFF: BUTTON PRESSED");
        bluetoothStart();
        enableBluetooth();
        checkBluetoothPermissions();
    }

    public void searchButtonClicked(View view) {
        Log.d(TAG, "SEARCH: BUTTON PRESSED");
        if (!bluetoothAdapter.isEnabled()){
            enableBluetooth();
        }
        else if (bluetoothAdapter.isEnabled()) {
            listViewPaired.setAdapter(null);
            pairedDevicesArrayAdapter.clear();
            doBluetoothDiscovery();
            statusBluetooth.setText("BLUETOOTH DISCOVERY");
        }
        //enableBluetooth();
        //listViewPaired.setAdapter(null);
        //statusBluetooth.setText("");
        //pairedDevicesArrayAdapter.clear();
        //doBluetoothDiscovery();
        //listViewNewDevices.setAdapter(null);
        //newDevicesArrayAdapter.clear();
    }

    private void bluetoothStart() {
//        if(!bluetoothAdapter.isEnabled()){ searchButton.setEnabled(false); }
//        else{ searchButton.setEnabled(true); }
        if(!bluetoothAdapter.isEnabled()) {
            statusBluetooth.setText("BLUETOOTH DISABLED");
        }
        else {statusBluetooth.setText("BLUETOOTH ENABLED");}
        //statusBluetooth.setText("");
        listViewPaired.setAdapter(null);
//        listViewNewDevices.setAdapter(null);
    }

    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "DISCOVERY: CANCELLED");
            mBTDevice= (BluetoothDevice) av.getAdapter().getItem(arg2);

            //userToast("Connected to: "+ mBTDevice.getName());
            Log.d(TAG, "Connected to:" +mBTDevice.getAddress());
            String device_name=mBTDevice.getName();
            String device_address=mBTDevice.getAddress();
            bluetoothStart();

            Log.d(TAG, "Calling New Activity" );
            Intent newintent = new Intent(MainActivity.this, MapsActivity.class);
            newintent.putExtra(EXTRA_ADDRESS,device_address);
            startActivity(newintent);
        }
    };

    private void enableBluetooth() {
        Log.d(TAG, "");
        if(bluetoothAdapter==null){
            Log.i(TAG,"ERROR: No Bluetooth Capabilities");
        }
        if(!bluetoothAdapter.isEnabled()){
            Log.d(TAG, "BLUETOOTH: TURNING ON");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            searchButton.setEnabled(true);
        }
        if(bluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            bluetoothAdapter.disable();
            searchButton.setEnabled(false);
        }
    }

    private void doBluetoothDiscovery() {
        Log.d(TAG, "doDiscovery: Discovering Devices.");
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        searchButton.setText("ENABLE");
                        Log.d(TAG, "mBroadcastReceiver1: STATE OFF");
                        userToast("BLUETOOTH DISABLED");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        searchButton.setText("SEARCH");
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        userToast("BLUETOOTH ENABLED");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//                    newDevicesArrayAdapter.add(device);
//                    deviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, newDevicesArrayAdapter);
//                    listViewNewDevices.setAdapter(deviceListAdapter);
//                }
                if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                    pairedDevicesArrayAdapter.add(device);
                    deviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, pairedDevicesArrayAdapter);
                    listViewPaired.setAdapter(deviceListAdapter);

                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            }
        }
    };

    // WE NEED TO UNREGISTER BLUETOOTH ACTIVITIES AND CANCEL DISCOVERY AS THESE TAKE UP RESOURCES IN ANDROID AND IT IS BEST TO STOP THEM AFTER WE FINISH USING THEM
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called.");
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mReceiver);
        bluetoothAdapter.cancelDiscovery();
    }

    //THIS IS TO JUST SENT THE TOAST MESSAGES AS STATUS
    public void userToast(String toastMessage){
        Toast.makeText(getApplicationContext(),toastMessage,Toast.LENGTH_SHORT).show();
        statusBluetooth.setText(toastMessage);
    }

    //THIS IS NEEDED FOR BLUETOOTH PERMISSIONS FOR DEVICES ABOVE LOLLIPOP VERSION, FOR DEVICES BELOW API 23 CAN IGNORE THIS FUNCTION
    private void checkBluetoothPermissions() {
        Log.d(TAG, "checkBTPermissions: Checking permissions");
        int permission_check = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permission_check += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permission_check != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
        else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
}
