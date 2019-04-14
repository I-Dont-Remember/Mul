package com.example.mul;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ClientActivity extends AppCompatActivity {

    static Client_Provider_Common common = new Client_Provider_Common();

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the chat services
    private BTCommunicationService BTService = null;

    private String TAG = ClientActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (BTService == null){
                setupChat();
            }
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (BTService != null) {
            if (BTService.getState() == BTCommunicationService.STATE_NONE) {
                BTService.start();
            }
        }
    }

    private void setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        BTService = new BTCommunicationService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //When a message is recieved, assume that it contains wifi credentials
                    //so connect to wifi(That is a strong assumption. We should use some way of verifying)
                    common.connectToWifi(readMessage, getApplicationContext());
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();

                    Log.d("Tag", msg.getData().getString(TOAST));

                    if(msg.getData().getString(TOAST).equals("Device connection was lost")) {
                        //Connection was broken. Currently, we just completely disconnect from the provider.
                        //Idealy we should seamlessly connect to another provider
                        // TODO: get this working
                        // common.forgetCurrentNetwork(getApplicationContext());
                    }
                    break;
            }
        }
    };

//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case REQUEST_CONNECT_DEVICE:
//                // When DeviceListActivity returns with a device to connect
//                if (resultCode == Activity.RESULT_OK) {
//                    // Get the device MAC address
//                    String address = data.getExtras().getString(DiscoverDevices.EXTRA_DEVICE_ADDRESS);
//                    // Get the BLuetoothDevice object
//                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//                    // Attempt to connect to the device
//                    BTService.connect(device);
//                }
//                break;
//            case REQUEST_ENABLE_BT:
//                // When the request to enable Bluetooth returns
//                if (resultCode == Activity.RESULT_OK) {
//                    // Bluetooth is now enabled, so set up a chat session
//                    setupChat();
//                } else {
//                    // User did not enable Bluetooth or an error occured
//                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//        }
//    }

    public void onClickConnect(View v) {
//        Intent serverIntent = new Intent(ClientActivity.this, DiscoverDevices.class);
//        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

        initializeBTDiscovery();

        Intent i = new Intent(getApplicationContext(), Active_Client.class);
        startActivity(i);
    }

    private void initializeBTDiscovery(){
        //Turn on Bluetooth
        if (mBluetoothAdapter == null)
            Toast.makeText(getApplicationContext(), "Your device doesnt support Bluetooth", Toast.LENGTH_LONG).show();
        else if (!mBluetoothAdapter.isEnabled()) {
            Intent BtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(BtIntent, 0);
            Toast.makeText(getApplicationContext(), "Turning on Bluetooth", Toast.LENGTH_LONG).show();
        }

        // Quick permission check
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }

        //Direct all responses of "found" when searching to the "FoundReciever"
        registerReceiver(FoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(FoundReceiver, filter);

        //Start Discovering local bluetooth signals
        mBluetoothAdapter.startDiscovery();
        Toast.makeText(getApplicationContext(), "Searching for nearby devices", Toast.LENGTH_SHORT).show();

    }

    private final BroadcastReceiver FoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a new device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //Determine if found device is part of the Mul Community
                if(device.getName() != null){
                    //"MulTooth" is the current way to uniquely identify another Mul user
                    if(device.getName().contains("MulTooth")){
                        //If it is, return the device's address
                        //This currently implements a "first found" algorithm. A better solution
                        //might be to get a list of all local community members, then return the one with the
                        //strongest signal strength.

                        // Get the BLuetoothDevice object
                        BluetoothDevice BTDevice = mBluetoothAdapter.getRemoteDevice(device.getAddress());
                        // Attempt to connect to the device
                        BTService.connect(BTDevice);
                        mBluetoothAdapter.cancelDiscovery();
                        unregisterReceiver(FoundReceiver);
                    }
                }
                Log.d("Receiver", "Bluetooth Device Found: " + device.getName());
            }

            // When discovery cycle finished
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(((BTService.getState() == BTCommunicationService.STATE_CONNECTED) || (BTService.getState() == BTCommunicationService.STATE_CONNECTING)))
                {
                    //The bluetooth service is attempting to make a connection or is already connected
                    Toast.makeText(getApplicationContext(), "Finished Searching: Connection Made", Toast.LENGTH_LONG).show();

                    // get initial values here even though we won't have connected to the wifi, and actually might still fail in that attempt
                    // sessionStartRxBytes = TrafficStats.getTotalRxBytes();
                    // sessionStartTxBytes = TrafficStats.getTotalTxBytes();
                    // TODO: this actually starts the updater
                    //  timerHandler.post(updater);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Finished Searching: No Mul Users Available", Toast.LENGTH_LONG).show();
                }


            }
        }
    };
}
