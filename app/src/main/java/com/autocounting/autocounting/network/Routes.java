package com.autocounting.autocounting.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.autocounting.autocounting.managers.EnvironmentManager;

/**
 * Contains routes to the Rails server
 * @deprecated Will be phased out entirely.
 */
public class Routes {

    // Website URLs
    private static final String DEVELOPMENT_BASE_URL = "http://192.168.10.143:3000";
    private static final String STAGING_BASE_URL = "https://staging.lucalabs.io";
    private static final String PRODUCTION_BASE_URL = "https://expenses.lucalabs.io";

    private static final String RECEIPT_PATH = "/api/v1/receipts";

    // Firebase Storage URLs
    public static final String FIREBASE_STORAGE_URL = "gs://eu-autocounting";
    private static final String DEVELOPMENT_BUCKET = "/development/receipts";
    private static final String STAGING_BUCKET = "/staging/receipts";
    private static final String PRODUCTION_BUCKET = "/production/receipts";

    private static final String TAG = "Routes";

    public static String receiptsUrl(Context context) {
        return baseUrl(context) + RECEIPT_PATH;
    }

    public static String storageBucket(Context context) {
        switch (EnvironmentManager.currentEnvironment(context)) {
            case "production":
                return PRODUCTION_BUCKET;
            case "staging":
                return STAGING_BUCKET;
            case "development":
                return DEVELOPMENT_BUCKET;
            default:
                Log.w(TAG, "Using default bucket");
                return PRODUCTION_BUCKET;
        }
    }

    private static String baseUrl(Context context) {
        switch (EnvironmentManager.currentEnvironment(context)) {
            case "Production":
                return PRODUCTION_BASE_URL;
            case "Staging":
                return STAGING_BASE_URL;
            case "Development":
                return DEVELOPMENT_BASE_URL;
            default:
                Log.w(TAG, "Using default base url");
                return PRODUCTION_BASE_URL;
        }
    }
}
