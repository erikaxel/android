package io.lucalabs.expenses.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import com.google.firebase.storage.StorageReference;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.firebase.FirebaseActivity;
import io.lucalabs.expenses.views.fragments.PhotoPreviewFragment;
import io.lucalabs.expenses.views.fragments.ReceiptFormFragment;

public class ReceiptActivity extends FirebaseActivity {
    private ReceiptFormFragment mFormFragment;
    private boolean mShowingPreview;
    private CoordinatorLayout mCoordinatorLayout;

    private static final String TAG = ReceiptActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayDeleteIcon();
        setTitle(R.string.receipt_activity_title);
        setContentView(R.layout.activity_receipt);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.container);
        mFormFragment = ReceiptFormFragment.newInstance();

        if (savedInstanceState == null)
            showForm();
    }

    protected void onResume() {
        super.onResume();
    }

    private void showForm() {
        mShowingPreview = false;

        exitFullscreen();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mFormFragment)
                .commit();
    }

    public void showPreview(byte[] previewImage, StorageReference storageRef) {
        mShowingPreview = true;

        PhotoPreviewFragment photoPreviewFragment = PhotoPreviewFragment.newInstance();
        photoPreviewFragment.setPreviewImage(previewImage);
        photoPreviewFragment.setStorageRef(storageRef);

        enterFullscreen();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, photoPreviewFragment)
                .commit();
    }

    private void enterFullscreen() {
        mCoordinatorLayout.setFitsSystemWindows(false);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void exitFullscreen() {
        mCoordinatorLayout.setFitsSystemWindows(true);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
