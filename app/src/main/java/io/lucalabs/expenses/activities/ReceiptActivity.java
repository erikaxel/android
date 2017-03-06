package io.lucalabs.expenses.activities;

import android.os.Bundle;

import com.google.firebase.storage.StorageReference;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.firebase.FirebaseActivity;
import io.lucalabs.expenses.views.fragments.PhotoPreviewFragment;
import io.lucalabs.expenses.views.fragments.ReceiptFormFragment;

public class ReceiptActivity extends FirebaseActivity {
    private ReceiptFormFragment mFormFragment;
    private boolean mShowingPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayDeleteIcon();
        setTitle(R.id.receipt_activity_title);
        setContentView(R.layout.activity_receipt);

        mFormFragment = ReceiptFormFragment.newInstance();

        if (savedInstanceState == null)
            showForm();
    }

    protected void onResume() {
        super.onResume();
    }

    private void showForm() {
        mShowingPreview = false;
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mFormFragment)
                .commit();
    }

    public void showPreview(byte[] previewImage, StorageReference storageRef) {
        mShowingPreview = true;
        PhotoPreviewFragment photoPreviewFragment = PhotoPreviewFragment.newInstance();
        photoPreviewFragment.setPreviewImage(previewImage);
        photoPreviewFragment.setStorageRef(storageRef);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, photoPreviewFragment)
                .commit();
    }

    @Override
    protected void onDeleteAction() {
        mFormFragment.onDeleteAction();
    }

    @Override
    public void onBackPressed() {
        if (mShowingPreview)
            showForm();
        else super.onBackPressed();
    }
}
