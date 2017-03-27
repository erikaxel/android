package io.lucalabs.expenses.network.webapi;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.models.Task;
import io.lucalabs.expenses.network.storage.UploadReceiptTask;

public class TaskManagerService extends Service {
    private final static String TAG = TaskManagerService.class.getSimpleName();
    private ServiceHandler serviceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (TaskManagerService.this) {
                DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
                connectedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean connected = snapshot.getValue(Boolean.class);
                        if (connected) {
                            handleTasks();
                            handleReceipts();
                        } else {
                            Log.i(TAG, "Not connected to Firebase");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.err.println("Listener was cancelled");
                    }
                });
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
        Query uploadedReceipts = Inbox.receiptsByStatus(getBaseContext(), "UPLOADED");
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

        Query pendingReceipts = Inbox.receiptsByStatus(getBaseContext(), "PENDING");
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
    }
}
