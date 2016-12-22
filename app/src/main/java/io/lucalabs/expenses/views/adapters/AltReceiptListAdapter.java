package io.lucalabs.expenses.views.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;

import io.lucalabs.expenses.models.Receipt;

public class AltReceiptListAdapter extends ArrayAdapter<Receipt> {
    public AltReceiptListAdapter(Context context, int resource) {
        super(context, resource);
    }

    /**
     * Retrieves list items in reverse order (latest first).
     *
     * @return null if object is not a Receipt.
     */
    @Override
    public Receipt getItem(int position) {
        return super.getItem(super.getCount() - position - 1);
    }
}
