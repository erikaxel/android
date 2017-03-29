package io.lucalabs.expenses.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Updates, reads storage environment (development, staging, production) to/from SharedPreferences
 */
public class EnvironmentManager {
    private static String environment;
    private static String TAG = "EnvironmentManager";

    public static String currentEnvironment(Context context) {
        if (environment == null)
            readEnvironment(context);
        return environment;
    }

    private static void readEnvironment(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String savedEnvironment = sharedPreferences.getString("environment_pref", "");

        switch (savedEnvironment.toLowerCase()) {
            case "production":
                environment = "production";
                break;
            case "staging":
                environment = "staging";
                break;
            case "development":
                environment = "development";
                break;
            default:
                environment = "production";
        }
        Log.i(TAG, "Updated environment to " + environment);
    }

    public static void reset() {
        Log.i(TAG, "Resetting environment");
        environment = null;
    }
}
