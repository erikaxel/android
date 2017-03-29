package io.lucalabs.expenses.network.webapi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.lucalabs.expenses.models.Task;
import io.lucalabs.expenses.models.User;
import io.lucalabs.expenses.models.annotations.Arg;
import io.lucalabs.expenses.network.Routes;
import io.lucalabs.expenses.utils.RESTBuilder;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiRequest {
    private Context mContext;
    private Task mTask;
    private List<String[]> mParams;

    private static final String TAG = ApiRequest.class.getSimpleName();

    public ApiRequest(Context context, Task task) {
        mContext = context;
        mTask = task;
    }

    public boolean start() {
        if (!mTask.getRequestMethod().equals("DELETE")) {

            // Complete tasks that are trying to access deleted objects
            if (mTask.getObject() == null)
                return true;

            try {
                buildParams(mTask.getObject());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        FormBody form = buildForm();
        return makeRequest(form);
    }

    @Nullable
    private boolean makeRequest(FormBody form) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Routes.getFullPath(mContext, mTask.getUrl()))
                .method(mTask.getRequestMethod(), form)
                .build();

        for (int i = 0; i < form.size(); i++)
            Log.i(TAG, "name: " + form.name(i) + ", value " + form.value(i));

        try {
            Response response = client.newCall(request).execute();
            response.close();
            Log.i(TAG, "code: " + response.code());
            if (response.isSuccessful()) {
                return true;
            } else {
                Log.w(TAG, "Did not succeed. Response: " + response.code());
                return true; // Unsuccessful tasks are also removed from queue
            }
        } catch (IOException e) {
            Log.i(TAG, "Couldn't send request (" + e.getMessage() + ")");
            e.printStackTrace();
            return false;
        }
    }

    @NonNull
    private FormBody buildForm() {
        FormBody.Builder formBuilder = new FormBody.Builder();

        if (mParams != null)
            for (String[] argPair : mParams)
                RESTBuilder.addToForm(formBuilder, argPair[0], argPair[1]);

        RESTBuilder.addToForm(formBuilder, "token", User.getFirebaseToken(mContext));
        return formBuilder.build();
    }

    private void buildParams(Object object) throws IllegalArgumentException, IllegalAccessException {
        mParams = new ArrayList<>();

        for (Field f : object.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Arg.class)) {
                f.setAccessible(true);
                Object value = f.get(object);
                if (value instanceof Long) {
                    // We convert long to double by default. Perhaps this should be done somewhere else?
                    Long l = (long) value;
                    String text = "" + l.doubleValue() / 100;
                    mParams.add(new String[]{f.getAnnotation(Arg.class).name(), text});
                } else if (value != null)
                    mParams.add(new String[]{f.getAnnotation(Arg.class).name(), value.toString()});
            }
        }
    }
}