package com.autocounting.autocounting.network.storage;

import com.autocounting.autocounting.models.User;
import com.autocounting.autocounting.network.Routes;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Handles connection to the Google / Firebase file storage
 */

public class ReceiptStorage {
    private static FirebaseStorage firebaseStorage;

    /**
     * @return a reference to the storage for the given user and application environment
     */
    public static StorageReference forUser(User user, String environment) {
        return getReference()
                .child(environment)
                .child("receipts")
                .child(user.getSavedUid());
    }

    private static StorageReference getReference() {
        if (firebaseStorage == null)
            setupFirebaseConnection();

        return firebaseStorage.getReferenceFromUrl(Routes.FIREBASE_STORAGE_URL);
    }

    private static void setupFirebaseConnection() {
        firebaseStorage = FirebaseStorage.getInstance();
    }
}
