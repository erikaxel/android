package io.lucalabs.expenses.network.webapi;


import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.models.User;
import io.lucalabs.expenses.network.Routes;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PatchReceiptTask extends AsyncTask<Void, Void, Void> {
    private Context mContext;
    private Receipt mReceipt;

    private static final String TAG = "PostExpenseReportTask";

    public PatchReceiptTask(Context context, Receipt expenseReport) {
        mContext = context;
        mReceipt = expenseReport;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        post();
        return null;
    }

    private void post() {
        OkHttpClient client = new OkHttpClient();

        FormBody.Builder formBuilder = new FormBody.Builder();
        smartAdd(formBuilder, "receipt[merchant_name]", mReceipt.getMerchant_name());
        smartAdd(formBuilder, "receipt[amount]", "" + (double) mReceipt.getAmount_cents() / 100);
        smartAdd(formBuilder, "receipt[currency]", mReceipt.getCurrency());
        smartAdd(formBuilder, "receipt[used_date]", mReceipt.getUsed_date());
        smartAdd(formBuilder, "receipt[reimbursable]", mReceipt.isReimbursable() ? "true" : "false");
        smartAdd(formBuilder, "receipt[comment]", mReceipt.getComment());
        smartAdd(formBuilder, "token", User.getToken(mContext));

        Request request = new Request.Builder()
                .url(Routes.receiptsUrl(mContext, mReceipt))
                .patch(formBuilder.build())
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void smartAdd(FormBody.Builder builder, String paramName, String paramValue) {
        if (paramValue != null)
            builder.add(paramName, paramValue);
    }
}
