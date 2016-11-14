package com.autocounting.autocounting.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class NetworkManager {

    public final static int OK = 0;
    public final static int INTERNET_UNAVAILABLE = 1;
    public final static int SERVER_ERROR = 500;

    public static boolean networkIsAvailable(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean readyToUpload(Context context) {

        NetworkInfo networkInfo = getNetworkInfo(context);

        if(networkInfo != null) {
            boolean isConnectedToWifi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            boolean isConnectedToCellular = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;

            return (networkInfo.isConnected()) && (isConnectedToWifi || (isConnectedToCellular && !PreferenceManager.
                    getDefaultSharedPreferences(context).getBoolean("wifi_only_pref", false)));
        }

        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

}
