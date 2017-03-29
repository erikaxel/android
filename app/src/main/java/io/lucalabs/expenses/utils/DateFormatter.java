package io.lucalabs.expenses.utils;

import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * Formats and handles dates.
 */
public class DateFormatter {

    private static final String FIREBASE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String TAG = "DateFormatter";

    /**
     * @param dateString: A firebase-friendly datestring
     * @return Locale-specific, human-readable datestring
     */
    public static String formatToLocale(Context context, String dateString) {
        if (dateString == null) {
            Log.i(TAG, "Datestring was null");
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(FIREBASE_FORMAT);
        try {
            Date date = sdf.parse(dateString);
            java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
            return dateFormat.format(date);
        } catch (ParseException e) {
            Log.i(TAG, "Couldn't parse date");
            e.printStackTrace();
        }

        return "";
    }

    public static String toDateString(int year, int month, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat(FIREBASE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        return sdf.format(calendar.getTime());
    }
}
