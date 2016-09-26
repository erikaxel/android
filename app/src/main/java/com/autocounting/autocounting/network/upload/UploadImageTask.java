package com.autocounting.autocounting.network.upload;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.autocounting.autocounting.models.User;
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

    private static final String FIREBASE_STORAGE_URL = "gs://autocounting.appspot.com";
    private static final String POST_RECEIPT_URL = "https://beta.autocounting.no/receipts?";
    private UploadResponseHandler responseHandler;
    private FirebaseLogger logger;

    private User user;
    private Bitmap originalImage;
    private int numberOfFilesReady = 0;

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
        Bitmap mediumImage = ImageHandler.makeMedium(originalImage);
        Bitmap thumbnail = ImageHandler.makeThumbnail(originalImage);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(FIREBASE_STORAGE_URL);

        logger.startUploadingThumb();
        UploadTask uploadThumbnail = storageRef.child(user.generateUserFileLocation("thumbnail", true))
                .putBytes(ImageHandler.makeByteArray(thumbnail));
        uploadThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                logger.onThumbUploaded();
                if(++numberOfFilesReady == 3)
                    postReceipt();
            }
        });

        UploadTask uploadMedium = storageRef.
                child(user.generateUserFileLocation("medium", false))
                .putBytes(ImageHandler.makeByteArray(originalImage));
        uploadMedium.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                responseHandler.onFileUploadFailed();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                logger.onOriginalUploaded();
                if(++numberOfFilesReady == 3)
                    postReceipt();;
                responseHandler.onFileUploadFinished(taskSnapshot.getDownloadUrl().toString());
            }
        });

        logger.startUploadingOriginal();
        UploadTask uploadOriginal = storageRef.
                child(user.generateUserFileLocation("original", false))
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
                if(++numberOfFilesReady == 3)
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
                .add("token", user.getToken())
                .build();

        Request request = new Request.Builder()
                .url(POST_RECEIPT_URL)
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
