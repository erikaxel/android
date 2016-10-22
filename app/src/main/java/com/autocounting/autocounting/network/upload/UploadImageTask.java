package com.autocounting.autocounting.network.upload;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.autocounting.autocounting.models.Receipt;
import com.autocounting.autocounting.models.User;
import com.autocounting.autocounting.network.RouteManager;
import com.autocounting.autocounting.network.logging.FirebaseLogger;
import com.autocounting.autocounting.utils.ImageHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadImageTask extends AsyncTask<Bitmap, String, String> {

    private static final String TAG = "UploadImageTask";
    private UploadResponseHandler responseHandler;
    private FirebaseLogger logger;

    private User user;
    private Receipt receipt;
    private Bitmap originalImage;

    private RouteManager routeManager;

    public UploadImageTask(UploadResponseHandler responseHandler) {
        Log.i(TAG, "Running upload task " + this.toString());
        this.responseHandler = responseHandler;
        user = User.getCurrentUser(responseHandler.getContext());
    }

    @Override
    protected String doInBackground(Bitmap... args) {
        this.receipt = new Receipt();
        startLogs();

        Log.i(TAG, "Initialising receipt " + receipt.getFilename());
        responseHandler.onFileUploadStarted(receipt.getFilename());
        originalImage = ImageHandler.scaleOriginal(args[0]);
        routeManager = new RouteManager(responseHandler.getContext());

        Bitmap mediumImage = ImageHandler.makeMedium(originalImage);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(RouteManager.FIREBASE_STORAGE_URL);

        logger.startUploadingOriginal();
        UploadTask uploadOriginal = storageRef.
                child(user.generateUserFileLocation("original", routeManager.storageUrl(), receipt.getFilename()))
                .putBytes(ImageHandler.makeByteArray(originalImage));
        uploadOriginal.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                responseHandler.onFileUploadFailed();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                logger.onOriginalUploaded();
                postReceipt();;
                responseHandler.onFileUploadFinished(taskSnapshot.getDownloadUrl().toString());
            }
        });

        return "processed";
    }

    private void startLogs() {
        logger = new FirebaseLogger(responseHandler.getContext(),
                user.getUid(),
                receipt.getFilename());
        logger.start();
    }

    @Override
    protected void onPostExecute(String result) {
        responseHandler.onFileUploadFinished(result);
    }

    private void postReceipt() {
        OkHttpClient client = new OkHttpClient();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(responseHandler.getContext());

        Log.i(TAG, "Posting " + receipt.getFilename());
        RequestBody form = new FormBody.Builder()
                .add("receipt[image_file_name]", receipt.getFilename() + ".jpg")
                .add("receipt[image_content_type]", "image/jpeg")
                .add("receipt[image_file_size]", String.valueOf(originalImage.getByteCount()))
                .add("resize_images", "1")
                .add("user_ocr", prefs.getBoolean("disable_ocr_pref", false)? "0" : "1")
                .add("token", user.getToken())
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
