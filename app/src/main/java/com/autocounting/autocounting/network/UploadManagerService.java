package com.autocounting.autocounting.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.autocounting.autocounting.models.Receipt;
import com.autocounting.autocounting.network.upload.ReceiptEvent;
import com.autocounting.autocounting.network.upload.UploadReceiptTask;
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

        if (isRunning || !NetworkManager.readyToUpload(this)) {
            Log.i(TAG, "Not runnign upload queue");
            return;
        }

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
                    Log.i(TAG, "Uploading receipt with bitmap: " + bitmap.toString());
                    new UploadReceiptTask(this).execute(new Receipt(imageAdress, bitmap));
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
    public void onFileUploadStarted(String filename) {
        new ReceiptEvent(this, filename).receiptAdded();
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
