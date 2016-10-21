package com.autocounting.autocounting.models;

import android.util.Log;

public class Receipt {

    public static final String TAG = "Receipt";
    private String filename;

    public Receipt() {
        generateFilename();
    }

    private void generateFilename() {
        filename = String.valueOf(System.currentTimeMillis() / 10L);
        Log.i(TAG, "Generated " + filename);
    }

    public String getFilename() {
        return filename;
    }
}
