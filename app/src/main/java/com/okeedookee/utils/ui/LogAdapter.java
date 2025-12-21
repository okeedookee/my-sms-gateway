package com.okeedookee.utils.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.okeedookee.utils.R;
import com.okeedookee.utils.utils.AppLog;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private List<AppLog> logs;

    public LogAdapter(List<AppLog> logs) {
        this.logs = logs;
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvTime;
        public final TextView tvMessage;

        public LogViewHolder(View view) {
            super(view);
            tvTime = view.findViewById(R.id.tvTime);
            tvMessage = view.findViewById(R.id.tvMessage);
        }
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        AppLog log = logs.get(position);
        holder.tvTime.setText(log.getTimestamp());
        holder.tvMessage.setText(log.getMessage());
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public void updateLogs(List<AppLog> newLogs) {
        this.logs = newLogs;
        notifyDataSetChanged();
    }
}
