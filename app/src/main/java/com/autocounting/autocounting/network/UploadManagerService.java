package com.autocounting.autocounting.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Network;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.autocounting.autocounting.network.upload.UploadImageTask;
import com.autocounting.autocounting.network.upload.UploadResponseHandler;
import com.autocounting.autocounting.utils.ImageHandler;

import java.io.File;

public class UploadManagerService extends IntentService implements UploadResponseHandler {

    private static boolean isRunning;
    private final static String TAG = "UploadManagerService";

    public UploadManagerService(String name) {
        super(name);
    }

    public UploadManagerService() {
        super("UploadManagerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Running");

        if (isRunning || !NetworkManager.networkIsAvailable(this))
            return;

        Log.i(TAG, "Running upload queue");

        isRunning = true;
        uploadQueue();
        isRunning = false;
    }

    private void uploadQueue() {
        File imageFolder = getImageFolder();

        if (imageFolder.list() != null) {
            Log.i(TAG, imageFolder.list().length + " images in " + imageFolder.getAbsolutePath());
            for (String imageAdress : imageFolder.list()) {

                Bitmap bitmap = ImageHandler.getBitmapFromUri(
                        getApplicationContext(),
                        Uri.fromFile(new File(imageFolder, imageAdress)));

                if (bitmap != null) {
                    Log.i(TAG, "Uploading bitmap " + bitmap.toString());
                    new UploadImageTask(this).execute(bitmap);
                } else {
                    Log.i(TAG, "Image not found");
                }

                Log.i(TAG, "Deleting " + imageAdress + " from queue");
                new File(imageFolder, imageAdress).delete();
            }
        } else {
            Log.i(TAG, imageFolder.getAbsolutePath() + " is empty");
        }
    }

    private File getImageFolder() {
        File imageFolder = new File(Environment.getExternalStorageDirectory(), "receipt_queue");

        if (!imageFolder.exists())
            imageFolder.mkdirs();

        return imageFolder;
    }

    // UploadResponseHandler

    @Override
    public void onFileUploadStarted(String filenme) {

    }

    @Override
    public void onFileUploadFinished(String result) {

    }

    @Override
    public void onFileUploadFailed() {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
