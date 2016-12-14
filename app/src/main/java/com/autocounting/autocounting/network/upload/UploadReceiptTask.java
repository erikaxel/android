package com.autocounting.autocounting.network.upload;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.autocounting.autocounting.managers.EnvironmentManager;
import com.autocounting.autocounting.models.Receipt;
import com.autocounting.autocounting.models.User;
import com.autocounting.autocounting.network.NetworkStatus;
import com.autocounting.autocounting.network.Routes;
import com.autocounting.autocounting.network.database.ReceiptDatabase;
import com.autocounting.autocounting.network.storage.ReceiptStorage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//import com.autocounting.autocounting.network.logging.FirebaseLogger;

public class UploadReceiptTask {

    private static final String TAG = "UploadReceiptTask";
    private UploadResponseHandler responseHandler;
//    private FirebaseLogger logger;

    private FirebaseUser user;
    private Receipt receipt;

    public UploadReceiptTask(UploadResponseHandler responseHandler) {
        Log.i(TAG, "Running upload task " + this.toString());
        this.responseHandler = responseHandler;
        user = User.getCurrentUser();
    }

    public void uploadReceipt(Receipt receipt) {
        this.receipt = receipt;
//        startLogs();
        start();
    }

    private void start() {
        Log.d(TAG, "Here's all we know about our receipt");
        Log.d(TAG, "It has a name: " + receipt.getFirebase_ref());
        Log.d(TAG, "It has this many bytes: " + receipt.getImage().length);
        responseHandler.onFileUploadStarted(receipt.getFilename());

        DatabaseReference dbRef = ReceiptDatabase
                .getUserReference(user,
                        EnvironmentManager.currentEnvironment(responseHandler.getContext()))
                .child(receipt.getFirebase_ref());
        dbRef.keepSynced(true);

        if (!NetworkStatus.networkIsAvailable(responseHandler.getContext())){
            Log.w(TAG, "No network detected");
            return;
        }

//        logger.startUploadingOriginal();

        UploadTask uploadOriginal = ReceiptStorage.getReceiptReference(
                user,
                EnvironmentManager.currentEnvironment(responseHandler.getContext()),
                receipt.getFirebase_ref())
                .child("pages")
                .child("0.original.jpg")
                .putBytes(receipt.getImage());

        Log.d(TAG, "Uploading to " + ReceiptStorage.getReceiptReference(
                user,
                EnvironmentManager.currentEnvironment(responseHandler.getContext()),
                receipt.getFirebase_ref())
                .child("pages")
                .child("0.original.jpg").getPath());

        uploadOriginal.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                responseHandler.onFileUploadFailed();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    logger.onOriginalUploaded();
                postReceipt();
                Receipt.delete(receipt);
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
        OkHttpClient client = new OkHttpClient();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(responseHandler.getContext());

        RequestBody form = new FormBody.Builder()
                .add("receipt[firebase_ref]", receipt.getFirebase_ref())
                .add("page_one_file_name", "0.jpg")
                .add("token", User.getToken(responseHandler.getContext()))
                .add("use_ocr", prefs.getBoolean("disable_ocr_pref", false) ? "0" : "1")
                .add("page_one_file_size", String.valueOf(receipt.getImage().length))
                .build();

        Request request = new Request.Builder()
                .url(Routes.receiptsUrl(responseHandler.getContext()))
                .post(form)
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.i(TAG, "Current token: " + User.getToken(responseHandler.getContext()));
            Log.i(TAG, response.body().string());
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
