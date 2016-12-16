package io.lucalabs.expenses.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * This class has methods that return information about the current network connectivity
 * state of the device.
 */
public class NetworkStatus {

    public final static int OK = 0;
    public final static int INTERNET_UNAVAILABLE = 1;
    public final static int SERVER_ERROR = 500;

    /**
     * @return true if the device is connected to the Internet. False if not.
     */
    public static boolean networkIsAvailable(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * @return true if the device is connected to the Internet, using the user's preferred
     * network type. Returns false if not. (Current preference types: Cellular/Wifi, Wifi only)
     */
    public static boolean appropriateNetworkIsAvailable(Context context) {
        if (networkIsAvailable(context)) {
            NetworkInfo networkInfo = getNetworkInfo(context);

            if (networkInfo != null) {
                boolean isConnectedToWifi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                boolean isConnectedToCellular = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;

                return (networkInfo.isConnected()) && (isConnectedToWifi || (isConnectedToCellular && !PreferenceManager.
                        getDefaultSharedPreferences(context).getBoolean("wifi_only_pref", false)));
            }
        }
        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

}
