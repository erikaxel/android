package io.lucalabs.expenses.models;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import io.lucalabs.expenses.network.Routes;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Handles and synchronizes device information with The Rails Server
 */
public class Device {

    private final static String TAG = "DeviceModel";

    /**
     * Register a new device on the backend server.
     * Will update existing device if one with the same fcm_token exists
     */
    public static void register(Context context) {
        OkHttpClient client = new OkHttpClient();

        FormBody formBody = new FormBody.Builder()
                .add("data[attributes][fcm_token]", FirebaseInstanceId.getInstance().getToken())
                .add("data[attributes][os_string]", getDeviceString(context))
                .build();

        Request request = new Request.Builder()
                .post(formBody)
                .url(Routes.deviceRegistrationUrl(context))
                .addHeader("Authorization", "Bearer " + User.getServerToken(context).getToken())
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.i(TAG, "Device create response: " + response.code() + ", message: " + response.message());
        } catch (IOException e) {
            Log.w(TAG, "Couldn't create device");
            e.printStackTrace();
        }
    }

    /**
     * @return String in format "os:os_version:manufacturer:model:app_version"
     * eg. "android:7.0:samsung:SM-G930F:1.1.10"
     */
    private static String getDeviceString(Context context) {
        try {
            return new StringBuilder("android")
                    .append(":")
                    .append(Build.VERSION.RELEASE)
                    .append(":")
                    .append(Build.MANUFACTURER)
                    .append(":")
                    .append(Build.MODEL)
                    .append(":")
                    .append(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName)
                    .toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
}
