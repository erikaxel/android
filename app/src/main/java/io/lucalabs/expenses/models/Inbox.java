package io.lucalabs.expenses.models;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;

import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.network.database.ReceiptDatabase;
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

    public static DatabaseReference findExpenseReport(Context context, String firebaseRef) {
        return queryDb(context).child("expense_reports").child(firebaseRef);
    }

    public static StorageReference receiptThumbnail(Context context, Receipt receipt) {
        return  queryStorage(context)
                .child(receipt.getFirebase_ref())
                .child("pages")
                .child("0.thumbnail.jpg");
    }

    private static DatabaseReference queryDb(Context context) {
        return ReceiptDatabase.getUserReference(
                User.getCurrentUser(),
                EnvironmentManager.currentEnvironment(context));
    }

    private static StorageReference queryStorage(Context context){
        return ReceiptStorage.getUserReference(
                User.getCurrentUser(),
                EnvironmentManager.currentEnvironment(context));
    }

}

