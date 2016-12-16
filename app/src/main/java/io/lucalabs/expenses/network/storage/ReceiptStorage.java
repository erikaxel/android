package io.lucalabs.expenses.network.storage;

import io.lucalabs.expenses.network.Routes;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Handles connection to the Google / Firebase file storage
 */

public class ReceiptStorage {
    private static FirebaseStorage firebaseStorage;

    /**
     * @return a storage reference for the given user and application environment
     */
    public static StorageReference getUserReference(FirebaseUser user, String environment) {
        return getReference()
                .child(environment)
                .child("receipts")
                .child(user.getUid());
    }

    /**
     * @return a storage reference for the given user, environment, receipt
     */
    public static StorageReference getReceiptReference(FirebaseUser user, String environment, String receipt) {
        return getUserReference(user, environment).child(receipt);
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
