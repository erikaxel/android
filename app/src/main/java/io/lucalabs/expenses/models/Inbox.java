package io.lucalabs.expenses.models;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;
import android.util.Log;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.network.database.UserDatabase;
import io.lucalabs.expenses.network.storage.ReceiptStorage;

/*
 * Serves as an abstraction of the user's Expense Item inbox.
 * Handles queries.
 */
public class Inbox {

    public static Query allExpenseReports(Context context) {
        return queryDb(context).child("expense_reports");
    }

    public static Query receiptsForExpenseReport(Context context, String key) {
        return queryDb(context)
                .child("receipts")
                .orderByChild("expense_report_firebase_key")
                .equalTo(key);
    }

    public static ExpenseReport createExpenseReport(Context context){
        DatabaseReference ref = UserDatabase.newReportReference(
                User.getCurrentUser(),
                EnvironmentManager.currentEnvironment(context));
        ExpenseReport expenseReport = new ExpenseReport();
        expenseReport.setFirebase_ref(ref.getKey());
        ref.setValue(expenseReport);
        return expenseReport;
    }

    public static DatabaseReference findExpenseReport(Context context, String firebaseRef) {
        return queryDb(context).child("expense_reports").child(firebaseRef);
    }

    public static DatabaseReference findReceipt(Context context, String firebaseRef){
        return queryDb(context).child("receipts").child(firebaseRef);
    }

    /**
     * Fetches a storage reference to a receipt image.
     * @param type is the image size/type to be loaded (e.g. "original", "medium", "thumbnail")
     */
    public static StorageReference receiptImage(Context context, Receipt receipt, String type) {
        Log.d("ReceiptListAdapter", "ref is " + queryStorage(context).getPath() + "/" + receipt.getFirebase_ref() + "/pages/0.thumbnail.jpg");
        return  queryStorage(context)
                .child(receipt.getFirebase_ref())
                .child("pages")
                .child("0." + type + ".jpg");
    }

    private static DatabaseReference queryDb(Context context) {
        DatabaseReference dbRef = UserDatabase.getUserReference(
                User.getCurrentUser(),
                EnvironmentManager.currentEnvironment(context));
        dbRef.keepSynced(true);
        return dbRef;
    }

    private static StorageReference queryStorage(Context context){
        return ReceiptStorage.getUserReference(
                User.getCurrentUser(),
                EnvironmentManager.currentEnvironment(context));
    }

}

