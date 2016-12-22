package io.lucalabs.expenses.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.IgnoreExtraProperties;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.network.database.ReceiptDatabase;
import io.lucalabs.expenses.utils.DateFormatter;
import io.lucalabs.expenses.utils.ImageHandler;

@DatabaseTable(tableName = "receipts")
public class Receipt {

    @Ignore
    public static final String TAG = "ReceiptModel";

    @Ignore
    private String filename;

    @Ignore
    private File imageFile;

    public enum Status {
        PENDING(0), UPLOADING(1), UPLOADED(2), POSTING(3), POSTED(4), PARSED(5);

        private int status;

        private Status(int status) {
            this.status = status;
        }
    }

    // Fields that are persisted to SQLite database
    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField
    private Status status; // column name = status

    @DatabaseField
    private byte[] image; // column name = image

    @DatabaseField
    private String firebase_ref;  // column name = firebaseref

    // Firebase attributes
    @Ignore
    private String merchant_name;
    @Ignore
    private long amount_cents;
    @Ignore
    private String interpreted_at;
    @Ignore
    private String used_date;
    @Ignore
    private String updated_at;

    public Receipt() {
    }

    public Receipt(byte[] image, Context context) {
        this.image = image;
        this.setStatus(Status.PENDING);
        DatabaseReference dbRef = ReceiptDatabase
                .newReceiptReference(User.getCurrentUser(),
                        EnvironmentManager.currentEnvironment(context));
        this.setFirebase_ref(dbRef.getKey());
        this.save();
        List<Receipt> recs = Receipt.find(Receipt.class, "firebaseref = ? ", firebase_ref);

        if (recs.size() == 0) {
            Log.e("ReceiptStatus", "Receipt not saved: " + firebase_ref);
        } else {
            Receipt rec = recs.get(0);
            Log.i("ReceiptStatus", rec.getFirebase_ref() + " extra is now " + rec.getStatus());
        }
//
        Log.i("ReceiptStatus", getFirebase_ref() + " is now " + getStatus());
    }

    public Receipt(File folder, String filename) {
        Log.i(TAG, "New receipt with filename" + filename);
        this.filename = filename;
        this.imageFile = new File(folder, filename);
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

    public String getMerchant_name() {
        return merchant_name;
    }

    public void setMerchant_name(String merchant_name) {
        this.merchant_name = merchant_name;
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
            return "Hang on ...";
        else return getMerchant_name();
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getInterpreted_at() {
        return interpreted_at;
    }

    public void setInterpreted_at(String interpreted_at) {
        this.interpreted_at = interpreted_at;
    }

    public boolean isInterpreted() {
        return getInterpreted_at() != null;
    }

    public String getUsed_date() {
        return used_date;
    }

    public void setUsed_date(String used_date) {
        this.used_date = used_date;
    }

    public String getDateString(Context context) {
        return DateFormatter.formatToLocale(getUsed_date(), context);
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void updateStatus(Status status) {
        setStatus(status);
        update();
        Log.i("ReceiptStatus", firebase_ref + " is now " + status);
    }

    public void setFirebase_ref(String firebase_ref) {
        this.firebase_ref = firebase_ref;
    }

    public String getFirebase_ref() {
        return firebase_ref;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}
