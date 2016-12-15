package com.autocounting.autocounting.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.autocounting.autocounting.network.upload.UploadService;

public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Network state changed");
        context.startService(new Intent(context, UploadService.class));
    }
}
