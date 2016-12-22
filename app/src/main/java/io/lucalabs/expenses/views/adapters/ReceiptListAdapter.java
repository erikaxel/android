package io.lucalabs.expenses.views.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.network.storage.ReceiptStorage;
import io.lucalabs.expenses.utils.ImageFetcher;

public class ReceiptListAdapter extends FirebaseListAdapter<Receipt> {
    private static final String TAG = "ReceiptListAdapter";
    private FirebaseUser user;

    public ReceiptListAdapter(Activity activity, Query query) {
        super(activity, Receipt.class, R.layout.receipt_list_item, query);
        user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "----------------------------------------------------------");
    }

    @Override
    protected void populateView(View view, Receipt receipt, int position) {
        Log.d(TAG, "Receipt firebase_ref: " + receipt.getFirebase_ref());
        if (receipt == null) {
            ((TextView) view.findViewById(R.id.receipt_text)).setText(R.string.corrupt_data_notice);
            return;
        }
        List<Receipt> cachedReceipts = Receipt.find(Receipt.class, "firebaseref = ?", receipt.getFirebase_ref());
//        Log.i("ReceiptStatus", cachedReceipts.get(0).getFirebase_ref() + " listcheck " + cachedReceipts.get(0).getStatus());

        if (cachedReceipts.size() > 0) {
            Log.d(TAG, "Setting thumbnail from cached receipt");
            setThumbnailFromCache(view, cachedReceipts, receipt);
        } else {
            Log.d(TAG, "Setting thumbnail from firebase");
            setThumbnailFromFirebase(view, receipt);
        }

        ((TextView) view.findViewById(R.id.receipt_text)).setText(receipt.getMerchant_name());
        ((TextView) view.findViewById(R.id.receipt_price)).setText(receipt.getAmountString());
        ((TextView) view.findViewById(R.id.receipt_date)).setText(receipt.getDateString(mActivity));
    }

    /**
     * Sets receipt thumbnail from Firebase.
     * Caches it in memory with Glide.
     */
    private void setThumbnailFromFirebase(View v, Receipt receipt) {
        StorageReference storageReference = ReceiptStorage
                .getUserReference(user,
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
     * from Firebase is used instead.
     */
    private void setThumbnailFromCache(View v, List<Receipt> cachedReceipts, Receipt receipt) {
        Receipt cachedReceipt = cachedReceipts.get(0);
        new ImageFetcher((ImageView) v.findViewById(R.id.receipt_thumb)).execute(cachedReceipt);

        if(receipt.isInterpreted()) {
            cachedReceipt.updateStatus(Receipt.Status.PARSED);
//            cachedReceipt.delete();
        }
    }

    /**
     * Retrieves list items in reverse order (latest first).
     *
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
