package io.lucalabs.expenses.utils;

import java.lang.reflect.Field;

import io.lucalabs.expenses.models.annotations.Arg;
import okhttp3.FormBody;

public class RESTBuilder {

    public static void addToForm(FormBody.Builder builder, String paramName, String paramValue) {
        if (paramValue != null)
            builder.add(paramName, paramValue);
    }
}
