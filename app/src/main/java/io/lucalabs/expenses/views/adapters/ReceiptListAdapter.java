package io.lucalabs.expenses.views.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.views.presenters.ReceiptPresenter;

public class ReceiptListAdapter extends FirebaseListAdapter<ReceiptPresenter> {
    private static final String TAG = "ReceiptListAdapter";
    private FirebaseUser user;

    public ReceiptListAdapter(Activity activity, Query query) {
        super(activity, ReceiptPresenter.class, R.layout.receipt_list_item, query);
        user = FirebaseAuth.getInstance().getCurrentUser();
        Log.i(TAG, "------------------- RECEIPT LIST -------------------");
    }

    @Override
    protected void populateView(View view, final ReceiptPresenter receipt, int position) {
        Log.i(TAG, "receipt " + position + ": " + receipt.getMerchant_name());

        setThumbnailFromCache(view, receipt);
        setThumbnailFromFirebase(view, receipt);

        ((TextView) view.findViewById(R.id.receipt_text)).setText(receipt.getMerchantString(mActivity));
        ((TextView) view.findViewById(R.id.receipt_price)).setText(receipt.getPrettyAmountString());
        ((TextView) view.findViewById(R.id.receipt_date)).setText(receipt.getUsedDateString(mActivity));
    }

    /*
     * This method runs when a separate device has uploaded a receipt, and the image
     * is not yet in storage.
     */

    private void handleReceiptFromOtherDevice(View view) {
        ((TextView) view.findViewById(R.id.receipt_text)).setText(mActivity.getString(R.string.uploading_external_receipt));
        ((ImageView) view.findViewById((R.id.receipt_thumb))).setImageResource(R.drawable.ic_menu_send);
    }

    /**
     * Sets receipt thumbnail and merchant name from Firebase.
     * Caches it in memory with Glide.
     */
    private void setThumbnailFromFirebase(View view, Receipt receipt) {
        StorageReference ref = Inbox.receiptImage(mActivity, receipt, "thumbnail");
        Glide.with(mActivity)
                .using(new FirebaseImageLoader())
                .load(ref)
                .into((ImageView) view.findViewById(R.id.receipt_thumb));
    }

    /**
     * Sets receipt thumbnail and merchant name (status) from SQLite database.
     */
    private void setThumbnailFromCache(final View view, final ReceiptPresenter receipt) {
        Inbox.cachedReceiptImage(mActivity, receipt.getFirebase_ref()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                receipt.setFilename(dataSnapshot.getValue(String.class));

                if(receipt.getFilename() != null)
                    Glide.with(mActivity)
                            .load(receipt.getImage(mActivity))
                            .asBitmap()
                            .into((ImageView) view.findViewById(R.id.receipt_thumb));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Retrieves list items in reverse order (latest first).
     *
     * @return null if object is not a Receipt.
     */
    @Override
    public ReceiptPresenter getItem(int position) {
        Object obj = super.getItem(super.getCount() - position - 1);
        if (obj instanceof ReceiptPresenter)
            return (ReceiptPresenter) obj;
        else return null;
    }
}
