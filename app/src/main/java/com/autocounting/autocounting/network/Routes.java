package com.autocounting.autocounting.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Routes {

    // Website URLs
    private static final String DEVELOPMENT_BASE_URL = "http://192.168.10.143:3000";
    private static final String STAGING_BASE_URL = "https://staging.lucalabs.io";
    private static final String PRODUCTION_BASE_URL = "https://expenses.lucalabs.io";

    private static final String RECEIPT_PATH = "/api/v1/receipts";
    private static final String ERROR_PATH = "/error";

    // Firebase Storage URLs
    public static final String FIREBASE_STORAGE_URL = "gs://eu-autocounting";
    private static final String DEVELOPMENT_BUCKET = "/development/receipts";
    private static final String STAGING_BUCKET = "/staging/receipts";
    private static final String PRODUCTION_BUCKET = "/production/receipts";

    private String environment;

    private static final String TAG = "Routes";

    public String getEnvironment() {
        return environment;
    }

    public String receiptsUrl() {
        return baseUrl() + RECEIPT_PATH;
    }

    public Routes(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setEnvironment(sharedPreferences.getString("environment_pref", ""));
    }

    private void setEnvironment(String environment){
        environment = environment.toLowerCase();
        switch (environment){
            case "production":
                this.environment = "production";
                break;
            case "staging":
                this.environment = "staging";
                break;
            case "development":
                this.environment = "development";
                break;
            default:
                this.environment = "production";
        }
    }

    public String storageBucket() {
        switch (environment) {
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

    public String baseUrl() {
        switch (environment) {
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
    public String errorUrl() {
        return baseUrl() + ERROR_PATH;
    }
}
