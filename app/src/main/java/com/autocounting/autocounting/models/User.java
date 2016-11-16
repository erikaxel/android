package com.autocounting.autocounting.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.util.Log;

public class User {

    private String token;
    private String uid;
    private String email;
    private SharedPreferences preferences;

    public User(Context context, String token, String uid, String email) {
        setPreferences(context);
        setToken(token);
        setUid(uid);
        setEmail(email);
    }

    public User(Context context) {
        setPreferences(context);
        this.token = getSavedToken();
        this.uid = getSavedUid();
    }

    public static User getCurrentUser(Context context) {
        return new User(context);
    }

    // Move to receipts
    public String generateUserFileLocation(String firebaseReference, String storagePath) {
        return new StringBuilder(storagePath)
                .append("/")
                .append(getSavedUid())
                .append("/")
                .append(firebaseReference)
                .append("/pages")
                .append("/0.original.jpg")
                .toString();
    }

    public static void clearSavedData(Context context) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putString("token", null);
        editor.putString("uid", null);
        editor.putString("email", null);
        editor.apply();
    }

    public String getSavedToken() {
        return preferences.getString("token", "");
    }

    public String getSavedUid() {
        return preferences.getString("uid", "");
    }

    public String getSavedEmail(){return preferences.getString("email", "");}

    public void save() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.putString("uid", uid);
        editor.putString("email", email);
        editor.apply();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private void setPreferences(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return getSavedEmail().contains("@autocounting.com")
                || getSavedEmail().contains("@lucalabs.io")
                || getSavedEmail().contains("tmbv93@gmail.com");
    }

    public void logout(Context context) {
        Receipt.deleteReceiptFolder();
        clearSavedData(context);
    }
}
