package io.lucalabs.expenses.utils;
import android.util.Log;

public class NumberFormatter {

    /**
     * @return Firebase friendly long from String
     */
    public static long getLongFromString(String s){
        if(s.equals(""))
            return 0;
        else return Long.valueOf(s.replaceAll( "[^\\d]", ""));
    }
}
