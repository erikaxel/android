package com.autocounting.autocounting.network.logging;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class LogUploader extends Thread {

    private FirebaseAnalytics analytics;
    private String uid;
    private Long timeSpent;
    private String filename;
    private String eventType;

    private final String TAG = "LOGUPLOADER";

    public LogUploader(FirebaseAnalytics analytics, String uid, Long timeSpent, String filename, String eventType){
        this.analytics = analytics;
        this.uid = uid;
        this.timeSpent = timeSpent;
        this.filename = filename;
        this.eventType = eventType;
    }

    public void run(){
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        bundle.putLong("time_taken", timeSpent);
        bundle.putString("filename", filename);
        bundle.putString("platform", "Android");
        analytics.logEvent(eventType, bundle);
    }
}
