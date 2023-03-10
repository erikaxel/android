package io.lucalabs.expenses.views.presenters;

import android.content.Context;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.models.Receipt;

public class ReceiptPresenter {
    

    public static String getStatusString(Context context, Receipt receipt) {
        if (receipt.getInternal_status() == null)
            return context.getString(R.string.name_not_found);

        switch (receipt.getInternal_status()) {
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
                return receipt.getMerchant_name();
        }
    }

    public static String getMerchantString(Context context, Receipt receipt) {
        if (receipt.getMerchant_name() == null)
            return getStatusString(context, receipt);
        else {
            String merchantString = receipt.getMerchant_name();
            if (merchantString.length() > 24)
                return merchantString.substring(0, 20) + " ...";
            else return merchantString;
        }
    }

    public static String getPrettyAmount(Receipt receipt) {
        return receipt.getAmountString().replaceAll("([.]00)", ".-").replaceAll("([,]00)", ",-");
    }

    /**
     * @return Firebase status if it exists, fall back on internal status, then "NOSTATUS"
     */
    public static String getApplicableStatus(Receipt receipt) {
        if(receipt.getStatus() != null)
            return receipt.getStatus();
        else if(receipt.getInternal_status() != null)
            return receipt.getInternal_status().name();
        else return "NOSTATUS";
    }
}
