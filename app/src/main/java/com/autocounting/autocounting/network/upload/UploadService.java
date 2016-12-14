package com.autocounting.autocounting.network.upload;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import com.autocounting.autocounting.models.Receipt;

import java.io.File;

public class UploadService extends Service implements UploadResponseHandler {
    private final static String TAG = "UploadService";
    private ServiceHandler serviceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (UploadService.this) {
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
        File receiptFolder = Receipt.getReceiptFolder();

        Log.i(TAG, Receipt.count(Receipt.class) + " receipts waiting for upload");
        if(Receipt.count(Receipt.class) > 0) {
            for (Receipt receipt : Receipt.listAll(Receipt.class)) {
                Log.i(TAG, "One fine receipt");
                new UploadReceiptTask(this).uploadReceipt(receipt);
            }
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
