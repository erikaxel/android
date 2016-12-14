package com.autocounting.autocounting.utils;

import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Formats and handles dates.
 */
public class DateFormatter {

    private static final String TAG = "DateFormatter";

    public static String formatToLocale(String dateString, Context context){
        if(dateString == null) {
            Log.i(TAG, "Datestring was null");
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date = sdf.parse(dateString);
            java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
            return dateFormat.format(date);
        }

        catch(ParseException e) {
            Log.i(TAG, "Couldn't parse date");
            e.printStackTrace();
        }

        return "";
    }
}
