package com.autocounting.autocounting.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import com.autocounting.autocounting.models.Receipt;
import com.autocounting.autocounting.network.upload.UploadReceiptTask;
import com.autocounting.autocounting.network.upload.UploadResponseHandler;

import java.io.File;

public class UploadManager extends Service implements UploadResponseHandler {
    private final static String TAG = "UploadManager";
    private ServiceHandler serviceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (UploadManager.this) {
                if(NetworkManager.networkIsAvailable(getContext()))
                    uploadQueue();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting ...");

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        return Service.START_NOT_STICKY;
    }

    public void onCreate() {
        HandlerThread handlerThread = new HandlerThread("ServiceStartArgs", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        serviceHandler = new ServiceHandler(handlerThread.getLooper());
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Shutting down ...");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void uploadQueue() {
        Log.i(TAG, "Running upload queue");
        File imageFolder = Receipt.getReceiptFolder();

        if (imageFolder.list() != null) {
            Log.i(TAG, imageFolder.list().length + " images in " + imageFolder.getAbsolutePath());
            for (String imageAddress : imageFolder.list())
                new UploadReceiptTask(this).uploadReceipt(new Receipt(imageFolder, imageAddress));
        } else {
            Log.i(TAG, imageFolder.getAbsolutePath() + " is empty");
        }

        Log.i(TAG, "Queue uploaded");
    }

    // UploadResponseHandler

    @Override
    public void onFileUploadStarted(String filename) {
    }

    @Override
    public void onFileUploadFinished(String result) {

    }

    @Override
    public void onFileUploadFailed() {
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
