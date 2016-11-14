package com.autocounting.autocounting.models;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class Receipt {

    public static final String TAG = "Receipt";
    private String filename;
    private File imageFile;

    public Receipt() {
        this.filename = generateFilename();
        Log.i(TAG, "New empty receipt: " + filename);
    }

    public Receipt(File folder, String filename) {
        this.filename = filename;
        this.imageFile = new File(folder, filename);
    }

    public void deleteFromQueue() {
        Log.i(TAG, "Deleting receipt " + filename + " from queue");
        imageFile.delete();
    }

    private static String generateFilename() {
        return String.valueOf(System.currentTimeMillis() / 10L);
    }

    public File makeFile(File folder) throws IOException {
        return File.createTempFile(filename, ".jpg", folder);
    }

    public String getFilename() {
        return filename;
    }

    public File getImageFile() {
        return imageFile;
    }

    public static File getReceiptFolder() {
        File imageFolder = new File(Environment.getExternalStorageDirectory(), "receipt_queue");

        if (!imageFolder.exists())
            imageFolder.mkdirs();

        return imageFolder;
    }

    public static void deleteReceiptFolder() {
        File receiptFolder = getReceiptFolder();
        for (File file : receiptFolder.listFiles())
            file.delete();
        receiptFolder.delete();
    }
}
