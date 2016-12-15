package com.autocounting.autocounting.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.autocounting.autocounting.managers.EnvironmentManager;

/**
 * Contains Rails server URLs
 */
public class Routes {

    // Website URLs
    private static final String DEVELOPMENT_BASE_URL = "http://10.10.10.193:3000/"; // ip addr show
    private static final String STAGING_BASE_URL = "https://staging.lucalabs.io";
    private static final String PRODUCTION_BASE_URL = "https://expenses.lucalabs.io";

    private static final String RECEIPT_PATH = "/api/v1/receipts.json";

    // Firebase Storage URLs
    public static final String FIREBASE_STORAGE_URL = "gs://eu-autocounting";

    private static final String TAG = "Routes";

    public static String receiptsUrl(Context context) {
        return baseUrl(context) + RECEIPT_PATH;
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
