package io.lucalabs.expenses.network.webapi;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import android.util.Log;

import io.lucalabs.expenses.models.ExpenseReport;
import io.lucalabs.expenses.models.User;
import io.lucalabs.expenses.network.Routes;
import io.lucalabs.expenses.utils.RESTBuilder;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PatchExpenseReportTask extends AsyncTask<Void, Void, Void> {
    private Context mContext;
    private ExpenseReport mExpenseReport;

    private static final String TAG = "PatchExpenseReportTask";

    public PatchExpenseReportTask(Context context, ExpenseReport expenseReport) {
        Log.i(TAG, "starting");
        mContext = context;
        mExpenseReport = expenseReport;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        post();
        return null;
    }

    private void post() {
        Log.i(TAG, "posting");

        OkHttpClient client = new OkHttpClient();

        FormBody.Builder formBuilder = new FormBody.Builder();
        RESTBuilder.addToForm(formBuilder, "expense_report[name]", mExpenseReport.getName());
        RESTBuilder.addToForm(formBuilder, "expense_report[comment]", mExpenseReport.getComment());
        RESTBuilder.addToForm(formBuilder, "expense_report[source]", mExpenseReport.getSource());
        RESTBuilder.addToForm(formBuilder, "expense_report[billable]", mExpenseReport.isBillable() ? "true" : "false");
        RESTBuilder.addToForm(formBuilder, "expense_report[travel]", mExpenseReport.isTravel() ? "true" : "false");
        RESTBuilder.addToForm(formBuilder, "expense_report[destination]", mExpenseReport.getDestination());
        RESTBuilder.addToForm(formBuilder, "expense_report[departure_at]", mExpenseReport.getDeparture_at());
        RESTBuilder.addToForm(formBuilder, "expense_report[arrival_at]", mExpenseReport.getArrival_at());
        RESTBuilder.addToForm(formBuilder, "expense_report[project_code]", mExpenseReport.getProject_code());
        RESTBuilder.addToForm(formBuilder, "expense_report[firebase_ref]", mExpenseReport.getFirebase_ref());

        if(mExpenseReport.isFinalized())
            RESTBuilder.addToForm(formBuilder, "expense_report[finalized]", "true");

        RESTBuilder.addToForm(formBuilder, "token", User.getToken(mContext));

        Request request = new Request.Builder()
                .url(Routes.expenseReportsUrl(mContext, mExpenseReport))
                .patch(formBuilder.build())
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
