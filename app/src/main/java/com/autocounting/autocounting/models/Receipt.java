package com.autocounting.autocounting.models;

import android.graphics.Bitmap;
import android.util.Log;

import com.autocounting.autocounting.utils.ImageHandler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Receipt {

    public static final String TAG = "Receipt";
    private String filename;
    private Bitmap image;

    public Receipt(){
        this.filename = generateFilename();
        Log.i(TAG, "New empty receipt: " + filename);
    }

    public Receipt(String filename, Bitmap image) {
        this.filename = filename;
        this.image = ImageHandler.scaleOriginal(image);
        Log.i(TAG, "New receipt from file/image: " + filename);
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

    public Bitmap getImage() {
        return image;
    }
}
