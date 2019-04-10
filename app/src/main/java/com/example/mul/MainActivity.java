package com.example.mul;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends PermissionsActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SHOW_ICON = "show_icon" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickClient(View view) {
        Intent i = new Intent(getApplicationContext(), ClientActivity.class);
        startActivity(i);
    }

    public void onClickProvider(View view) {
        Intent i = new Intent(getApplicationContext(), ProviderActivity.class);
        startActivity(i);
    }

    @Override
    void onPermissionsOkay() {

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
