package com.autocounting.autocounting.network.upload;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.autocounting.autocounting.models.User;
import com.autocounting.autocounting.utils.ImageHandler;
import com.firebase.client.annotations.NotNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.mimecraft.FormEncoding;

import java.io.File;
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

    private User user;
    private Bitmap originalImage;

    public UploadImageTask(UploadResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    protected String doInBackground(Bitmap... args) {

        originalImage = args[0];
        Bitmap thumbnail = ImageHandler.makeThumbnail(originalImage);
        user = User.getCurrentUser(responseHandler.getContext());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(FIREBASE_STORAGE_URL);

        // Potentially unsafe
        System.out.println("Change made");
        System.out.println("User.getCurrentUser()");
        System.out.println(User.getCurrentUser(responseHandler.getContext()).getSavedUid());
        storageRef.child(user.generateUserFileLocation("thumbnail", true))
                .putBytes(ImageHandler.makeByteArray(thumbnail));
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
                postReceipt();
                responseHandler.onFileUploadFinished(taskSnapshot.getDownloadUrl().toString());
            }
        });

        return "-1";
    }

    @Override
    protected void onPostExecute(String result) {
        responseHandler.onFileUploadFinished(result);
    }

    private void postReceipt() {
        OkHttpClient client = new OkHttpClient();
        System.out.println("So this fired");
        System.out.println(user.getLastGeneratedName());
        System.out.println(user.getToken());

        RequestBody form = new FormBody.Builder()
                .add("receipt[image_file_name]", user.getLastGeneratedName() + ".jpg")
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
