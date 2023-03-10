package io.lucalabs.expenses.network.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.models.User;
import io.lucalabs.expenses.network.NetworkStatus;
import io.lucalabs.expenses.network.Routes;
import io.lucalabs.expenses.network.database.UserDatabase;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadReceiptTask {

    private static final String TAG = UploadReceiptTask.class.getSimpleName();
//    private FirebaseLogger logger;

    private Context mContext;
    private FirebaseUser mUser;
    private Receipt mReceipt;

    public UploadReceiptTask(Context context) {
        mContext = context;
        mUser = User.getCurrentUser();
    }

    public void uploadReceipt(Receipt receipt) {
        Log.i(TAG, "Initialising " + receipt.getFirebase_ref());

        if (!NetworkStatus.appropriateNetworkIsAvailable(mContext)) {
            Log.w(TAG, "No appropriate network detected");
            return;
        }

        mReceipt = receipt;
        Inbox.findReceipt(mContext, mReceipt.getFirebase_ref()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mReceipt = dataSnapshot.getValue(Receipt.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        if (receipt.getInternal_status() == Receipt.Status.UPLOADED)
            postReceipt();
        else start();
    }

    private void start() {
        Log.i(TAG, "Starting ..." + mReceipt.getFirebase_ref());

        DatabaseReference dbRef = UserDatabase
                .getUserReference(mUser,
                        EnvironmentManager.currentEnvironment(mContext))
                .child(mReceipt.getFirebase_ref());

        mReceipt.updateInternalStatus(mContext, Receipt.Status.UPLOADING);

//        logger.startUploadingOriginal();

        UploadTask uploadOriginal = ReceiptStorage.getReceiptReference(
                mUser,
                EnvironmentManager.currentEnvironment(mContext),
                mReceipt.getFirebase_ref())
                .child("pages")
                .child("0.original.jpg")
                .putBytes(mReceipt.getImage(mContext));

        uploadOriginal.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "File upload failed");
                mReceipt.updateInternalStatus(mContext, Receipt.Status.PENDING);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    logger.onOriginalUploaded();
                mReceipt.updateInternalStatus(mContext, Receipt.Status.UPLOADED);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        postReceipt();
                    }
                }).start();
            }
        });
    }

//    private void startLogs() {
//        logger = new FirebaseLogger(responseHandler.getContext(),
//                user.getUid(),
//                receipt.getFilename());
//        logger.start();
//    }

    private void postReceipt() {
        mReceipt.updateInternalStatus(mContext, Receipt.Status.POSTING);
        OkHttpClient client = new OkHttpClient();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        RequestBody form = new FormBody.Builder()
                .add("receipt[firebase_ref]", mReceipt.getFirebase_ref())
                .add("page_one_file_name", "0.jpg")
                .add("token", User.getFirebaseToken(mContext))
                .add("use_ocr", prefs.getBoolean("disable_ocr_pref", false) ? "0" : "1")
                .add("expense_report[firebase_ref]", mReceipt.getExpense_report_firebase_key())
                .add("create_expense_report", "true") // Creates expense report if it doesn't exist
                .add("page_one_file_size", String.valueOf(mReceipt.getImage(mContext).length))
                .build();
        Log.i(TAG, "Posting receipt " + mReceipt.getFirebase_ref() + " to " + Routes.receiptsUrl(mContext, null));
        Request request = new Request.Builder()
                .url(Routes.receiptsUrl(mContext, null))
                .post(form)
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
            if (response.isSuccessful())
                mReceipt.updateInternalStatus(mContext, Receipt.Status.POSTED);
            else
                mReceipt.updateInternalStatus(mContext, Receipt.Status.UPLOADED);
        } catch (IOException e) {
            mReceipt.updateInternalStatus(mContext, Receipt.Status.UPLOADED);
            e.printStackTrace();
        }
        Log.i(TAG, "Finishing " + mReceipt.getFirebase_ref());
    }
}
