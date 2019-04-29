package com.example.mul;

import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Active_Client extends AppCompatActivity {
    // TODO: this is same as Provider, is there anyway to abstract all this crap?
    private long sessionStartRxBytes = 0;
    private long sessionStartTxBytes = 0;
    private final Handler timerHandler = new Handler();
    private Runnable updater;

    private String TAG = ClientActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_client);
        MainActivity.connected = true;

        final TextView tv = findViewById(R.id.stats);
        tv.post(new Runnable() {
            @Override
            public void run() {
                tv.setText(getFriendlyUsage(0,0));
            }
        });

        updater = new Runnable() {
            @Override
            public void run() {
                TextView tv = findViewById(R.id.stats);
                long currentTx = TrafficStats.getTotalTxBytes();
                long currentRx = TrafficStats.getTotalRxBytes();
                Log.i(TAG, String.format("currentTx: %d currentRx: %d", currentTx, currentRx));
                Log.i(TAG, String.format("startTx: %d startRx: %d", sessionStartTxBytes, sessionStartRxBytes));
                long deltaTx = currentTx - sessionStartTxBytes;
                long deltaRx = currentRx - sessionStartRxBytes;
                tv.setText(getFriendlyUsage(deltaTx, deltaRx));
                // TODO: change to a longer time but can leave at 1 second while building app
                timerHandler.postDelayed(this, 1000);
            }
        };

        // TODO: here for testing functionality, remove me and uncomment section when bluetooth connection made
        sessionStartRxBytes = TrafficStats.getTotalRxBytes();
        sessionStartTxBytes = TrafficStats.getTotalTxBytes();
        timerHandler.post(updater);
    }


    public void onClickDisconnect(View view) {
        Log.d(TAG, "in handle disconnect");
        timerHandler.removeCallbacks(updater);

        // TODO: get working
        // this can't work because there's no BTService in this activity, we would have to use some sort of singleton pattern to share variables between activities
//        if (BTService != null){
//            BTService.stop();
//        }
        ClientActivity.common.forgetCurrentNetwork(getApplicationContext());

        finish();
    }

    public void onClickTopUp(View view) {
        Intent i = new Intent(getApplicationContext(), topUp.class);
        startActivity(i);
    }

    // TODO: ahhhh don't hate me these are copied directly from Provider, they need to be abstracted to atone for this shameful behaviour
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        timerHandler.removeCallbacks(updater);
        Log.d(TAG, "destroying active client");
    }

}
