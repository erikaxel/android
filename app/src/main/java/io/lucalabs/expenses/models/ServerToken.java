package io.lucalabs.expenses.models;

/**
 * Representation of the Rails Server Token
 */

public class ServerToken {
    private String token;
    private String expires_at;
    private String min_android_version;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(String expires_at) {
        this.expires_at = expires_at;
    }

    public String getMin_android_version() {
        return min_android_version;
    }

    public void setMin_android_version(String min_android_version) {
        this.min_android_version = min_android_version;
    }
}
