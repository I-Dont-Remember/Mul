package com.example.mul;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private EditText ssidEdit;
    private EditText passwordEdit;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ssidEdit = findViewById(R.id.ssidEdit);
        passwordEdit = findViewById(R.id.passwordEdit);

        String ssid = readSetting("ssid");
        String password = readSetting("password");
        ssidEdit.setText(ssid);
        passwordEdit.setText(password);
    }

    public void onClickSubmit(View view) {
        String ssid = ssidEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        Toast.makeText(this, String.format("%s and %s", ssid, password), Toast.LENGTH_SHORT).show();
        writeSetting("ssid", ssid);
        writeSetting("password", password);
    }

    public void writeSetting(String key, String data) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(SettingsActivity.class.getSimpleName(), 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(key, data);
        editor.commit();
    }

    public String readSetting(String key) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(SettingsActivity.class.getSimpleName(), 0);
        return settings.getString(key, "");
    }
}
