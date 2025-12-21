package com.okeedookee.utils.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.okeedookee.utils.utils.LogRepository;
import com.okeedookee.utils.worker.SmsWorker;

public class SmsAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogRepository.addLog(context, "Alarm received. Triggering SMS check...");

        // Trigger the worker immediately
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SmsWorker.class).build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                "SmsCheckWorker_OneTime",
                ExistingWorkPolicy.REPLACE,
                workRequest);
    }
}
