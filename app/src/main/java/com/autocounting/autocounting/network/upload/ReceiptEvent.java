package com.autocounting.autocounting.network.upload;

import android.app.Activity;
import android.content.Context;

import com.basecamp.turbolinks.TurbolinksSession;

/*
 * Handles Javascript calls to browser
 */
public class ReceiptEvent {

    private Activity activity;
    private String filename;

    public ReceiptEvent(Activity activity, String filename) {
        this.activity = activity;
        this.filename = filename;
    }

    public void receiptAdded() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                TurbolinksSession.getDefault(activity).getWebView().loadUrl("javascript:Receipt.add_receipt({'receipt[image_file_name]':'" + filename + ".jpg'});");
            }
        });
    }
}
