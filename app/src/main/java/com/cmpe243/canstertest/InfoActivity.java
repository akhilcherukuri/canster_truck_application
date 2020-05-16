package com.cmpe243.canstertest;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.StringTokenizer;


public class InfoActivity extends AppCompatActivity  {


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
      TextView tVCLat,tVCLng,tVDLat,tVDLng,tVUL,tVUM,tVUR,tVUB,tVSpeed,tVCmpsCurrent,tVCmpsRequired;
      TextView tVSteerDirections, tVDistance, tVBattery, tVRPS, tVPWM, tVMotorSpeed, tVNextCheckpoint;
      private double destinationLat = 0.0, destinationLng = 0.0;
      private double currentLat = 0.0, currentLng = 0.0;
      TextView statusInfo;
      String dirCLat ="0.0" , dirCLng ="0.0";
    //==============================OUTPUT==============================


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_main);

        //==============================BLUETOOTH==============================
        Intent intent = getIntent();
        mConnectedDeviceAddress = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //==============================BLUETOOTH==============================

        //==============================findViewById==============================
        statusInfo =(TextView)findViewById(R.id.infoStatus);
          tVCLat=(TextView)findViewById(R.id.tV_cLat_info);
          tVCLng=(TextView)findViewById(R.id.tV_cLng_info);
          tVDLat=(TextView)findViewById(R.id.tV_dLat_info);
          tVDLng=(TextView)findViewById(R.id.tV_dLng_info);
          tVCmpsCurrent=(TextView)findViewById(R.id.tV_CompassCurrent_info);
          tVCmpsRequired=(TextView)findViewById(R.id.tV_CompassRequired_info);
          tVSpeed=(TextView)findViewById(R.id.tV_speed_info);
          tVSteerDirections=(TextView)findViewById(R.id.tV_steeringDirection_info);
//          tVUL=(TextView)findViewById(R.id.tV_ultraLeft_info);
//          tVUM=(TextView)findViewById(R.id.tV_ultraMiddle_info);
//          tVUR=(TextView)findViewById(R.id.tV_ultraRight_info);
//          tVUB=(TextView)findViewById(R.id.tV_ultraBack_info);
          tVDistance=(TextView)findViewById(R.id.tvDistance_info);
          tVBattery=(TextView)findViewById(R.id.tV_Battery_info);
          tVRPS=(TextView)findViewById(R.id.tV_RPS_info);
          tVPWM=(TextView)findViewById(R.id.tV_PWM_info);
          tVMotorSpeed=(TextView)findViewById(R.id.tV_MotorSpeed_info);
          tVNextCheckpoint=(TextView)findViewById(R.id.tV_CheckpointIndex_info);

        //==============================findViewByID==============================


        //==============================BOTTOMNAVBAR==============================
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_debug:
                        if (mChatService != null) {
                            mChatService.stop();
                        }
                        mBluetoothAdapter.cancelDiscovery();
                        Intent openActivity3 = new Intent(InfoActivity.this, DebugActivity.class);
                        openActivity3.putExtra(EXTRA_ADDRESS,mConnectedDeviceAddress);
                        startActivityForResult(openActivity3,1);
                        break;
                    case R.id.navigation_maps:
                        if (mChatService != null) {
                            mChatService.stop();
                        }
                        mBluetoothAdapter.cancelDiscovery();
                        Intent openActivity2 = new Intent(InfoActivity.this, MapsActivity.class);
                        openActivity2.putExtra(EXTRA_ADDRESS,mConnectedDeviceAddress);
                        startActivityForResult(openActivity2,1);
                        break;
                    case R.id.navigation_bluetooth:
                        Toast.makeText(InfoActivity.this, "Bluetooth Restart", Toast.LENGTH_SHORT).show();
                        mChatService.stop();
                        setupChat();
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
                                //inputPacketString += Character.toString((char) rx_char);
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
        String dirSteerHeading = tokenizer.nextToken();
        String dirReached = tokenizer.nextToken();
        String dirDistance = tokenizer.nextToken();
        String dirBattery = tokenizer.nextToken();
        String dirRPS = tokenizer.nextToken();
        String dirPWM = tokenizer.nextToken();
        String dirUB = tokenizer.nextToken();
        String dirMotorSpeed = tokenizer.nextToken();
        String dirCheckpointIndex = tokenizer.nextToken();
        if (dirIgnore.endsWith("canster")) {
//            tVUL.setText("Left: " + dirUL + " cm");
//            tVUM.setText("Middle: " + dirUM + " cm");
//            tVUR.setText("Right: " + dirUR + " cm");
//            tVUB.setText("Back: " + dirUB + " cm");
            tVCLat.setText("Latitude:\n" + dirCLat + "°");
            tVCLng.setText("Longitude:\n" + dirCLng + "°");
            tVDLat.setText("Latitude:\n" + dirDLat + "°");
            tVDLng.setText("Longitude:\n" + dirDLng + "°");
            tVCmpsCurrent.setText("Current Heading:\n" + dirCCompass + "°");
            tVCmpsRequired.setText("Required Heading:\n" + dirRCompass + "°");
            tVSpeed.setText("Speed:\t" + dirSpeed + " kph");
            tVDistance.setText("Distance till Destination:\t" + dirDistance + " meters");
            tVBattery.setText("Battery:\t" + dirBattery + " volts");
            tVRPS.setText("RPS:\t" + dirRPS);
            tVPWM.setText("PWM:\t" + dirPWM);
            tVNextCheckpoint.setText("Waypoint Index:\t" + dirCheckpointIndex);
            if (dirReached.equals("1")) {
                statusInfo.setText("Status: Destination Reached");
                manageBlinkEffect();
            } else {
                statusInfo.setText("Status: Going to Destination");
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
            switch (dirMotorSpeed) {
                case "-3":
                    tVMotorSpeed.setText("Motor Speed: Reverse Fast");
                    break;
                case "-2":
                    tVMotorSpeed.setText("Motor Speed: Reverse Medium");
                    break;
                case "-1":
                    tVMotorSpeed.setText("Motor Speed: Reverse Slow");
                    break;
                case "0":
                    tVMotorSpeed.setText("Motor Speed: Neutral");
                    break;
                case "1":
                    tVMotorSpeed.setText("Motor Speed: Forward Slow");
                    break;
                case "2":
                    tVMotorSpeed.setText("Motor Speed: Forward Medium");
                    break;
                case "3":
                    tVMotorSpeed.setText("Motor Speed: Forward Fast");
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
        statusInfo.setText(prefix + message);

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
    public void manageBlinkEffect() {
        ObjectAnimator anim = ObjectAnimator.ofInt(statusInfo,"backgroundColor",Color.DKGRAY,Color.RED,Color.DKGRAY);
        anim.setDuration(800);
        anim.setEvaluator(new ArgbEvaluator());
//        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(10);
        anim.start();
    }
}
