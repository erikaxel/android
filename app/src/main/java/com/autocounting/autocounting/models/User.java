package com.autocounting.autocounting.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class User {

    // Remove these
    private String uid;
    private String email;
    private Context context;
    private SharedPreferences preferences;

    private static String token;

    private static final String TAG = "User";

    public User(Context context, String uid, String email) {
        setContext(context);
        setPreferences();
        setUid(uid);
        setEmail(email);
    }

    public User(Context context) {
        setContext(context);
        setPreferences();
        this.uid = getSavedUid();
    }

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void clearSavedData(Context context) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putString("token", null);
        editor.putString("uid", null);
        editor.putString("email", null);
        editor.apply();
    }

    public String getSavedUid() {
        return preferences.getString("uid", "");
    }

    public String getSavedEmail() {
        return preferences.getString("email", "");
    }

    public void save() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("uid", uid);
        editor.putString("email", email);
        editor.apply();
    }

    private void setPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static boolean isAdmin() {
        String email = getCurrentUser().getEmail();
        return email.contains("@autocounting.com")
                || email.contains("@lucalabs.io")
                || email.contains("tmbv93@gmail.com");
    }

    public void logout(Context context) {
        Receipt.deleteReceiptFolder();
        clearSavedData(context);
    }
}
