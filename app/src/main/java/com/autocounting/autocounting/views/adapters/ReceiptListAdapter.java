package com.autocounting.autocounting.views.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.autocounting.autocounting.R;
import com.autocounting.autocounting.managers.EnvironmentManager;
import com.autocounting.autocounting.models.Receipt;
import com.autocounting.autocounting.network.storage.ReceiptStorage;
import com.autocounting.autocounting.utils.ImageFetcher;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ReceiptListAdapter extends FirebaseListAdapter<Receipt> {

    private static final String TAG = "ReceiptListAdapter";

    public ReceiptListAdapter(Activity activity, DatabaseReference dbReference) {
        super(activity, Receipt.class, R.layout.receipt_list_item, dbReference);
    }

    @Override
    protected void populateView(View v, Receipt receipt, int position) {
        if (receipt == null || receipt.getFirebase_ref() == null)
            return;

        ((TextView) v.findViewById(R.id.receipt_text)).setText(receipt.getMerchantString());
        ((TextView) v.findViewById(R.id.receipt_price)).setText(receipt.getAmountString());
        ((TextView) v.findViewById(R.id.receipt_date)).setText(receipt.getDateString(mActivity));

        List<Receipt> cachedReceipts = Receipt.find(Receipt.class, "firebaseref = ?", receipt.getFirebase_ref());

        if (cachedReceipts.isEmpty())
            setThumbnailFromFirebase(v, receipt);
        else {
            setThumbnailFromCache(v, cachedReceipts, receipt);
        }
    }

    /**
     * Sets receipt thumbnail from Firebase.
     * Caches it in memory with Glide.
     */
    private void setThumbnailFromFirebase(View v, Receipt receipt) {
        StorageReference storageReference = ReceiptStorage
                .getUserReference(FirebaseAuth.getInstance().getCurrentUser(),
                        EnvironmentManager.currentEnvironment(mActivity))
                .child(receipt.getFirebase_ref())
                .child("pages")
                .child("0.thumbnail.jpg");

        Glide.with(mActivity)
                .using(new FirebaseImageLoader())
                .load(storageReference)
                .into((ImageView) v.findViewById(R.id.receipt_thumb));
    }


    /**
     * Sets receipt thumbnail from SQLite database.
     * If receipt has been interpreted on server, the cached receipt is deleted and the one
     * from firebase is used instead.
     */
    private void setThumbnailFromCache(View v, List<Receipt> cachedReceipts, Receipt receipt) {
        Log.i(TAG, "Setting thumbnail from cache. Interpreted? " + receipt.isInterpreted());
        Receipt cachedReceipt = cachedReceipts.get(0);
        if(receipt.isInterpreted()){
            setThumbnailFromFirebase(v, receipt);
            Log.i(TAG, "Deleting ...");
            cachedReceipt.delete();
            return;
        }

        new ImageFetcher((ImageView) v.findViewById(R.id.receipt_thumb)).execute(cachedReceipt);
    }

    /**
     * Reverses list order
     */
    @Override
    public Receipt getItem(int position) {
        Object obj = super.getItem(super.getCount() - position - 1);
        if (obj instanceof Receipt)
            return super.getItem(super.getCount() - position - 1);
        else return null;
    }
}
