package com.cmpe243.canstertest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DebugActivity extends AppCompatActivity {
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
//    private static final int MESSAGE_DEVICE_ADDRESS = 6;
    private static final String TAG = "Debug_Activity";
    private BluetoothAdapter mBluetoothAdapter = null;
    private String mConnectedDeviceName = null;
    private String mConnectedDeviceAddress = null;
    private BluetoothChatService mChatService = null;
    private StringBuffer mOutStringBuffer;
    StringBuilder appendMessages, appendMessages2;
    String inputPacketString;
    private static final char ENDLINE = '\n';
    public static String EXTRA_ADDRESS = "device_address";

    Button send_btn,start_btn,stop_btn;
    TextView status,rxData;
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        //Bluetooth_Setting:
        Intent intent = getIntent();
        mConnectedDeviceAddress = intent.getStringExtra(MapsActivity.EXTRA_ADDRESS);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Bluetooth_Setting:

        rxData=(TextView)findViewById(R.id.rxData) ;
        status=(TextView)findViewById(R.id.status);
        send_btn=(Button)findViewById(R.id.send_btn);
        start_btn=(Button)findViewById(R.id.start_btn);
        stop_btn=(Button) findViewById(R.id.stop_btn);
        //end_btn=(Button)findViewById(R.id.end_btn);
        editText=(EditText) findViewById(R.id.editText);
        appendMessages = new StringBuilder();
        appendMessages2 = new StringBuilder();

        rxData.setMovementMethod(new ScrollingMovementMethod());

        userToast("Status: Connected to ",mConnectedDeviceAddress,true);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
//                    case R.id.navigation_maps:
//                        //onDestroy();
//                        Intent openActivity3 = new Intent(DebugActivity.this, MapsActivity.class);
//                        openActivity3.putExtra(EXTRA_ADDRESS,mConnectedDeviceAddress);
//                        startActivity(openActivity3);
//                        break;
                    case R.id.navigation_debug:
                        Toast.makeText(DebugActivity.this, "Already On Debug", Toast.LENGTH_SHORT).show();
                        break;
//                    case R.id.navigation_bluetooth:
//                        mChatService.stop();
//                        setupChat();
//                        break;
                }
                return true;
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
        mBluetoothAdapter.cancelDiscovery();
        finish();
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            userToast(" ","Bluetooth OFF.",true);
            finish();
        } else if (mChatService == null) {
            setupChat();
        }
    }

    private void setupChat() {
        mChatService = new BluetoothChatService(this, mHandler);
        mOutStringBuffer = new StringBuffer("");
        connectDevice(false);
    }

    public void sendMessage(String message) {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            userToast("Status: ","Not Connected!.Try Again",false);
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
            mOutStringBuffer.setLength(0);
            editText.setText(mOutStringBuffer);
        }
    }



    Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            userToast("Status: "," Connected to "+mConnectedDeviceName,false);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            userToast("Status: ", "Connecting...",false);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            userToast(" ", "Unable to Connect.Try Again",false);

                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    userToast("Status: ","Message Sent",false);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    //rxData.setText(" ");
                    //String readMessage = new String(readBuf, 0, msg.arg1);
                    for(int i=0;i<readBuf.length;i++){
                        byte rx_char=readBuf[i];
                        if(rx_char == ENDLINE){
                            inputPacketString+=Character.toString((char)rx_char);
                            parseMessage(inputPacketString);
                            inputPacketString="";
                        }
                        else{
                            inputPacketString+=Character.toString((char)rx_char);
                        }
                    }
                    //appendMessages.append("RX: \t" + readMessage + "\n");
                    //rxData.setText(appendMessages);
                    userToast(" ","Status: Incoming Message",false);
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

    public void send_btn(View view) {
        String message = (editText.getText().toString() + "\r\n");
        appendMessages.append("TX: \t" + message + "\n");
        rxData.setText(appendMessages);
        sendMessage(message);
    }

    public void start_btn(View view) {
        String message = "$START\r\n";
        appendMessages.append("TX: \t" + message + "\n");
        rxData.setText(appendMessages);
        sendMessage(message);
    }

    public void stop_btn(View view) {
        String message = "$STOP\r\n";
        appendMessages.append("TX: \t" + message + "\n");
        rxData.setText(appendMessages);
        sendMessage(message);
    }

    private void parseMessage(String readMessage) {
        appendMessages.append("RX: \t" + readMessage + "\n");
        rxData.setText(appendMessages);
        //rxData.setText("RX: "+readMessage);
    }




    public void userToast(String prefix, String message, boolean toast) {
        if (toast) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
        status.setText(prefix + message);

    }
    private void connectDevice(boolean secure) {

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mConnectedDeviceAddress);
        mChatService.connect(device, secure);
    }

}
