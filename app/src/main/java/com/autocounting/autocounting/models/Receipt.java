package com.autocounting.autocounting.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.autocounting.autocounting.utils.ImageHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Receipt {

    public static final String TAG = "Receipt";
    private String filename;
    private File imageFile;
    private Context context;

    public Receipt() {
        this.filename = generateFilename();
        Log.i(TAG, "New empty receipt: " + filename);
    }

    public Receipt(File folder, String filename) {
        new Receipt(folder, filename, null);
    }

    public Receipt(File folder, String filename, Context context){
        this.filename = filename;
        this.imageFile = new File(folder, filename);
        this.context = context;
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

    public Bitmap getThumbnail(Context context){
        Bitmap bitmap = ImageHandler.getBitmapFromFile(context, imageFile);
        try {
            return ImageHandler.correctRotation((ImageHandler.makeThumbnail(bitmap)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public Bitmap getThumbnail(){
        return getThumbnail(context);
    }

    public static List<Receipt> getAll(Context context){
        File imageFolder = getReceiptFolder();
        ArrayList<Receipt> receipts = new ArrayList<>();

        for(String imageAdress : imageFolder.list())
            receipts.add(0, new Receipt(imageFolder, imageAdress, context));

        return receipts;
    }

    public static File getReceiptFolder() {
        File imageFolder = new File(Environment.getExternalStorageDirectory(), "receipt_queue");

        if (!imageFolder.exists())
            imageFolder.mkdirs();

        return imageFolder;
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
