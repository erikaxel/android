package com.autocounting.autocounting.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

/**
 * Updates, reads storage environment (development, staging, production) to/from SharedPreferences
 */
public class EnvironmentManager {
    private static String environment;

    public static String currentEnvironment(Context context){
        if(environment == null)
            readEnvironment(context);
        return environment;
    }

    private static void readEnvironment(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String savedEnvironment = sharedPreferences.getString("environment_pref", "");

        switch(savedEnvironment.toLowerCase()){
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
    }

    public static void reset(){
        environment = null;
    }
}
