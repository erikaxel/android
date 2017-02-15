package io.lucalabs.expenses.network.webapi;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import android.util.Log;

import io.lucalabs.expenses.models.ApiRequestObject;
import io.lucalabs.expenses.models.User;
import io.lucalabs.expenses.utils.RESTBuilder;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiRequestTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = ApiRequestTask.class.getSimpleName();

    private Context mContext;
    private ApiRequestObject mApiRequestObject;
    private String mUrl;
    private String mRequestMethod;

    public ApiRequestTask(Context context, String requestMethod, ApiRequestObject apiRequestObject, String url) {
        mContext = context;
        mApiRequestObject = apiRequestObject;
        mRequestMethod = requestMethod;
        mUrl = url;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        FormBody form = buildForm();
        makeRequest(form);
        return null;
    }

    @Nullable
    private void makeRequest(FormBody form) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(mUrl)
                .method(mRequestMethod, form)
                .build();

        try {
            Log.i(TAG, "Trying");
            Response response = client.newCall(request).execute();
            response.close();
            Log.i(TAG, "Apparently succeeding");
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @NonNull
    private FormBody buildForm() {
        FormBody.Builder formBuilder = new FormBody.Builder();

        for (String[] argPair : mApiRequestObject.getParams())
            RESTBuilder.addToForm(formBuilder, argPair[0], argPair[1]);

        RESTBuilder.addToForm(formBuilder, "token", User.getToken(mContext));
        return formBuilder.build();
    }
}