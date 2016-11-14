package com.autocounting.autocounting.views.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.autocounting.autocounting.R;
import com.autocounting.autocounting.models.Receipt;
import com.autocounting.autocounting.utils.AnimationRunner;
import com.autocounting.autocounting.utils.ImageFetcher;

import java.util.List;

public class ReceiptListAdapter extends BaseAdapter {
    private Context context;
    private List<Receipt> receipts;
    private boolean imageWasAdded;

    public ReceiptListAdapter(Context context, List<Receipt> receipts, boolean imageWasAdded) {
        this.context = context;
        this.receipts = receipts;
        this.imageWasAdded = imageWasAdded;
    }

    @Override
    public int getCount() {
        return receipts.size();
    }

    @Override
    public Receipt getItem(int position) {
        return receipts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View v, ViewGroup viewGroup) {
        Log.i("OfflineActivity", "position == " + position);

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            v = (View) inflater.inflate(R.layout.receipt_list_item, null);

            if (imageWasAdded && position == 0)
                flash(v);
        }

        Receipt receipt = receipts.get(position);

        TextView receiptTitle = (TextView) v.findViewById(R.id.receipt_text);
        receiptTitle.setText("Waiting for connection ...");

        ImageView receiptThumb = (ImageView) v.findViewById(R.id.receipt_thumb);
        new ImageFetcher(receiptThumb).execute(receipt);

        return v;
    }

    private void flash(View v) {
        Log.d("OfflineActivity", "Flash");
        v.setDrawingCacheEnabled(true);
        Thread animationThread = new Thread(new AnimationRunner(context, v, android.R.anim.slide_in_left, 500));
        animationThread.setPriority(Thread.MAX_PRIORITY);
        animationThread.run();
    }
}
