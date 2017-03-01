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
    private static final String DEVELOPMENT_BASE_URL = "http://10.10.10.193:3000"; // hostname -I
    private static final String STAGING_BASE_URL = "https://staging.lucalabs.com";
    private static final String PRODUCTION_BASE_URL = "https://system.lucalabs.com";

    private static final String RECEIPTS_PATH = "/api/v1/receipts";
    private static final String EXPENSE_REPORTS_PATH = "/api/v1/expense_reports";

    // Firebase Storage URLs
    public static final String FIREBASE_STORAGE_URL = "gs://eu-autocounting";

    private static final String TAG = "Routes";

    public static String objectsPath(Object object, boolean isPostRequest) {
        Object pathArg = isPostRequest ? null : object;

        switch(object.getClass().getSimpleName()){
            case "ExpenseReport" :
                return expenseReportsPath((ExpenseReport) pathArg);
            case "Receipt" :
                return receiptsPath((Receipt) pathArg);
            default :
                return null;
        }
    }

    public static String receiptsUrl(Context context, Receipt receipt){
        return baseUrl(context) + receiptsPath(receipt);
    }

    public static String receiptsPath(Receipt receipt){
        String url = RECEIPTS_PATH;;
        if (receipt != null)
            url += "/" + receipt.getFirebase_ref();
        return url + ".json";
    }

    public static String expenseReportsUrl(Context context, ExpenseReport expenseReport){
        return baseUrl(context) + expenseReportsPath(expenseReport);
    }

    public static String expenseReportsPath(ExpenseReport expenseReport) {
        String url = EXPENSE_REPORTS_PATH;
        if (expenseReport != null)
            url += "/" + expenseReport.getFirebase_ref();
        return url + ".json";
    }

    public static String getFullPath(Context context, String path){
        return baseUrl(context) + path;
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
