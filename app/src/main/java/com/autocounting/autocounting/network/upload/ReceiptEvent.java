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
    private String filename;

    public ReceiptEvent(Context context, String filename) {
        this.context = context;
        this.filename = filename;
    }

    public void receiptAdded() {
        Log.i(TAG, "Javascript: Receipt.add_receipt " + filename);
        new Handler(Looper.getMainLooper()).post((new Runnable() {
            public void run() {
                TurbolinksSession.getDefault(context).getWebView().loadUrl("javascript:Receipt.add_receipt({'receipt[image_file_name]':'" + filename + ".jpg'});");
            }
        }));
    }
}
