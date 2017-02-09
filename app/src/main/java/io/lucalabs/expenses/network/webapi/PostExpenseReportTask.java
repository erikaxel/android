package io.lucalabs.expenses.network.webapi;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

import io.lucalabs.expenses.models.ExpenseReport;
import io.lucalabs.expenses.models.User;
import io.lucalabs.expenses.network.Routes;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostExpenseReportTask extends AsyncTask<Void, Void, Void> {
    private Context mContext;
    private ExpenseReport mExpenseReport;

    private static final String TAG = "PostExpenseReportTask";

    public PostExpenseReportTask(Context context, ExpenseReport expenseReport) {
        mContext = context;
        mExpenseReport = expenseReport;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        post();
        return null;
    }

    private void post() {
        OkHttpClient client = new OkHttpClient();

        FormBody.Builder formBuilder = new FormBody.Builder();
        smartAdd(formBuilder, "expense_report[name]", mExpenseReport.getName());
        smartAdd(formBuilder, "expense_report[comment]", mExpenseReport.getComment());
        smartAdd(formBuilder, "expense_report[source]", mExpenseReport.getSource());
        smartAdd(formBuilder, "expense_report[billable]", mExpenseReport.isBillable() ? "true" : "false");
        smartAdd(formBuilder, "expense_report[travel]", mExpenseReport.isTravel() ? "true" : "false");
        smartAdd(formBuilder, "expense_report[destination]", mExpenseReport.getDestination());
        smartAdd(formBuilder, "expense_report[departure_at]", mExpenseReport.getDeparture_at());
        smartAdd(formBuilder, "expense_report[arrival_at]", mExpenseReport.getArrival_at());
        smartAdd(formBuilder, "expense_report[project_code]", mExpenseReport.getProject_code());
        smartAdd(formBuilder, "expense_report[firebase_ref]", mExpenseReport.getFirebase_ref());
        smartAdd(formBuilder, "token", User.getToken(mContext));

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

    private void smartAdd(FormBody.Builder builder, String paramName, String paramValue) {
        if (paramValue != null)
            builder.add(paramName, paramValue);
    }
}
