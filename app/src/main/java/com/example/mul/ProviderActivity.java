package com.example.mul;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;
import android.net.TrafficStats;

import com.google.android.gms.common.util.Strings;

public class ProviderActivity extends AppCompatActivity {
    //Get Access to common methods
    Client_Provider_Common common = new Client_Provider_Common();

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

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

    //UI Elements
    private EditText ET_SSID, ET_Password;

    // Name of the connected device
    private String mConnectedDeviceName = null;

    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the chat services
    private BTCommunicationService BTService = null;

    private String TAG = ProviderActivity.class.getSimpleName();

    private long sessionStartRxBytes = 0;
    private long sessionStartTxBytes = 0;
    private final Handler timerHandler = new Handler();
    private Runnable updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        mBluetoothAdapter.setName("MulTooth79797");

        common.ensureDiscoverable(mBluetoothAdapter, getApplicationContext());



        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        updater = new Runnable() {
            @Override
            public void run() {
                TextView tv = findViewById(R.id.stats);
                long currentTx = TrafficStats.getMobileTxBytes();
                long currentRx = TrafficStats.getMobileRxBytes();
                Log.i(TAG, String.format("currentTx: %d currentRx: %d", currentTx, currentRx));
                Log.i(TAG, String.format("startTx: %d startRx: %d", sessionStartTxBytes, sessionStartRxBytes));
                long deltaTx = currentTx - sessionStartTxBytes;
                long deltaRx = currentRx - sessionStartRxBytes;
                tv.setText(getFriendlyUsage(deltaTx, deltaRx));
                // TODO: change to a longer time but can leave at 1 second while building app
                timerHandler.postDelayed(this, 1000);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (BTService == null) setupChat();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (BTService != null) BTService.stop();
        timerHandler.removeCallbacks(updater);
    }

    // The Handler that gets information back from the BluetoothChatService

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            Log.i(TAG, "Bluetooth mHandler");
            switch (msg.what) {
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    //When a connection is made, create the concatenated string containing hotspot credentials
                    String wifiString = "Mul-123" + "." + "mulrocks";
                    //Then send that to the client so the client can connect
                    //sendMessage1(wifiString);
                    common.sendMessage1(wifiString, BTService, mOutStringBuffer, getApplicationContext());

                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(EXTRA_DEVICE_ADDRESS);

                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                    // Attempt to connect to the device
                    BTService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public void onClickProvide(View view) {
        Log.i(TAG, "clicked provide");
        if (!isHotspotEnabled()) {
            // actually starting the hotspot
            Intent intent = new Intent(getString(R.string.intent_action_turnon));
            sendImplicitBroadcast(this,intent);

            // makes the assumption that once hotspot is on, Wifi is off and all traffic is through mobile network.
            // The provider can still use phone though, so that will throw off traffic readings versus actual client usage.
            sessionStartRxBytes = TrafficStats.getMobileRxBytes();
            sessionStartTxBytes = TrafficStats.getMobileTxBytes();
            Log.i(TAG, String.format("startRx: %d startTx: %d", sessionStartRxBytes, sessionStartTxBytes));
        } else {
            Toast.makeText(this, "! Looks like hotspot is already on !", Toast.LENGTH_SHORT).show();
        }

        timerHandler.post(updater);

    }

//    public void connect(View v) {
//        Log.i(TAG, "connecting bluetooth");
//
//        if (BTService.getState() == BTCommunicationService.STATE_CONNECTED) {
//            Toast.makeText(this, "Already Connected", Toast.LENGTH_LONG);
//        } else {
//            Intent serverIntent = new Intent(this, DiscoverDevices.class);
//            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
//        }
//    }

    public boolean isHotspotEnabled() {
        // assumes false if any errors occur, is that the more dangerous option?
        boolean isEnabled = false;
        // Check if hotspot on with reflection
        // https://android.googlesource.com/platform/frameworks/base/+/refs/heads/pie-release-2/wifi/java/android/net/wifi/WifiManager.java#2133
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Method[] methods = wifiManager.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("isWifiApEnabled")) {
                try {
                    isEnabled = (boolean) method.invoke(wifiManager);
                } catch (Exception e) {
                    Log.e(TAG, "Failed checking AP status: " + e.toString());
                    e.printStackTrace();
                    isEnabled = false;
                }
            }
        }
        //Error isWifiApEnabled not found
        return isEnabled;
    }

    private static void sendImplicitBroadcast(Context ctxt, Intent i) {
        PackageManager pm=ctxt.getPackageManager();
        List<ResolveInfo> matches=pm.queryBroadcastReceivers(i, 0);

        for (ResolveInfo resolveInfo : matches) {
            Intent explicit=new Intent(i);
            ComponentName cn=
                    new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                            resolveInfo.activityInfo.name);

            explicit.setComponent(cn);
            ctxt.sendBroadcast(explicit);
        }
    }

    public void onClickStop(View view) {
        Log.i(TAG, "attempt to stop hotspot");
        if (isHotspotEnabled()) {
            Intent intent = new Intent(getString(R.string.intent_action_turnoff));
            sendImplicitBroadcast(this,intent);

            // shut off data update
            // TODO: try catch
            timerHandler.removeCallbacks(updater);
        } else {
            Toast.makeText(this, "! Looks like hotspot is not on !", Toast.LENGTH_SHORT).show();
        }

    }

//    public void onClickStats(View view) {
//        // fill in the textview with current stats info
//        TextView tv = (TextView) findViewById(R.id.stats);
//        long deltaTx = 0;
//        long deltaRx = 0;
//
//        if (isHotspotEnabled()) {
//            // no devices are connected, not purposely running any services but still have data exchange on order of hundreds of KB per minute
//            long currentTx = TrafficStats.getMobileTxBytes();
//            long currentRx = TrafficStats.getMobileRxBytes();
//            Log.i(TAG, String.format("currentTx: %d currentRx: %d", currentTx, currentRx));
//            Log.i(TAG, String.format("startTx: %d startRx: %d", sessionStartTxBytes, sessionStartRxBytes));
//            deltaTx = currentTx - sessionStartTxBytes;
//            deltaRx = currentRx - sessionStartRxBytes;
//
//        }
//        tv.setText(String.format("DeltaTx: %s DeltaRx: %s", getFriendlyFormat(), deltaRx));
//    }

    private String formatDataUsed(long dataUsed) {
        if (dataUsed > (1000*1000)) {
            return String.format("%d MB", dataUsed / (1000*1000));
        } else if (dataUsed > 1000) {
            return String.format("%d KB", dataUsed / 1000);
        } else {
            return String.format("%d B", dataUsed);
        }
    }

    private String getFriendlyUsage(long tx, long rx) {
        return String.format("deltaTx: %s deltaRx: %s", formatDataUsed(tx), formatDataUsed(rx));
    }
}

// Initial tracking, seems like there's a weirdly large amount of data right away then after a bit it trails off to almost no change as one might have expected for the entire duration
//    2019-04-11 11:49:14.878 29302-29302/com.example.mul I/ProviderActivity: clicked provide
//    2019-04-11 11:49:14.903 29302-29302/com.example.mul I/ProviderActivity: startRx: 44942294 startTx: 4495290
//    2019-04-11 11:49:14.917 29302-29302/com.example.mul I/HotSpotIntentReceiver: Received intent with action: com.example.mul.TURN_ON
//    2019-04-11 11:49:14.929 29302-29302/com.example.mul I/MagicActivity: attempting to turn off hotspot
//    2019-04-11 11:49:15.043 29302-29302/com.example.mul W/ActivityThread: handleWindowVisibility: no activity for token android.os.BinderProxy@afad04d
//    2019-04-11 11:49:15.065 29302-29302/com.example.mul I/PermissionsActivity: settingPermissions
//    2019-04-11 11:49:15.066 29302-29302/com.example.mul I/PermissionsActivity: location permssion
//    2019-04-11 11:49:15.078 29302-29302/com.example.mul I/MagicActivity: onCreate
//    2019-04-11 11:49:15.118 29302-29512/com.example.mul I/ContentValues: Received start intent
//    2019-04-11 11:49:15.118 29302-29512/com.example.mul I/ContentValues: Action/data to turn on hotspot
//    2019-04-11 11:49:15.121 29302-29512/com.example.mul I/MyOreoWifiManager: starting tethering
//    2019-04-11 11:49:15.123 29302-29512/com.example.mul I/CallbackMaker: in constructor
//    2019-04-11 11:49:15.139 29302-29512/com.example.mul I/CallbackMaker: trying to generate constructor
//    2019-04-11 11:49:15.558 29302-29512/com.example.mul I/com.example.mu: The ClassLoaderContext is a special shared library.
//    2019-04-11 11:49:15.566 29302-29512/com.example.mul I/ConnectivityManager: startTethering caller:com.example.mul
//    2019-04-11 11:49:17.371 29302-29302/com.example.mul I/ProviderActivity: currentTx: 7262672 currentRx: 50391178
//    2019-04-11 11:49:17.372 29302-29302/com.example.mul I/ProviderActivity: startTx: 4495290 startRx: 44942294
//    2019-04-11 11:49:28.842 29302-29302/com.example.mul I/ProviderActivity: currentTx: 7730001 currentRx: 50809257
//    2019-04-11 11:49:28.843 29302-29302/com.example.mul I/ProviderActivity: startTx: 4495290 startRx: 44942294
//    2019-04-11 11:49:35.305 29302-29302/com.example.mul I/ProviderActivity: currentTx: 7730001 currentRx: 50809257
//    2019-04-11 11:49:35.306 29302-29302/com.example.mul I/ProviderActivity: startTx: 4495290 startRx: 44942294
//    2019-04-11 11:49:38.513 29302-29302/com.example.mul I/ProviderActivity: currentTx: 7730001 currentRx: 50809257
//    2019-04-11 11:49:38.513 29302-29302/com.example.mul I/ProviderActivity: startTx: 4495290 startRx: 44942294
//    2019-04-11 11:49:40.228 29302-29302/com.example.mul I/ProviderActivity: currentTx: 7733014 currentRx: 50810894
//    2019-04-11 11:49:40.229 29302-29302/com.example.mul I/ProviderActivity: startTx: 4495290 startRx: 44942294
//    2019-04-11 11:49:42.873 29302-29302/com.example.mul I/ProviderActivity: currentTx: 7735745 currentRx: 50815975
//    2019-04-11 11:49:42.874 29302-29302/com.example.mul I/ProviderActivity: startTx: 4495290 startRx: 44942294
//    2019-04-11 11:49:43.931 29302-29302/com.example.mul I/ProviderActivity: currentTx: 7735745 currentRx: 50815975
//    2019-04-11 11:49:43.932 29302-29302/com.example.mul I/ProviderActivity: startTx: 4495290 startRx: 44942294
//    2019-04-11 11:50:04.972 29302-29302/com.example.mul I/ProviderActivity: currentTx: 7735745 currentRx: 50815975
//    2019-04-11 11:50:04.973 29302-29302/com.example.mul I/ProviderActivity: startTx: 4495290 startRx: 44942294
//    2019-04-11 11:51:27.346 29302-29302/com.example.mul I/ProviderActivity: currentTx: 7741947 currentRx: 50818400
//    2019-04-11 11:51:27.347 29302-29302/com.example.mul I/ProviderActivity: startTx: 4495290 startRx: 44942294
// From 49:17 to 51:27 = 110s
// TX: 7741947 - 7262672 ~  480000 480KB avg ~ 5KB/s
// RX: 50818400 - 50391178 ~ 430000 430KB avg ~ 4KB/s