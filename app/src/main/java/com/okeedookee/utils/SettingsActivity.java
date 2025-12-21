package com.okeedookee.utils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText etFileUrl;
    private TextInputEditText etToken;
    private TextInputEditText etInterval;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        etFileUrl = findViewById(R.id.etFileUrl);
        etToken = findViewById(R.id.etToken);
        etInterval = findViewById(R.id.etInterval);
        btnSave = findViewById(R.id.btnSave);

        loadCurrentSettings();

        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void loadCurrentSettings() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        String fileUrl = prefs.getString(MainActivity.KEY_FILE_URL, "");
        String token = prefs.getString(MainActivity.KEY_TOKEN, "");
        int interval = prefs.getInt(MainActivity.KEY_INTERVAL, 15);

        etFileUrl.setText(fileUrl);
        etToken.setText(token);
        etInterval.setText(String.valueOf(interval));
    }

    private void saveSettings() {
        String fileUrl = etFileUrl.getText().toString().trim();
        String token = etToken.getText().toString().trim();
        String intervalStr = etInterval.getText().toString().trim();

        if (fileUrl.isEmpty()) {
            Toast.makeText(this, "GitHub File URL cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (token.isEmpty()) {
            Toast.makeText(this, "GitHub Token cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (intervalStr.isEmpty()) {
            Toast.makeText(this, "Check Interval cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        int interval;
        try {
            interval = Integer.parseInt(intervalStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid interval value", Toast.LENGTH_SHORT).show();
            return;
        }

        if (interval < 1) {
            Toast.makeText(this, "Interval must be at least 15 minutes (Android requirement)", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(MainActivity.KEY_FILE_URL, fileUrl)
                .putString(MainActivity.KEY_TOKEN, token)
                .putInt(MainActivity.KEY_INTERVAL, interval)
                .apply();

        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
        finish(); // Return to MainActivity
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
