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

        Intent intent = new Intent(getString(R.string.intent_action_turnon));
        sendImplicitBroadcast(this,intent);
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

    public void onClickTurnOnData(View v){
        MagicActivity.useMagicActivityToTurnOn(this);
    }

    public void onClickTurnOffData(View v){
        MagicActivity.useMagicActivityToTurnOff(this);
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
}
