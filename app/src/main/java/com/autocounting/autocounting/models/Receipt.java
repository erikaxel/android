package com.autocounting.autocounting.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.autocounting.autocounting.managers.EnvironmentManager;
import com.autocounting.autocounting.network.database.ReceiptDatabase;
import com.autocounting.autocounting.utils.ImageHandler;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.IgnoreExtraProperties;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

@IgnoreExtraProperties
public class Receipt extends SugarRecord {

    @Ignore
    public static final String TAG = "Receipt";

    @Ignore
    private String filename;

    @Ignore
    private File imageFile;

    private boolean isUploaded; // column name = is_uploaded

    private byte[] image; // column name = image

    // Firebase attributes
    @Ignore
    private String merchant_name;

    private String firebase_ref;  // column name = firebaseref
    @Ignore
    private long amount_cents;
    @Ignore
    private String interpreted_at;

    public Receipt() {
    }

    public Receipt(byte[] image, Context context) {
        this.image = image;
        this.isUploaded = false;
        DatabaseReference dbRef = ReceiptDatabase
                .newReceiptReference(User.getCurrentUser(),
                        EnvironmentManager.currentEnvironment(context));
        this.firebase_ref = dbRef.getKey();
        Log.d(TAG, "Set to ref " + this.firebase_ref);
    }

    public Receipt(File folder, String filename) {
        Log.i(TAG, "New receipt with filename" + filename);
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
        filename = generateFilename();
        return File.createTempFile(filename, ".jpg", folder);
    }

    public String getFilename() {
        return filename;
    }

    public File getImageFile() {
        return imageFile;
    }

    public Bitmap getThumbnail() {
        Bitmap bitmap = BitmapFactory.decodeByteArray(getImage(), 0, getImage().length);
        try {
            return ImageHandler.correctRotation((ImageHandler.makeThumbnail(bitmap)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
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

    public String getMerchant_name() {
        return merchant_name;
    }

    public void setMerchant_name(String merchant_name) {
        this.merchant_name = merchant_name;
    }

    public String getFirebase_ref() {
        return firebase_ref;
    }

    public void setFirebase_ref(String firebase_ref) {
        this.firebase_ref = firebase_ref;
    }

    public long getAmount_cents() {
        return amount_cents;
    }

    public void setAmount_cents(long amount_cents) {
        this.amount_cents = amount_cents;
    }

    public String getAmountString() {
        if (getAmount_cents() == 0)
            return "";
        else return new DecimalFormat("#.00").format((double) getAmount_cents() / 100);
    }

    public String getMerchantString() {
        if (getMerchant_name() == null)
            return "New receipt";
        else return getMerchant_name();
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public boolean getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(boolean isUploaded) {
        this.isUploaded = isUploaded;
    }

    public String getInterpreted_at() {
        return interpreted_at;
    }

    public void setInterpreted_at(String interpreted_at) {
        this.interpreted_at = interpreted_at;
    }

    public boolean isInterpreted(){
        return getInterpreted_at() != null;
    }
}
