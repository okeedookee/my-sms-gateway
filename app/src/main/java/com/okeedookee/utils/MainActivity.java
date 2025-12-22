package com.okeedookee.utils;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.ExistingWorkPolicy;
// import androidx.work.ExistingPeriodicWorkPolicy; // Unused
// import androidx.work.PeriodicWorkRequest; // Unused
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.okeedookee.utils.config.GitHubConfig;
import com.okeedookee.utils.ui.LogAdapter;
import com.okeedookee.utils.utils.LogRepository;
import com.okeedookee.utils.worker.SmsWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnSettings;
    private Button btnStart, btnStop;
    private RecyclerView rvLogs;
    private Handler logRefreshHandler;
    private Runnable logRefreshRunnable;
    private android.widget.TextView tvNextRunTimer;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private boolean isWorkerRunning = false;

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String WORK_NAME = "SmsCheckWorker";

    public static final String PREFS_NAME = "SmsGatewayPrefs";
    public static final String KEY_FILE_URL = "file_url";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_INTERVAL = "check_interval";
    public static final String KEY_LAST_RUN_TIME = "last_run_time";
    public static final String KEY_IS_SERVICE_RUNNING = "is_service_running";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SharedPreferences with GitHubConfig defaults
        initializeDefaultPreferences();

        // Clear all existing logs on app start
        LogRepository.clearLogs(this);

        btnSettings = findViewById(R.id.btnSettings);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        tvNextRunTimer = findViewById(R.id.tvNextRunTimer);
        rvLogs = findViewById(R.id.rvLogs);

        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        LogAdapter adapter = new LogAdapter(LogRepository.getLogs(this));
        rvLogs.setAdapter(adapter);

        checkPermissions();
        updateButtonStates();

        btnStart.setOnClickListener(v -> {
            // Clear logs when starting service
            LogRepository.clearLogs(this);
            refreshLogs();
            startWorker();
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
            updateButtonStates();
        });

        btnStop.setOnClickListener(v -> {
            stopWorker();
            Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
            updateButtonStates();
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        android.widget.TextView tvDisclaimer = findViewById(R.id.tvDisclaimer);
        tvDisclaimer.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("mailto:tookeedookee@gmail.com"));
            startActivity(browserIntent);
        });

        // Observe work status changes - REMOVED as we manage state manually now
        // updateButtonStates() called manually
        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData("SmsCheckWorker_OneTime")
                .observe(this, workInfos -> {
                    if (workInfos != null && !workInfos.isEmpty()) {
                        WorkInfo workInfo = workInfos.get(0);
                        if (workInfo.getState() == WorkInfo.State.RUNNING
                                || workInfo.getState() == WorkInfo.State.ENQUEUED) {
                            isWorkerRunning = true;
                        } else {
                            isWorkerRunning = false;
                        }
                        updateTimer(); // Update UI immediately
                    }
                });

        // Setup periodic log refresh
        logRefreshHandler = new Handler(Looper.getMainLooper());
        logRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshLogs();
                logRefreshHandler.postDelayed(this, 2000); // Refresh every 2 seconds
            }
        };
        logRefreshHandler.post(logRefreshRunnable);

        // Setup timer for next run countdown
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimer();
                timerHandler.postDelayed(this, 1000); // Update every second
            }
        };
        timerHandler.post(timerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLogs();
        updateButtonStates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logRefreshHandler != null && logRefreshRunnable != null) {
            logRefreshHandler.removeCallbacks(logRefreshRunnable);
        }
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private void updateTimer() {
        if (btnStart.isEnabled()) {
            tvNextRunTimer.setText("Service Stopped");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastRunTime = prefs.getLong(KEY_LAST_RUN_TIME, 0);
        int intervalMinutes = prefs.getInt(KEY_INTERVAL, 15);

        if (lastRunTime == 0) {
            if (isWorkerRunning) {
                tvNextRunTimer.setText("Status: Running check...");
            } else {
                tvNextRunTimer.setText("Next run in: Calculating...");
            }
            return;
        }

        if (isWorkerRunning) {
            tvNextRunTimer.setText("Status: Running check...");
            return;
        }

        long nextRunTime = lastRunTime + (intervalMinutes * 60 * 1000L);
        long currentTime = System.currentTimeMillis();
        long diff = nextRunTime - currentTime;

        if (diff > 0) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.MINUTES.toSeconds(minutes);
            tvNextRunTimer.setText(String.format("Next run in: %02d:%02d", minutes, seconds));
        } else {
            tvNextRunTimer.setText("Status: Waiting for system execution...");
        }
    }

    private void refreshLogs() {
        if (rvLogs.getAdapter() instanceof LogAdapter) {
            LogAdapter adapter = (LogAdapter) rvLogs.getAdapter();
            adapter.updateLogs(LogRepository.getLogs(this));
        }
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.SEND_SMS
        };

        List<String> missing = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }

        if (!missing.isEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    private void updateButtonStates() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isRunning = prefs.getBoolean(KEY_IS_SERVICE_RUNNING, false);

        if (isRunning) {
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
        } else {
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        }
    }

    private void startWorker() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_SERVICE_RUNNING, true).apply();

        // Update last run time to now
        prefs.edit().putLong(KEY_LAST_RUN_TIME, System.currentTimeMillis()).apply();

        // Trigger immediate run
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SmsWorker.class).build();
        WorkManager.getInstance(this).enqueueUniqueWork(
                "SmsCheckWorker_OneTime",
                ExistingWorkPolicy.REPLACE,
                workRequest);

        LogRepository.addLog(this, "Service started (Exact Timing Approach).");
        updateButtonStates();
    }

    private void stopWorker() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_SERVICE_RUNNING, false).apply();

        com.okeedookee.utils.utils.AlarmScheduler.cancelAlarm(this);
        WorkManager.getInstance(this).cancelUniqueWork("SmsCheckWorker_OneTime"); // Cancel any pending immediate work

        LogRepository.addLog(this, "Service stopped by user.");
        updateButtonStates();
    }

    /**
     * Initialize SharedPreferences with default values from GitHubConfig
     * Only sets defaults if preferences are empty (first launch)
     */
    private void initializeDefaultPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!prefs.contains(KEY_FILE_URL)) {
            prefs.edit()
                    .putString(KEY_FILE_URL, GitHubConfig.FILE_URL)
                    .putString(KEY_TOKEN, GitHubConfig.TOKEN)
                    .putInt(KEY_INTERVAL, 15) // Default 15 minutes
                    .apply();
        }
    }
}
