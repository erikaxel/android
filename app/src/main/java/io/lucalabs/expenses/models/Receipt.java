package io.lucalabs.expenses.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.IgnoreExtraProperties;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.models.annotations.Arg;
import io.lucalabs.expenses.network.database.UserDatabase;
import io.lucalabs.expenses.utils.DateFormatter;
import io.lucalabs.expenses.utils.ImageHandler;

@IgnoreExtraProperties
public class Receipt extends SugarRecord {

    @Ignore
    public static final String TAG = "ReceiptModel";

    public enum Status {
        PENDING(0), UPLOADING(1), UPLOADED(2), POSTING(3), POSTED(4), PARSED(5);

        private int status;

        private Status(int status) {
            this.status = status;
        }
    }

    // Fields that are persisted to SQLite database
    private Status status; // column name = status
    private String filename; // column name = filename
    @Arg(name="expense_report[firebase_key]")
    private String expense_report_firebase_key; // column name = expensereportfirebasekey
    private String firebase_ref;  // column name = firebaseref

    // Firebase attributes
    @Ignore @Arg(name="receipt[merchant_name]")
    private String merchant_name;
    @Ignore @Arg(name="receipt[amount]")
    private long amount_cents;
    @Ignore
    private String interpreted_at;
    @Ignore @Arg(name="receipt[used_date]")
    private String used_date;
    @Ignore @Arg(name="receipt[currency]")
    private String currency;
    @Ignore @Arg(name="receipt[reimbursable]")
    private boolean reimbursable;
    @Ignore @Arg(name="receipt[comment]")
    private String comment;

    public Receipt() {
    }

    public Receipt(byte[] image, Context context, String expenseReportRef) {
        this.setStatus(Status.PENDING);

        filename = generateFilename();

        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(image);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseReference dbRef = UserDatabase
                .newReceiptReference(User.getCurrentUser(),
                        EnvironmentManager.currentEnvironment(context), expenseReportRef);
        Log.d("ReceiptListAdapter", "Should instantiate");
        this.firebase_ref = dbRef.getKey();
        this.setExpense_report_firebase_key(context, expenseReportRef);
        this.save();
    }

    public String getStatusString(Context context) {
        if (status == null)
            return context.getString(R.string.name_not_found);

        switch (status) {
            case PENDING:
                return context.getString(R.string.waiting_to_upload);
            case UPLOADING:
                return context.getString(R.string.uploading);
            case UPLOADED:
                return context.getString(R.string.uploading);
            case POSTING:
                return context.getString(R.string.uploading);
            case POSTED:
                return context.getString(R.string.interpreting);
            default:
                return getMerchant_name();
        }
    }

    public void updateFromCache(Receipt cachedReceipt) {
        setStatus(cachedReceipt.getStatus());
    }

    public void setExpense_report_firebase_key(Context context, String expenseReportRef) {
        if (expenseReportRef == null)
            expenseReportRef = UserDatabase.newReportReference(User.getCurrentUser(),
                    EnvironmentManager.currentEnvironment(context)).getKey();
        setExpense_report_firebase_key(expenseReportRef);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    private static String generateFilename() {
        return String.valueOf(System.currentTimeMillis() / 10L) + ".jpg";
    }

    public File makeFile(File folder) throws IOException {
        filename = generateFilename();
        return File.createTempFile(filename, ".jpg", folder);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public File getImageFile(Context context) {
        return new File(context.getFilesDir().getAbsolutePath() + "/" + getFilename());
    }

    public Bitmap getThumbnail(Context context) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(getImage(context), 0, getImage(context).length);
        try {
            return ImageHandler.correctRotation((ImageHandler.makeThumbnail(bitmap)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
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

    public String getPrettyAmountString() {
        return getAmountString().replaceAll("([.]00)", ".-").replaceAll("([,]00)", ",-");
    }

    public String getMerchantString(Context context) {
        if (getMerchant_name() == null)
            return getStatusString(context);
        else {
            String merchantString = getMerchant_name();
            if (merchantString.length() > 24)
                return merchantString.substring(0, 20) + " ...";
            else return merchantString;
        }
    }

    public byte[] getImage(Context context) {
        File file = getImageFile(context);

        if (file == null)
            return null;

        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
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

    public String getUsedDateString(Context context) {
        return DateFormatter.formatToLocale(context, used_date);
    }

    public void setUsed_date(String used_date) {
        this.used_date = used_date;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void updateStatus(Status status) {
        setStatus(status);
        save();
        Log.i("ReceiptStatus", firebase_ref + " is now " + status);
    }

    public String getFirebase_ref() {
        return firebase_ref;
    }

    public void setFirebase_ref(String firebase_ref) {
        this.firebase_ref = firebase_ref;
    }

    public boolean isReimbursable() {
        return reimbursable;
    }

    public void setReimbursable(boolean reimbursable) {
        this.reimbursable = reimbursable;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExpense_report_firebase_key() {
        return expense_report_firebase_key;
    }

    public void setExpense_report_firebase_key(String expense_report_firebase_key) {
        this.expense_report_firebase_key = expense_report_firebase_key;
    }


    public boolean delete(Context context) {
        return Receipt.delete(this) &&
                new File(context.getFilesDir().getAbsolutePath() + "/" + getFilename()).delete();
    }

    public boolean equals(Receipt other) {
        return false;
    }
}
