package io.lucalabs.expenses.network.upload;

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

import io.lucalabs.expenses.models.Receipt;

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
                uploadReceipts();
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

    private void uploadReceipts() {
        for (Receipt receipt : Receipt.find(Receipt.class, "(status = 'PENDING' OR status = 'UPLOADED') AND filename IS NOT NULL")) {
            Log.w("RPath", "called from upload service " + receipt.getFirebase_ref());
            new UploadReceiptTask(this).uploadReceipt(receipt);
        }
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
