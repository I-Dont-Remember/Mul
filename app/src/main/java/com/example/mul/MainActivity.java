package com.example.mul;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

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
}
