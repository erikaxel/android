package io.lucalabs.expenses.network.upload;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.models.User;
import io.lucalabs.expenses.network.NetworkStatus;
import io.lucalabs.expenses.network.Routes;
import io.lucalabs.expenses.network.database.ReceiptDatabase;
import io.lucalabs.expenses.network.storage.ReceiptStorage;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadReceiptTask {

    private static final String TAG = "UploadReceiptTask";
    private UploadResponseHandler responseHandler;
//    private FirebaseLogger logger;

    private FirebaseUser user;
    private Receipt receipt;

    public UploadReceiptTask(UploadResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        user = User.getCurrentUser();
    }

    public void uploadReceipt(Receipt receipt) {
        Log.i(TAG, "Initialising " + receipt.getFirebase_ref());
        this.receipt = receipt;
        if (receipt.getStatus() == Receipt.Status.UPLOADED)
            postReceipt();
        else start();
    }

    private void start() {
        Log.i(TAG, "Starting ..." + receipt.getFirebase_ref());
        responseHandler.onFileUploadStarted(receipt.getFilename());

        DatabaseReference dbRef = ReceiptDatabase
                .getUserReference(user,
                        EnvironmentManager.currentEnvironment(responseHandler.getContext()))
                .child(receipt.getFirebase_ref());

        if (!NetworkStatus.appropriateNetworkIsAvailable(responseHandler.getContext())) {
            Log.w(TAG, "No appropriate network detected");
            return;
        }

        receipt.updateStatus(Receipt.Status.UPLOADING);

//        logger.startUploadingOriginal();

        UploadTask uploadOriginal = ReceiptStorage.getReceiptReference(
                user,
                EnvironmentManager.currentEnvironment(responseHandler.getContext()),
                receipt.getFirebase_ref())
                .child("pages")
                .child("0.original.jpg")
                .putBytes(receipt.getImage(responseHandler.getContext()));

        uploadOriginal.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "File upload failed");
                receipt.updateStatus(Receipt.Status.PENDING);
                responseHandler.onFileUploadFailed();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    logger.onOriginalUploaded();
                receipt.updateStatus(Receipt.Status.UPLOADED);
                postReceipt();
                responseHandler.onFileUploadFinished(taskSnapshot.getDownloadUrl().toString());
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
        Log.i(TAG, "Posting " + receipt.getFirebase_ref());
        receipt.updateStatus(Receipt.Status.POSTING);
        OkHttpClient client = new OkHttpClient();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(responseHandler.getContext());

        RequestBody form = new FormBody.Builder()
                .add("receipt[firebase_ref]", receipt.getFirebase_ref())
                .add("page_one_file_name", "0.jpg")
                .add("token", User.getToken(responseHandler.getContext()))
                .add("use_ocr", prefs.getBoolean("disable_ocr_pref", false) ? "0" : "1")
                .add("page_one_file_size", String.valueOf(receipt.getImage(responseHandler.getContext()).length))
                .build();

        Log.i(TAG, "Posting receipt " + receipt.getFirebase_ref() + " to " + Routes.receiptsUrl(responseHandler.getContext()));
        Request request = new Request.Builder()
                .url(Routes.receiptsUrl(responseHandler.getContext()))
                .post(form)
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
            if (response.isSuccessful())
                receipt.updateStatus(Receipt.Status.POSTED);
            else
                receipt.updateStatus(Receipt.Status.UPLOADED);
        } catch (IOException e) {
            receipt.updateStatus(Receipt.Status.UPLOADED);
            e.printStackTrace();
        }
        Log.i(TAG, "Finishing " + receipt.getFirebase_ref());

    }
}
