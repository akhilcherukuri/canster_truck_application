package com.cmpe243.canstertest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
    String inputPacketString;
    private static final char ENDLINE = '\n';
    public static String EXTRA_ADDRESS = "device_address";
    //==============================BLUETOOTH==============================

    //==============================OUTPUT==============================
      TextView tVCLat,tVCLng,tVDLat,tVDLng,tVUL,tVUM,tVUR,tVSpeed,tVCmpsCurrent,tVCmpsRequired, tVSteerDirections, tVDistance;
    //==============================OUTPUT==============================

    //==============================MAPS==============================
    private GoogleMap mMap;
    public Marker currentLocationMarker, destinationLocationMarker;
    private double destinationLat = 0.0, destinationLng = 0.0;
    LatLng cansterDestination;
    TextView statusMap;
    String dirCLat ="0.0" , dirCLng ="0.0";
    locationThread locationThread;
    Polyline polyline;
    boolean isCurrentSet = false;
    boolean mapVisible = false;
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
        statusMap=(TextView)findViewById(R.id.mapStatus);
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
          tVDistance=(TextView)findViewById(R.id.tvDistance);
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
                        bottomNavigationView.setItemIconTintList(ColorStateList.valueOf(Color.RED));
                        bottomNavigationView.setItemTextColor(ColorStateList.valueOf(Color.RED));
                        Toast.makeText(MapsActivity.this, "Stop Pressed", Toast.LENGTH_SHORT).show();
                        String message1 = "$STOP\r\n";
                        userToast("Status: ","STOP message sent",false);
                        sendMessage(message1);
                    case R.id.navigation_maps:
//                        Toast.makeText(MapsActivity.this, "Already On Maps", Toast.LENGTH_SHORT).show();
//                        mChatService.stop();
//                        setupChat();
                        break;
                    case R.id.navigation_bluetooth:
                        bottomNavigationView.setItemIconTintList(ColorStateList.valueOf(Color.GREEN));
                        bottomNavigationView.setItemTextColor(ColorStateList.valueOf(Color.GREEN));
                        Toast.makeText(MapsActivity.this, "Start Pressed", Toast.LENGTH_SHORT).show();
                        String message2 = "$loc" +","+ Math.floor(destinationLat*1000000)/1000000 +","+ Math.floor(destinationLng*1000000)/1000000 +"\r\n";
                        userToast("Status: ","START message sent",false);
                        sendMessage(message2);
                            polyline = mMap.addPolyline(new PolylineOptions()
                                .add(new LatLng(Double.parseDouble(dirCLat),Double.parseDouble(dirCLng)), new LatLng(destinationLat, destinationLng))
                                .width(8)
                                .color(Color.GREEN));
                        locationThread = new locationThread();
                        locationThread.start();
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
        LatLng cansterCurrentLocation = new LatLng(37.339312, -121.881111);
        MarkerOptions a = new MarkerOptions().position(cansterCurrentLocation).title("Canster Truck").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        //currentLocationMarker = mMap.addMarker(a);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cansterCurrentLocation));
        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(cansterCurrentLocation.latitude,cansterCurrentLocation.longitude),17.0f ));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                destinationLat = latLng.latitude; destinationLng = latLng.longitude;
                cansterDestination = new LatLng(destinationLat,destinationLng);
                MarkerOptions dest = new MarkerOptions().position(cansterDestination).title("Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                destinationLocationMarker = mMap.addMarker(dest);
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
                    //userToast("Status: ","Incoming Message",false);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
            }

            return false;
        }
    });

    private void parseMessage(String readMessage) {
        StringTokenizer tokenizer = new StringTokenizer(readMessage, ",");
        String dirIgnore = tokenizer.nextToken();
        dirCLat = tokenizer.nextToken();
        dirCLng = tokenizer.nextToken();
        String dirDLat = tokenizer.nextToken();
        String dirDLng = tokenizer.nextToken();
        String dirUL = tokenizer.nextToken();
        String dirUM = tokenizer.nextToken();
        String dirUR= tokenizer.nextToken();
        String dirSpeed = tokenizer.nextToken();
        String dirCCompass = tokenizer.nextToken();
        String dirRCompass = tokenizer.nextToken();
        String dirDistance = tokenizer.nextToken();
        String dirSteerHeading = tokenizer.nextToken();
        String dirReached = tokenizer.nextToken();
        if (dirIgnore.endsWith("canster")) {
            tVUL.setText("Left: " + dirUL + " cm");
            tVUM.setText("Middle: " + dirUM + " cm");
            tVUR.setText("Right: " + dirUR + " cm");
            tVCLat.setText("Current Latitude:\t" + dirCLat + "°");
            tVCLng.setText("Current Longitude:\t" + dirCLng + "°");
            tVDLat.setText("Destination Latitude:\t" + dirDLat + "°");
            tVDLng.setText("Destination Longitude:\t" + dirDLng + "°");
            tVCmpsCurrent.setText("Current Compass Heading:\t" + dirCCompass + "°");
            tVCmpsRequired.setText("Required Compass Heading:\t" + dirRCompass + "°");
            tVSpeed.setText("Speed:\t" + dirSpeed + " kph");
            tVDistance.setText("Distance till Destination:\t" + dirDistance + " meters");
            if (dirReached.equals("1\n")) {
                statusMap.setText("Status: Destination Reached");
                manageBlinkEffect();
            } else {
                statusMap.setText("Status: Going to Destination");
            }
            switch (dirSteerHeading) {
                case "-2":
                    tVSteerDirections.setText("Steering Directions: Hard Left");
                    break;
                case "-1":
                    tVSteerDirections.setText("Steering Directions: Slightly Left");
                    break;
                case "0":
                    tVSteerDirections.setText("Steering Directions: Straight");
                    break;
                case "1":
                    tVSteerDirections.setText("Steering Directions: Slightly Right");
                    break;
                case "2":
                    tVSteerDirections.setText("Steering Directions: Hard Right");
                    break;
            }
        }
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
        locationThread.cancel();
        finish();
    }
    public void manageBlinkEffect() {
        ObjectAnimator anim = ObjectAnimator.ofInt(statusMap,"backgroundColor",Color.BLACK,Color.RED,Color.BLACK);
        anim.setDuration(800);
        anim.setEvaluator(new ArgbEvaluator());
//        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(10);
        anim.start();
    }

    class locationThread extends Thread {
        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(isCurrentSet)
                            {
                                currentLocationMarker.remove();
                                polyline.remove();
                            }
                            else
                                isCurrentSet = true;
                                polyline.remove();
                            LatLng currentLocation = new LatLng(Double.parseDouble(dirCLat) , Double.parseDouble(dirCLng));
                            currentLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("CANSTER TRUCK").icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
                            polyline = mMap.addPolyline(new PolylineOptions()
                                    .add(new LatLng(Double.parseDouble(dirCLat),Double.parseDouble(dirCLng)), new LatLng(destinationLat, destinationLng))
                                    .width(8)
                                    .color(Color.GREEN));
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        public void cancel() {
            interrupt();
        }
    };
}
