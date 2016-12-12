package com.autocounting.autocounting.network.upload;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.autocounting.autocounting.models.Receipt;
import com.autocounting.autocounting.models.User;
import com.autocounting.autocounting.network.RouteManager;
import com.autocounting.autocounting.network.logging.FirebaseLogger;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadReceiptTask {

    private static final String TAG = "UploadReceiptTask";
    private UploadResponseHandler responseHandler;
    private FirebaseLogger logger;

    private User user;
    private Receipt receipt;
    private DatabaseReference dbReference;

    private RouteManager routeManager;

    public UploadReceiptTask(UploadResponseHandler responseHandler) {
        Log.i(TAG, "Running upload task " + this.toString());
        this.responseHandler = responseHandler;
        user = User.getCurrentUser(responseHandler.getContext());
    }

    public void uploadReceipt(Receipt receipt) {
        this.receipt = receipt;
        startLogs();
        start();
    }

    private void start() {
        Log.i(TAG, "Initialising receipt " + receipt.getFilename());
        responseHandler.onFileUploadStarted(receipt.getFilename());
        routeManager = new RouteManager(responseHandler.getContext());

        dbReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(routeManager.getEnvironment())
                .child(user.getSavedUid())
                .child("receipts")
                .push();

        new ReceiptEvent(responseHandler.getContext(), dbReference.getKey()).receiptAdded();

        Log.i(TAG, "Saving reference with key " + dbReference.getKey());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(RouteManager.FIREBASE_STORAGE_URL);

        logger.startUploadingOriginal();
        UploadTask uploadOriginal = null;

        try {
            FileInputStream receiptFos = new FileInputStream(receipt.getImageFile());

            uploadOriginal = storageRef.
                    child(user.generateUserFileLocation("original", routeManager.storageUrl(), receipt.getFilename()))
                    .putBytes(IOUtils.toByteArray(receiptFos));

            uploadOriginal.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    responseHandler.onFileUploadFailed();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    logger.onOriginalUploaded();
                    postReceipt();
                    receipt.deleteFromQueue();
                    responseHandler.onFileUploadFinished(taskSnapshot.getDownloadUrl().toString());
                }
            });

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void startLogs() {
        logger = new FirebaseLogger(responseHandler.getContext(),
                user.getUid(),
                receipt.getFilename());
        logger.start();
    }

    private void postReceipt() {
        OkHttpClient client = new OkHttpClient();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(responseHandler.getContext());

        RequestBody form = new FormBody.Builder()
                .add("receipt[firebase_ref]", dbReference.getKey())
                .add("page_one_file_name", "0.jpg")
                .add("token", user.getToken())
                .add("use_ocr", prefs.getBoolean("disable_ocr_pref", false) ? "0" : "1")
                .add("page_one_file_size", String.valueOf(receipt.getImageFile().length()))
                // .add date & time
                .build();

        Request request = new Request.Builder()
                .url(routeManager.receiptsUrl())
                .post(form)
                .build();

        try {
            Response response = client.newCall(request).execute();
            logger.onReceiptUploaded();
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
