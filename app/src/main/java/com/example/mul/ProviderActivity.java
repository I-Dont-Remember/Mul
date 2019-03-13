package com.example.mul;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ProviderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);

        ActionBar b = getSupportActionBar();
        if (b != null) {
            b.setDisplayShowHomeEnabled(true);
        }

    }
}
