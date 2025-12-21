package com.okeedookee.utils.worker;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.okeedookee.utils.MainActivity;
import com.okeedookee.utils.network.GithubApiService;
import com.okeedookee.utils.network.GithubFileResponse;
import com.okeedookee.utils.utils.LogRepository;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.okeedookee.utils.utils.AlarmScheduler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SmsWorker extends Worker {

    public SmsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        LogRepository.addLog(getApplicationContext(), "Worker started.");

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(MainActivity.PREFS_NAME,
                Context.MODE_PRIVATE);
        String fileUrl = prefs.getString(MainActivity.KEY_FILE_URL, "");
        String token = prefs.getString(MainActivity.KEY_TOKEN, "");

        LogRepository.addLog(getApplicationContext(), "Checking configuration...");

        if (fileUrl == null || fileUrl.isEmpty()) {
            LogRepository.addLog(getApplicationContext(),
                    "ERROR: GitHub File URL is not configured. Please go to Settings.");
            return Result.success(); // Return success to keep service running
        }

        if (token == null || token.isEmpty()) {
            LogRepository.addLog(getApplicationContext(),
                    "ERROR: GitHub Token is not configured. Please go to Settings.");
            return Result.success(); // Return success to keep service running
        }

        LogRepository.addLog(getApplicationContext(), "Configuration found. Proceeding...");

        // Parse the GitHub URL
        com.okeedookee.utils.utils.GitHubUrlParser.GitHubUrlInfo urlInfo = com.okeedookee.utils.utils.GitHubUrlParser
                .parse(fileUrl);

        if (urlInfo == null || !urlInfo.isValid()) {
            LogRepository.addLog(getApplicationContext(), "Error: Invalid GitHub URL format.");
            return Result.failure();
        }

        String owner = urlInfo.getOwner();
        String repo = urlInfo.getRepo();
        String path = urlInfo.getFilePath();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GithubApiService service = retrofit.create(GithubApiService.class);

        try {
            LogRepository.addLog(getApplicationContext(), "Checking GitHub for file...");
            Response<GithubFileResponse> response = service.getFileContent("Bearer " + token, owner, repo, path)
                    .execute();

            if (!response.isSuccessful()) {
                // ANY fetch error should NOT stop the service, just wait for next run
                String errorMsg = "ERROR: Unable to fetch file. Retrying next interval.";
                String errorDetails = "Error details: " + response.code() + " " + response.message();

                LogRepository.addLog(getApplicationContext(), errorMsg);
                LogRepository.addLog(getApplicationContext(), errorDetails);

                // Update last run time to ensure timer resets
                prefs.edit().putLong(MainActivity.KEY_LAST_RUN_TIME, System.currentTimeMillis()).apply();

                scheduleNextRun();
                return Result.success();
            }

            GithubFileResponse fileData = response.body();
            if (fileData == null || fileData.getContent() == null) {
                LogRepository.addLog(getApplicationContext(), "File is empty or content missing.");
                return Result.failure();
            }

            String cleanBase64 = fileData.getContent().replace("\n", "");
            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
            String csvContent = new String(decodedBytes, StandardCharsets.UTF_8);

            LogRepository.addLog(getApplicationContext(), "File found! Processing content...");

            String[] lines = csvContent.split("\n");
            int smsCount = 0;

            SmsManager smsManager = SmsManager.getDefault();

            for (String line : lines) {
                if (line == null || line.trim().isEmpty())
                    continue;

                String[] parts = line.split(",", 2);
                if (parts.length >= 2) {
                    String phone = parts[0].trim();
                    String message = parts[1].trim();

                    if (!phone.isEmpty() && !message.isEmpty()) {
                        try {
                            ArrayList<String> partsList = smsManager.divideMessage(message);
                            smsManager.sendMultipartTextMessage(phone, null, partsList, null, null);
                            smsCount++;
                            LogRepository.addLog(getApplicationContext(), "Sent to " + phone + ": \"" + message + "\"");
                        } catch (Exception e) {
                            LogRepository.addLog(getApplicationContext(),
                                    "Failed to send to " + phone + ": " + e.getMessage());
                        }
                    }
                }
            }

            if (smsCount > 0) {
                LogRepository.addLog(getApplicationContext(), "Deleting file from GitHub...");
                Response<Void> deleteResponse = service.deleteFile(
                        "Bearer " + token, owner, repo, path,
                        "Processed " + smsCount + " SMS messages",
                        fileData.getSha()).execute();

                if (deleteResponse.isSuccessful()) {
                    LogRepository.addLog(getApplicationContext(), "File deleted successfully.");
                } else {
                    LogRepository.addLog(getApplicationContext(), "Failed to delete file: " + deleteResponse.code());
                }
            } else {
                LogRepository.addLog(getApplicationContext(), "No valid SMS lines found in file.");
            }

            // Update last run time

            prefs.edit().putLong(MainActivity.KEY_LAST_RUN_TIME, System.currentTimeMillis()).apply();

        } catch (Exception e) {
            String errorMsg = "ERROR: Exception occurred. Retrying next interval.";
            String errorDetails = "Exception details: " + e.getMessage();

            LogRepository.addLog(getApplicationContext(), errorMsg);
            LogRepository.addLog(getApplicationContext(), errorDetails);

            // Update last run time to ensure timer resets
            prefs.edit().putLong(MainActivity.KEY_LAST_RUN_TIME, System.currentTimeMillis()).apply();

            scheduleNextRun();
            return Result.success();
        }

        scheduleNextRun();
        return Result.success();
    }

    private void scheduleNextRun() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(MainActivity.PREFS_NAME,
                Context.MODE_PRIVATE);
        boolean isServiceRunning = prefs.getBoolean(MainActivity.KEY_IS_SERVICE_RUNNING, false);

        if (isServiceRunning) {
            int intervalMinutes = prefs.getInt(MainActivity.KEY_INTERVAL, 15);
            AlarmScheduler.scheduleNextRun(getApplicationContext(), intervalMinutes * 60 * 1000L);
        }
    }

    /**
     * Cancels the periodic work to stop the service
     */
    private void cancelService() {
        androidx.work.WorkManager.getInstance(getApplicationContext())
                .cancelUniqueWork("SmsCheckWorker");
    }
}
