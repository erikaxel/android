package com.autocounting.autocounting.views.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.autocounting.autocounting.R;
import com.autocounting.autocounting.managers.EnvironmentManager;
import com.autocounting.autocounting.models.Receipt;
import com.autocounting.autocounting.network.storage.ReceiptStorage;
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
        if(receipt.getFirebase_ref() != null) {
            ((TextView) v.findViewById(R.id.receipt_text)).setText(receipt.getMerchantString());
            ((TextView) v.findViewById(R.id.receipt_price)).setText(receipt.getAmountString());

            List<Receipt> cachedReceipts = Receipt.find(Receipt.class, "firebaseref = ?", receipt.getFirebase_ref());

            if(!cachedReceipts.isEmpty()) {
                Receipt cachedReceipt = cachedReceipts.get(0);
                Bitmap cachedBitmap = BitmapFactory.decodeByteArray(cachedReceipt.getImage(), 0, cachedReceipt.getImage().length);
                ((ImageView) v.findViewById(R.id.receipt_thumb)).setImageBitmap(cachedBitmap);
            }

            else {

                StorageReference storageReference = ReceiptStorage
                        .getUserReference(FirebaseAuth.getInstance().getCurrentUser(),
                                EnvironmentManager.currentEnvironment(mActivity))
                        .child(receipt.getFirebase_ref())
                        .child("pages")
                        .child("0.thumbnail.jpg");

                Glide.with(mActivity)
                        .using(new FirebaseImageLoader())
                        .load(storageReference)
                        .placeholder(R.drawable.ic_menu_send)
                        .into((ImageView) v.findViewById(R.id.receipt_thumb));

            }
        } else {
            Log.w(TAG, "Receipt without firebase ref detected");
        }
    }

    /**
     * Reverses list order
     */
    @Override
    public Receipt getItem(int position) {
        return super.getItem(super.getCount() - position - 1);
    }
}
