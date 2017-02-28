package io.lucalabs.expenses.network.upload;

import android.app.Service;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.models.Task;

public class UploadService extends Service {
    private final static String TAG = "UploadService";
    private ServiceHandler serviceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (UploadService.this) {
                handleTasks();
                handleReceipts();
            }
        }
    }

    private void handleTasks() {
        Query query = Inbox.all(getBaseContext());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (DataSnapshot taskSnapshot : dataSnapshot.child("tasks").getChildren()) {
                            Task task = taskSnapshot.getValue(Task.class);
                            boolean success = task.perform();

                            if (!success)
                                break;
                        }
                    }
                }).start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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
        Query pendingReceipts = Inbox.receiptsByStatus(getBaseContext(), "UPLOADED");
        pendingReceipts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (DataSnapshot receiptSnapshot : dataSnapshot.getChildren())
                            new UploadReceiptTask(getBaseContext()).uploadReceipt(receiptSnapshot.getValue(Receipt.class));
                    }
                }).start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query uploadedReceipts = Inbox.receiptsByStatus(getBaseContext(), "PENDING");
        uploadedReceipts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (DataSnapshot receiptSnapshot : dataSnapshot.getChildren())
                            new UploadReceiptTask(getBaseContext()).uploadReceipt(receiptSnapshot.getValue(Receipt.class));
                    }
                }).start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//
//        // Set interpreted receipt status to parsed
//        for (Receipt receipt : Receipt.find(Receipt.class, "status = 'POSTED'")) {
//            nextReceipt = receipt;
//            UserDatabase.getUserReference(User.getCurrentUser(),
//                    EnvironmentManager.currentEnvironment(this))
//                    .child(receipt.getFirebase_ref())
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//
//                                                        @Override
//                                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                                            Receipt onlineReceipt = dataSnapshot.getValue(Receipt.class);
//
//                                                            if (onlineReceipt == null) {
//                                                                nextReceipt.delete(this);
//                                                                return;
//                                                            }
//
//                                                            if (onlineReceipt.isInterpreted())
//                                                                nextReceipt.updateStatus(Receipt.Status.PARSED);
//                                                        }
//
//                                                        @Override
//                                                        public void onCancelled(DatabaseError databaseError) {
//                                                            // Do nothing
//                                                        }
//                                                    }
//
//                    );
//        }
    }
}
