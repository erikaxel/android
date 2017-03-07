package io.lucalabs.expenses.views.presenters;

import android.content.Context;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.models.Receipt;

public class ReceiptPresenter extends Receipt {

    public String getStatusString(Context context) {
        if (getInternal_status() == null)
            return context.getString(R.string.name_not_found);

        switch (getInternal_status()) {
            case PENDING:
                return context.getString(R.string.waiting_to_upload);
            case UPLOADING:
                return context.getString(R.string.uploading);
            case UPLOADED:
                return context.getString(R.string.uploading);
            case POSTING:
                return context.getString(R.string.uploading);
            case POSTED:
                return context.getString(R.string.interpreting);
            default:
                return getMerchant_name();
        }
    }

    public String getMerchantString(Context context) {
        if (getMerchant_name() == null)
            return getStatusString(context);
        else {
            String merchantString = getMerchant_name();
            if (merchantString.length() > 24)
                return merchantString.substring(0, 20) + " ...";
            else return merchantString;
        }
    }
}
