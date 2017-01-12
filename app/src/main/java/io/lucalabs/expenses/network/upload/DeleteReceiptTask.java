package io.lucalabs.expenses.network.upload;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import android.util.Log;

import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Receipt;

public class DeleteReceiptTask {

    private static final String TAG = "DeleteReceiptTask";
    private Context mContext;
    private Receipt mReceipt;

    public DeleteReceiptTask(Context context, Receipt receipt) {
        mContext = context;
        mReceipt = receipt;
    }

    /*
     * Checks if thumbnail is uploaded to storage.
     * If so, deletes receipt.
     */
    public void deleteReceipt() {
        Inbox.receiptThumbnail(mContext, mReceipt).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "File was fetched. Cached receipt deleted");
                    mReceipt.delete(mContext);
                } else {
                    Log.i(TAG, "File could not be fetched");
                }
            }
        });
    }
}
