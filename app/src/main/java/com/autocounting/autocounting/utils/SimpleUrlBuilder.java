package com.autocounting.autocounting.utils;

public class SimpleUrlBuilder {

    /*
     * Accepts a base string, a path beginning with '/' and n args in the format 'argName=', 'argValue'
     * For now, add '&' in front of arguments n > 1
     */
    public static String buildUrl(String base, String path, String... args) {
        StringBuilder urlBuilder = new StringBuilder(base).append(path);

        if (args != null) {
            urlBuilder.append("?");
            for (int i = 0; i < args.length; i++) {
                urlBuilder.append(args[i]);
            }
        }
        return urlBuilder.toString();
    }
}
