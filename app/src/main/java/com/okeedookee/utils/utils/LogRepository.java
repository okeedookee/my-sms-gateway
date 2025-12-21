package com.okeedookee.utils.utils;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogRepository {
    private static final String FILENAME = "app_logs.json";
    private static final Gson gson = new Gson();

    public static void addLog(Context context, String message) {
        List<AppLog> list = new ArrayList<>(getLogs(context));
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        list.add(new AppLog(time, System.currentTimeMillis(), message)); // Add to end

        // Limit to last 100 logs
        if (list.size() > 100) {
            list = list.subList(0, 100);
        }

        saveLogs(context, list);
    }

    public static List<AppLog> getLogs(Context context) {
        File file = new File(context.getFilesDir(), FILENAME);
        if (!file.exists())
            return Collections.emptyList();

        try {
            byte[] encoded = Files.readAllBytes(file.toPath());
            String json = new String(encoded, StandardCharsets.UTF_8);
            Type type = new TypeToken<List<AppLog>>() {
            }.getType();
            List<AppLog> logs = gson.fromJson(json, type);
            return logs != null ? logs : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static void saveLogs(Context context, List<AppLog> logs) {
        File file = new File(context.getFilesDir(), FILENAME);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(gson.toJson(logs).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearLogs(Context context) {
        saveLogs(context, Collections.emptyList());
    }
}
