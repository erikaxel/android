package io.lucalabs.expenses.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/*
 * Allows the user to fetch the current Firebase user.
 * Handles token storage.
 */
public class User {

    private static String token;
    private static final String TAG = "User";

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getToken(Context context) {
        if (token == null)
            token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "");

        return token;
    }

    public static void setToken(Context context, String newToken) {
        token = newToken;
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putString("token", token);
        editor.apply();
    }

    public static boolean isAdmin() {
        String email = getCurrentUser().getEmail();
        return email.contains("@autocounting.com")
                || email.contains("@lucalabs.io")
                || email.contains("tmbv93@gmail.com");
    }
}
