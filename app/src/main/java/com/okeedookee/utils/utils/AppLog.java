package com.okeedookee.utils.utils;

public class AppLog {
    private final String timestamp;
    private final long dateObj;
    private final String message;

    public AppLog(String timestamp, long dateObj, String message) {
        this.timestamp = timestamp;
        this.dateObj = dateObj;
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getDateObj() {
        return dateObj;
    }

    public String getMessage() {
        return message;
    }
}
