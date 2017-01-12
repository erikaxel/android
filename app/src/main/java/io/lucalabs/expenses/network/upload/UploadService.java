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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.models.User;
import io.lucalabs.expenses.network.database.ReceiptDatabase;

public class UploadService extends Service {
    private final static String TAG = "UploadService";
    private ServiceHandler serviceHandler;
    private Receipt nextReceipt;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (UploadService.this) {
                handleReceipts();
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

    private void handleReceipts() {
        // Upload receipts
        for (Receipt receipt : Receipt.find(Receipt.class, "(status = 'PENDING' OR status = 'UPLOADED') AND filename IS NOT NULL")) {
            new UploadReceiptTask(this).uploadReceipt(receipt);
        }

        // Set interpreted receipt status to parsed
        for (Receipt receipt : Receipt.find(Receipt.class, "status = 'POSTED'")) {
            nextReceipt = receipt;
            ReceiptDatabase.getUserReference(User.getCurrentUser(),
                    EnvironmentManager.currentEnvironment(this))
                    .child(receipt.getFirebase_ref())
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Receipt onlineReceipt = dataSnapshot.getValue(Receipt.class);

                                                            if (onlineReceipt == null) {
                                                                nextReceipt.delete(this);
                                                                return;
                                                            }

                                                            if (onlineReceipt.isInterpreted())
                                                                nextReceipt.updateStatus(Receipt.Status.PARSED);
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                            // Do nothing
                                                        }
                                                    }

                    );
        }

        // Delete interpreted (finished) receipts from cache
        for (Receipt receipt : Receipt.find(Receipt.class, "status = 'PARSED'")) {
            Log.i(TAG, "Deleting " + receipt.getFirebase_ref());
            new DeleteReceiptTask(this, receipt).deleteReceipt();
        }
    }
}
