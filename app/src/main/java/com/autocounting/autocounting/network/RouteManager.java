package com.autocounting.autocounting.network;

public class RouteManager {

    //public static final String BASE_URL = "https://staging.autocounting.no";
    // Alternative BASE_URLs
    public static final String BASE_URL = "http://10.10.10.193:3000";
    // public static final String BASE_URL = "http://192.168.1.107:3000";
    // public static final String BASE_URL = "http://10.10.10.180:3000";
    private static final String RECEIPT_PATH = "/receipts";
    private static final String ERROR_PATH = "/error";

    // Firebase Storage URLs
    public static final String FIREBASE_STORAGE_URL = "gs://autocounting.appspot.com";
    public static final String RECEIPT_STORAGE_PATH = "/development/receipts";

    public static String receiptsUrl() {
        return BASE_URL + RECEIPT_PATH;
    }

    public static String getReceiptStoragePath(Context context){

    }

    public static String errorUrl() {
        return BASE_URL + ERROR_PATH;
    }
}
