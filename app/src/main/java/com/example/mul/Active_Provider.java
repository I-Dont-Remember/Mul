package com.example.mul;

import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

public class Active_Provider extends AppCompatActivity {
    private String TAG = ProviderActivity.class.getSimpleName();
    private Runnable updater;
    private long sessionStartRxBytes = 0;
    private long sessionStartTxBytes = 0;
    public final Handler timerHandler = new Handler();

    private long deltaTX_prev, deltaRX_prev;
    private boolean start_detecting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_provider);
        MainActivity.providing = true;

        // makes the assumption that once hotspot is on, Wifi is off and all traffic is through mobile network.
        // The provider can still use phone though, so that will throw off traffic readings versus actual client usage.
        sessionStartRxBytes = TrafficStats.getMobileRxBytes();
        sessionStartTxBytes = TrafficStats.getMobileTxBytes();
        Log.i(TAG, String.format("startRx: %d startTx: %d", sessionStartRxBytes, sessionStartTxBytes));

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

                if((deltaTX_prev - deltaTx) < 50)
                    start_detecting = true;

                if(start_detecting)
                    tv.setText(getFriendlyUsage(deltaTx, deltaRx));
                
                // TODO: change to a longer time but can leave at 1 second while building app
                timerHandler.postDelayed(this, 1000);
            }
        };

        // this actually starts the updater
        timerHandler.post(updater);
    }

    public void onClickStop(View view) {
        Log.i(TAG, "attempt to stop hotspot");
        if (isHotspotEnabled()) {
            Intent intent = new Intent(getString(R.string.intent_action_turnoff));
            ProviderActivity.sendImplicitBroadcast(this,intent);

            // shut off data update
            // TODO: try catch
            timerHandler.removeCallbacks(updater);
        } else {
            Toast.makeText(this, "! Looks like hotspot is not on !", Toast.LENGTH_SHORT).show();
        }

        Intent i = new Intent(getApplicationContext(), ProviderActivity.class);
        MainActivity.providing = false;
        startActivity(i);
    }

    public void onClickUpdate(View view) {
        Intent i = new Intent(getApplicationContext(), Session_Limits.class);
        startActivity(i);
    }

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

    private String formatDataUsed(long dataUsed) {
        if (dataUsed > (1024*1024)) {
            return String.format("%d.%d MB", dataUsed / (1024*1024), dataUsed % 1024*1024);
        } else if (dataUsed > 1024) {
            return String.format("%d KB", dataUsed / 1024);
        } else {
            return String.format("%d B", dataUsed);
        }
    }

    private String getFriendlyUsage(long tx, long rx) {
        return String.format("deltaTx: %s deltaRx: %s", formatDataUsed(tx), formatDataUsed(rx));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        timerHandler.removeCallbacks(updater);
    }


    public void onClickStats(View view) {
        // fill in the textview with current stats info
        TextView tv = (TextView) findViewById(R.id.stats);
        long deltaTx = 0;
        long deltaRx = 0;

        if (isHotspotEnabled()) {
            // no devices are connected, not purposely running any services but still have data exchange on order of hundreds of KB per minute
            long currentTx = TrafficStats.getMobileTxBytes();
            long currentRx = TrafficStats.getMobileRxBytes();
            Log.i(TAG, String.format("currentTx: %d currentRx: %d", currentTx, currentRx));
            Log.i(TAG, String.format("startTx: %d startRx: %d", sessionStartTxBytes, sessionStartRxBytes));
            deltaTx = currentTx - sessionStartTxBytes;
            deltaRx = currentRx - sessionStartRxBytes;

        }
//        tv.setText(String.format("DeltaTx: %s DeltaRx: %s", getFriendlyFormat(), deltaRx));
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
}
