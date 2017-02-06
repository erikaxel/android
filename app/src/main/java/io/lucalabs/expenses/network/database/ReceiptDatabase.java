package io.lucalabs.expenses.network.database;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.lucalabs.expenses.models.Receipt;

/**
 * Handles connection to Firebase database.
 */

public class ReceiptDatabase {

    private static FirebaseDatabase firebaseDatabase;

    /**
     * @return a database reference for the given user and application environment
     */
    public static DatabaseReference getUserReference(FirebaseUser user, String environment){
        return getReference()
                .child(environment)
                .child(user.getUid());
    }

    /**
     * Pushes a new receipt to the given user and application environment.
     * @return a database reference to the new receipt
     */
    public static DatabaseReference newReceiptReference(FirebaseUser user, String environment) {
        DatabaseReference ref = getUserReference(user, environment).child("receipts").push();
        ref.child("firebase_ref").setValue(ref.getKey());
        return ref;
    }

    /**
     * Pushes a new expense report to the given user and application environment.
     * @return a database reference to the new expense report
     */
    public static DatabaseReference newReportReference(FirebaseUser user, String environment) {
        return getUserReference(user, environment).child("expense_reports").push();
    }

    private static DatabaseReference getReference(){
        if(firebaseDatabase == null)
            setupFirebaseConnection();

        return firebaseDatabase.getReference();
    }

    private static void setupFirebaseConnection() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
    }
}
