package io.lucalabs.expenses.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Allows the user to fetch the current Firebase user.
 * Handles Firebase firebaseToken storage and Rails server firebaseToken storage.
 */
public class User {
    private static String firebaseToken;
    private static ServerToken serverToken;
    private static final String TAG = "User";

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getFirebaseToken(Context context) {
        if (firebaseToken == null)
            firebaseToken = PreferenceManager.getDefaultSharedPreferences(context).getString("firebaseToken", "");
        return firebaseToken;
    }

    public static void setFirebaseToken(Context context, String newFirebaseToken) {
        firebaseToken = newFirebaseToken;
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putString("firebaseToken", firebaseToken);
        editor.apply();
    }

    public static ServerToken getServerToken(Context context){
        if(serverToken == null){
            ServerToken preferenceServerToken = new ServerToken();
            serverToken.setToken(PreferenceManager.getDefaultSharedPreferences(context).getString("serverToken", ""));
            serverToken.setExpires_at(PreferenceManager.getDefaultSharedPreferences(context).getString("serverTokenExpiresAt", ""));
            serverToken.setMin_android_version(PreferenceManager.getDefaultSharedPreferences(context).getString("minAndroidVersion", ""));
            serverToken = preferenceServerToken;
        }

        return serverToken;
    }

    public static void setServerToken(Context context, ServerToken newServerToken){
        serverToken = newServerToken;
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putString("serverToken", serverToken.getToken());
        editor.putString("serverTokenExpiresAt", serverToken.getExpires_at());
        editor.putString("minAndroidVersion", serverToken.getMin_android_version());
        editor.apply();
    }

    public static boolean isAdmin() {
        String email = getCurrentUser().getEmail();
        return email.contains("@autocounting.com")
                || email.contains("@lucalabs.io")
                || email.contains("@lucalabs.com")
                || email.contains("tmbv93@gmail.com")
                || email.contains("tomas.veiden@gmail.com");
    }
}
