package io.lucalabs.expenses.views.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.ReceiptActivity;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Receipt;

public class ReceiptListAdapter extends FirebaseListAdapter<Receipt> {
    private static final String TAG = "ReceiptListAdapter";
    private FirebaseUser user;

    public ReceiptListAdapter(Activity activity, Query query) {
        super(activity, Receipt.class, R.layout.receipt_list_item, query);
        user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "------------------- INSTANCE -------------------");
    }

    @Override
    protected void populateView(View view, final Receipt receipt, int position) {
        Log.d(TAG, "receipt " + position + ": " + receipt.getMerchant_name());

        if (receipt == null) {
            ((TextView) view.findViewById(R.id.receipt_text)).setText(R.string.corrupt_data_notice);
            return;
        }

        List<Receipt> cachedReceipts;
        if(receipt.getFirebase_ref() != null)
            cachedReceipts = Receipt.find(Receipt.class, "firebaseref = ?", receipt.getFirebase_ref());
        else cachedReceipts = null;

        if(cachedReceipts == null){
            handleReceiptFromOtherDevice(view);
            return;
        }

        if(cachedReceipts.size() > 0) {
            Log.d(TAG, "cached receipt present");
            Receipt cachedReceipt = cachedReceipts.get(0);
            setThumbnailFromCache(view, cachedReceipt, receipt);
            receipt.updateFromCache(cachedReceipt);
        } else {
            setThumbnailFromFirebase(view, receipt);
        }

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
        StorageReference ref = Inbox.receiptThumbnail(mActivity, receipt);

        Glide.with(mActivity)
                .using(new FirebaseImageLoader())
                .load(ref)
                .into((ImageView) view.findViewById(R.id.receipt_thumb));
    }

    /**
     * Sets receipt thumbnail and merchant name (status) from SQLite database.
     */
    private void setThumbnailFromCache(View view, Receipt cachedReceipt, Receipt receipt) {
        Glide.with(mActivity)
                .load(cachedReceipt.getImage(mActivity))
                .asBitmap()
                .into((ImageView) view.findViewById(R.id.receipt_thumb));
    }

    /**
     * Retrieves list items in reverse order (latest first).
     * @return null if object is not a Receipt.
     */
    @Override
    public Receipt getItem(int position) {
        Object obj = super.getItem(super.getCount() - position - 1);
        if (obj instanceof Receipt)
            return (Receipt) obj;
        else return null;
    }
}
