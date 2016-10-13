package com.autocounting.autocounting.network.upload;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.autocounting.autocounting.models.User;
import com.autocounting.autocounting.network.RouteManager;
import com.autocounting.autocounting.network.logging.FirebaseLogger;
import com.autocounting.autocounting.utils.ImageHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadImageTask extends AsyncTask<Bitmap, String, String> {


    private UploadResponseHandler responseHandler;
    private FirebaseLogger logger;

    private User user;
    private Bitmap originalImage;
    private int numberOfFilesReady = 0;

    private RouteManager routeManager;

    public UploadImageTask(UploadResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        user = User.getCurrentUser(responseHandler.getContext());
        logger = new FirebaseLogger(responseHandler.getContext(),
                user.getUid(),
                user.getTempName());
        logger.start();
    }

    @Override
    protected String doInBackground(Bitmap... args) {
        responseHandler.onFileUploadStarted(user.getTempName());
        originalImage = ImageHandler.scaleOriginal(args[0]);
        routeManager = new RouteManager(responseHandler.getContext());

        Bitmap mediumImage = ImageHandler.makeMedium(originalImage);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(RouteManager.FIREBASE_STORAGE_URL);

        logger.startUploadingOriginal();
        UploadTask uploadOriginal = storageRef.
                child(user.generateUserFileLocation("original", routeManager.storageUrl()))
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

    @Override
    protected void onPostExecute(String result) {
        responseHandler.onFileUploadFinished(result);
    }

    private void postReceipt() {
        System.out.println("Filename: " + user.getTempName());
        OkHttpClient client = new OkHttpClient();

        RequestBody form = new FormBody.Builder()
                .add("receipt[image_file_name]", user.getTempName() + ".jpg")
                .add("receipt[image_content_type]", "image/jpeg")
                .add("receipt[image_file_size]", String.valueOf(originalImage.getByteCount()))
                .add("resize_images", "1")
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
