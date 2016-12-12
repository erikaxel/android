package com.autocounting.autocounting.network.database;

import com.autocounting.autocounting.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Handles connection to Firebase database.
 */

public class ReceiptDatabase {

    private static FirebaseDatabase firebaseDatabase;

    /**
     * @return a reference to the database for the given user and application environment
     */
    public static DatabaseReference forUser(User user, String environment){
        return getReference()
                .child(environment)
                .child(user.getSavedUid())
                .child("receipts");
    }

    private static DatabaseReference getReference(){
        if(firebaseDatabase == null)
            setupFirebaseConnection();

        return firebaseDatabase.getReference();
    }

    private static void setupFirebaseConnection(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
    }
}
