package com.example.mul;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class MainActivity extends PermissionsActivity {
    private final int REQUEST_READ_PHONE_STATE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SHOW_ICON = "show_icon" ;
    public static boolean providing = false;
    public static boolean connected = false;
    public static String IMEINumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView settings = findViewById(R.id.settingView);
        settings.setText(String.format("%s - %s", readSetting("ssid"), readSetting("password")));

        // get the IMEI number of the device
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            finish();
        } else {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            IMEINumber = tm.getImei();
            else
            IMEINumber = tm.getDeviceId();
        }
    }

    public void onClickClient(View view) {
        Intent i;

        if(!connected)
            i = new Intent(getApplicationContext(), ClientActivity.class);
        else
            i = new Intent(getApplicationContext(), Active_Client.class);

        startActivity(i);
    }

    public void onClickProvider(View view) {
        Intent i;

        if(!providing)
            i = new Intent(getApplicationContext(), ProviderActivity.class);
        else
            i = new Intent(getApplicationContext(), Active_Provider.class);

        startActivity(i);
    }

    @Override
    void onPermissionsOkay() {

    }

    public void onClickSettings(View view) {
        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(i);
    }


    public String readSetting(String key) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(SettingsActivity.class.getSimpleName(), 0);
        return settings.getString(key, "");
    }
//    public void onClickTurnOnAction(View v){
//        Intent intent = new Intent(getString(R.string.intent_action_turnon));
//        sendImplicitBroadcast(this,intent);
//    }

//    public void onClickTurnOffAction(View v){
//        Intent intent = new Intent(getString(R.string.intent_action_turnoff));
//        sendImplicitBroadcast(this,intent);
//    }
}
