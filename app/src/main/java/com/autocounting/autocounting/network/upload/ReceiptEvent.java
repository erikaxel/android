package com.autocounting.autocounting.network.upload;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.basecamp.turbolinks.TurbolinksSession;

/*
 * Handles Javascript calls to browser
 */
public class ReceiptEvent {

    private static String TAG = "ReceiptEvent";
    private Context context;
    private String reference;

    public ReceiptEvent(Context context, String reference) {
        this.context = context;
        this.reference = reference;
    }

    public void receiptAdded() {
        Log.i(TAG, "Javascript: Receipt.add_receipt " + reference);
        new Handler(Looper.getMainLooper()).post((new Runnable() {
            public void run() {
                TurbolinksSession.getDefault(context).getWebView().loadUrl("javascript:Receipt.add_receipt({'receipt[firebase_ref]':'" + reference + "'});");
            }
        }));
    }
}
