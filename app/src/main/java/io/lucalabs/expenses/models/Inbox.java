package io.lucalabs.expenses.models;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.network.database.ReceiptDatabase;
import io.lucalabs.expenses.network.storage.ReceiptStorage;

/*
 * Serves as an abstraction of the user's Receipt inbox.
 * Handles queries.
 */
public class Inbox {

    public static Query allReceipts(Context context){
        return ReceiptDatabase.getUserReference(
                User.getCurrentUser(),
                EnvironmentManager.currentEnvironment(context))
                .orderByChild("expense_report_id")
                .equalTo(null);
    }

    public static StorageReference receiptThumbnail(Context context, Receipt receipt){
        return ReceiptStorage
                .getUserReference(User.getCurrentUser(),
                        EnvironmentManager.currentEnvironment(context))
                .child(receipt.getFirebase_ref())
                .child("pages")
                .child("0.thumbnail.jpg");
    }
}
