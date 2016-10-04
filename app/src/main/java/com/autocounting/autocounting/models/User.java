package com.autocounting.autocounting.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class User {

    private String token;
    private String uid;
    private SharedPreferences preferences;
    private String tempName;

    public User(Context context, String token, String uid) {
        setPreferences(context);
        setToken(token);
        setUid(uid);
        generateTempName();
    }

    public User(Context context) {
        setPreferences(context);
        generateTempName();
        this.token = getSavedToken();
        this.uid = getSavedUid();
    }

    public static User getCurrentUser(Context context) {
        return new User(context);
    }

    public String generateUserFileLocation(String type, Boolean needsNewName) {
        return new StringBuilder("receipts/")
                .append(getSavedUid())
                .append("/")
                .append(tempName)
                .append(".")
                .append(type)
                .append(".jpg")
                .toString();
    }

    public static void clearSavedData(Context context){
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putString("token", null);
        editor.putString("uid", null);
        editor.apply();
    }

    public String getSavedToken() {
        return preferences.getString("token", "");
    }

    public String getTempName() {
        return tempName;
    }

    private void generateTempName(){
        tempName = String.valueOf(System.currentTimeMillis() / 1000L);
    }

    public String getSavedUid() {
        return preferences.getString("uid", "");
    }

    public void save() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.putString("uid", uid);
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
}
