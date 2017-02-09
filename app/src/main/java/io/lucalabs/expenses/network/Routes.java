package io.lucalabs.expenses.network;

import android.content.Context;
import android.util.Log;

import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.models.ExpenseReport;
import io.lucalabs.expenses.models.Receipt;

/**
 * Contains Rails server URLs
 */
public class Routes {

    // Website URLs
    private static final String DEVELOPMENT_BASE_URL = "http://10.10.10.193:3000"; // ip addr show
    private static final String STAGING_BASE_URL = "https://staging.lucalabs.io";
    private static final String PRODUCTION_BASE_URL = "https://expenses.lucalabs.io";

    private static final String RECEIPT_PATH = "/api/v1/receipts/";
    private static final String EXPENSE_REPORT_PATH = "/api/v1/expense_reports/";

    // Firebase Storage URLs
    public static final String FIREBASE_STORAGE_URL = "gs://eu-autocounting";

    private static final String TAG = "Routes";

    public static String receiptsUrl(Context context, Receipt receipt){
        String url = baseUrl(context) + RECEIPT_PATH;;
        if (receipt != null)
            url += receipt.getFirebase_ref();
        return url + ".json";
    }

    public static String expenseReportsUrl(Context context, ExpenseReport expenseReport) {
        String url = baseUrl(context) + EXPENSE_REPORT_PATH;
        if (expenseReport != null)
            url += expenseReport.getFirebase_ref();
        return url + ".json";
    }

    private static String baseUrl(Context context) {
        switch (EnvironmentManager.currentEnvironment(context)) {
            case "production":
                return PRODUCTION_BASE_URL;
            case "staging":
                return STAGING_BASE_URL;
            case "development":
                return DEVELOPMENT_BASE_URL;
            default:
                Log.w(TAG, "Using default base url");
                return PRODUCTION_BASE_URL;
        }
    }
}
