package com.cmpe243.canstertest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.StringTokenizer;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {


    //==============================BLUETOOTH==============================
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final String TAG = "Debug_Activity";
    private BluetoothAdapter mBluetoothAdapter = null;
    private String mConnectedDeviceName = null;
    private String mConnectedDeviceAddress = null;
    private BluetoothChatService mChatService = null;
    private StringBuffer mOutStringBuffer;
    StringBuilder appendMessages;
    String inputPacketString;
    private static final char ENDLINE = '\n';
    public static String EXTRA_ADDRESS = "device_address";
    //==============================BLUETOOTH==============================

    //==============================OUTPUT==============================
//    TextView tVLat, tVLng, tVCompass, tVHeading, tVBattery, tVUltrasonic;
      TextView tVCLat,tVCLng,tVDLat,tVDLng,tVUL,tVUM,tVUR,tVSpeed,tVCmpsCurrent,tVCmpsRequired, tVSteerDirections;
    //==============================OUTPUT==============================

    //==============================MAPS==============================
    private GoogleMap mMap;
    private Marker currentLocation, destinationLocation;
    private double destinationLat = 0.0, destinationLng = 0.0;
    LatLng cansterCurrent, cansterDestination;
    Button startTrip, endTrip;
    TextView statusMap;
    //==============================MAPS==============================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //==============================BLUETOOTH==============================
        Intent intent = getIntent();
        mConnectedDeviceAddress = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //==============================BLUETOOTH==============================

        //==============================findViewById==============================
        //startTrip = (Button) findViewById(R.id.mapStartButton);
        //endTrip = (Button) findViewById(R.id.mapStopButton);
        statusMap=(TextView)findViewById(R.id.mapStatus);
//        tVLat=(TextView)findViewById(R.id.tV_mapLat);
//        tVLng=(TextView)findViewById(R.id.tV_mapLng);
//        tVCompass=(TextView)findViewById(R.id.tV_mapCompass);
//        tVHeading=(TextView)findViewById(R.id.tV_mapHeading);
//        tVBattery=(TextView)findViewById(R.id.tV_Battery);
//        tVUltrasonic=(TextView)findViewById(R.id.tV_ultrasonic);
          tVCLat=(TextView)findViewById(R.id.tV_cLat);
          tVCLng=(TextView)findViewById(R.id.tV_cLng);
          tVDLat=(TextView)findViewById(R.id.tV_dLat);
          tVDLng=(TextView)findViewById(R.id.tV_dLng);
          tVCmpsCurrent=(TextView)findViewById(R.id.tV_CompassCurrent);
          tVCmpsRequired=(TextView)findViewById(R.id.tV_CompassRequired);
          tVSpeed=(TextView)findViewById(R.id.tV_speed);
          tVSteerDirections=(TextView)findViewById(R.id.tV_steeringDirection);
          tVUL=(TextView)findViewById(R.id.tV_ultraLeft);
          tVUM=(TextView)findViewById(R.id.tV_ultraMiddle);
          tVUR=(TextView)findViewById(R.id.tV_ultraRight);
        //==============================findViewByID==============================

        //==============================MAPS==============================
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //==============================MAPS==============================

        //==============================BOTTOMNAVBAR==============================
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_debug:
//                        Intent openActivity2 = new Intent(MapsActivity.this, DebugActivity.class);
//                        openActivity2.putExtra(EXTRA_ADDRESS,mConnectedDeviceAddress);
//                        Intent intent = getIntent();
//                        mConnectedDeviceAddress = intent.getStringExtra(MapsActivity.EXTRA_ADDRESS);
//                        openActivity2.putExtra(EXTRA_ADDRESS,mConnectedDeviceAddress);
//                        startActivity(openActivity2);
                        bottomNavigationView.setItemIconTintList(ColorStateList.valueOf(Color.RED));
                        bottomNavigationView.setItemTextColor(ColorStateList.valueOf(Color.RED));
                        Toast.makeText(MapsActivity.this, "Stop Pressed", Toast.LENGTH_SHORT).show();
                        String message1 = "$STOP\r\n";
                        userToast("Status: ","STOP message sent",false);
                        sendMessage(message1);
                    case R.id.navigation_maps:
                        //Toast.makeText(MapsActivity.this, "Already On Maps", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.navigation_bluetooth:
//                        mChatService.stop();
//                        setupChat();
                        bottomNavigationView.setItemIconTintList(ColorStateList.valueOf(Color.GREEN));
                        bottomNavigationView.setItemTextColor(ColorStateList.valueOf(Color.GREEN));
                        Toast.makeText(MapsActivity.this, "Start Pressed", Toast.LENGTH_SHORT).show();
                        String message2 = "$START" +","+ destinationLat +","+ destinationLng +"\r\n";
                        userToast("Status: ","START message sent",false);
                        sendMessage(message2);
                        break;
                }
                return true;
            }
        });
        //==============================BOTTOMNAVBAR==============================

        //==============================TOPTOOLBAR==============================
//        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(myToolbar);
        //==============================TOPTOOLBAR==============================
    }
/*==================================================================================================================================
    Called when the map is ready to be used
==================================================================================================================================*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in SJSU 10th Street and move the camera
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLng SJSU = new LatLng(37.339312, -121.881111);
        mMap.addMarker(new MarkerOptions().position(SJSU).title("Canster Truck").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(SJSU));
        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(SJSU.latitude,SJSU.longitude),17.0f ));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                destinationLat = latLng.latitude; destinationLng = latLng.longitude;
                cansterDestination = new LatLng(destinationLat,destinationLng);
                mMap.addMarker(new MarkerOptions().position(cansterDestination).title("Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                userToast("Destination Marker:",cansterDestination.toString(),false);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.remove();
                destinationLat = 0.0; destinationLng =0.0;
                userToast("Destination Marker:","Removed",false);
                return true;
            }
        });

    }

//    public void startTripButtonClicked(View view) {
//        String message = "$START" +","+ destinationLat +","+ destinationLng +"\r\n";
//        userToast("Status: ","START message sent",false);
//        sendMessage(message);
//    }
//    public void stopTripButtonClicked(View view) {
//        String message = "$STOP\r\n";
//        userToast("Status: ","STOP message sent",false);
//        sendMessage(message);
//    }

    private void connectDevice(boolean secure) {

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mConnectedDeviceAddress);
        mChatService.connect(device, secure);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            userToast("Status: ","Bluetooth Disabled",true);
            finish();
        } else if (mChatService == null) {
            setupChat();
        }
    }

    private void sendMessage(String message) {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            userToast("Status: ","No Connection",false);
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
            mOutStringBuffer.setLength(0);
        }
    }

    private void setupChat() {
        mChatService = new BluetoothChatService(this, mHandler);
        mOutStringBuffer = new StringBuffer("");
        connectDevice(false);
    }
/*==================================================================================================================================
    Handler allows you to send and process Message and Runnable objects associated with a thread's MessageQueue.
==================================================================================================================================*/
    Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            userToast("Status: "," Connected",false);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            userToast("Status: ", "Connecting",false);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            userToast("Status: ", "No Connection",false);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    if (readBuf != null) {
                        for (int i = 0; i < readBuf.length; i++) {
                            byte rx_char = readBuf[i];
                            if (rx_char == ENDLINE) {
                                inputPacketString += Character.toString((char) rx_char);
                                parseMessage(inputPacketString);
                                inputPacketString = "";
                            } else {
                                inputPacketString += Character.toString((char) rx_char);
                            }
                        }
                    }
                    userToast("Status: ","Incoming Message",false);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;
//                case Constants.MESSAGE_DEVICE_ADDRESS:
//                    mConnectedDeviceAddress =msg.getData().getString(Constants.DEVICE_ADDRESS);
//                    break;
                case Constants.MESSAGE_TOAST:
                    break;
            }

            return false;
        }
    });

    private void parseMessage(String readMessage) {
        //appendMessages.append("RX: \t" + readMessage + "\n");
//        String incomingMessage = readMessage;
//        String preFix = "$canster,";
//        String noPreFix = incomingMessage.substring(incomingMessage.indexOf(preFix)+ preFix.length());
//        String[] tokens = noPreFix.split(",");
//        tVLat.setText("Latitude:\t" + tokens[1].toString());
//        tVLng.setText("Longitude:\t" + tokens[2].toString());
//        tVCompass.setText("Compass Heading:\t" + tokens[3].toString());
//        tVHeading.setText("Battery:\t" + tokens[4].toString());
        StringTokenizer tokenizer = new StringTokenizer(readMessage, ",");
        String dirIgnore = tokenizer.nextToken();
        String dirCLat = tokenizer.nextToken();
        String dirCLng = tokenizer.nextToken();
        String dirDLat = tokenizer.nextToken();
        String dirDLng = tokenizer.nextToken();
        String dirUL = tokenizer.nextToken();
        String dirUM = tokenizer.nextToken();
        String dirUR= tokenizer.nextToken();
        String dirSpeed = tokenizer.nextToken();
        String dirCCompass = tokenizer.nextToken();
        String dirRCompass = tokenizer.nextToken();
        String dirSteerHeading = tokenizer.nextToken();
        tVUL.setText("Left: " + dirUL +" cm");
        tVUM.setText("Middle: " + dirUM + " cm");
        tVUR.setText("Right: " + dirUR + " cm");
        tVCLat.setText("Current Latitude:\t" + dirCLat +"°");
        tVCLng.setText("Current Longitude:\t" + dirCLng +"°");
        tVDLat.setText("Destination Latitude:\t" + dirDLat +"°");
        tVDLng.setText("Destination Longitude:\t" + dirDLng +"°");
        tVCmpsCurrent.setText("Current Compass Heading:\t" + dirCCompass +"°");
        tVCmpsRequired.setText("Required Compass Heading:\t" + dirRCompass +"°");
        tVSpeed.setText("Speed:\t" + dirSpeed +" kph");
//        if(dirSteerHeading.equals("-2")){
//            tVSteerDirections.setText("Steering Directions: Hard Left");
//        }
//        else if (dirSteerHeading.equals("-1")){
//            tVSteerDirections.setText("Steering Directions: Slightly Left");
//        }
//        else if (dirSteerHeading.equals("0")){
//            tVSteerDirections.setText("Steering Directions: Straight");
//        }
//        else if (dirSteerHeading.equals("1")){
//            tVSteerDirections.setText("Steering Directions: Slightly Right");
//        }
//        else if (dirSteerHeading.equals("2")){
//            tVSteerDirections.setText("Steering Directions: Hard Right");
//        }
//
//
//
//        switch (dirSteerHeading) {
//            case "-2":
//                tVSteerDirections.setText("Steering Directions: Hard Left");
//            case "-1":
//                tVSteerDirections.setText("Steering Directions: Slightly Left");
//            case "0":
//                tVSteerDirections.setText("Steering Directions: Straight");
//            case "1":
//                tVSteerDirections.setText("Steering Directions: Slighty Right");
//            case "2":
//                tVSteerDirections.setText("Steering Directions: Hard Right");
//        }

        tVSteerDirections.setText("Steering Direction:\t" + dirSteerHeading);
//        String dirLat = tokenizer.nextToken();
//        String dirLng = tokenizer.nextToken();
//        String dirCompass = tokenizer.nextToken();
//        String dirSpeed = tokenizer.nextToken();
//        String dirBattery = tokenizer.nextToken();
//        String dirUltrasonicL = tokenizer.nextToken();
//        String dirUltrasonicM = tokenizer.nextToken();
//        String dirUltrasonicR = tokenizer.nextToken();
//        tVLat.setText("Latitude:\t" + dirLat);
//        tVLng.setText("Longitude:\t" + dirLng);
//        tVCompass.setText("Compass Heading:\t" + dirCompass);
//        tVHeading.setText("Speed:\t" + dirSpeed);
//        tVBattery.setText("Battery:\t" + dirBattery);
//        tVUltrasonic.setText("L:\t" + dirUltrasonicL + "\t" + "M:\t" + dirUltrasonicM + "\t" + "R:\t" + dirUltrasonicR);
    }
/*==================================================================================================================================
    userToast is used to Log messages to show in status textView,
    @boolean toast = true -> show toast message along with textView
    @boolean toast = false -> show only textView
==================================================================================================================================*/
    public void userToast(String prefix, String message, boolean toast) {
        if (toast) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
        statusMap.setText(prefix + message);

    }
/*==================================================================================================================================
    onDestroy() used to release all remaining resources created by onCreate()
==================================================================================================================================*/
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
        mBluetoothAdapter.cancelDiscovery();
        //mBluetoothAdapter.disable();
        finish();
    }

}
