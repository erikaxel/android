package io.lucalabs.expenses.network.webapi;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import io.lucalabs.expenses.models.CostCategory;
import io.lucalabs.expenses.models.Device;
import io.lucalabs.expenses.models.ServerToken;
import io.lucalabs.expenses.models.User;
import io.lucalabs.expenses.network.Routes;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BackendServer {
    private static final String TAG = BackendServer.class.getSimpleName();

    /**
     * Receives and sets a ServerToken, based on the User's FirebaseToken.
     * After running this, you are ready to run exchangeData()
     */
    public static void exchangeTokens(final Activity activity) {
        Log.i(TAG, "Connecting to backend");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Routes.userTokenUrl(activity) + "?token=" + User.getFirebaseToken(activity))
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                Gson gson = new Gson();
                ServerToken token = gson.fromJson(response.body().string(), ServerToken.class);
                User.setServerToken(activity, token);
            } else {
                Log.w(TAG, "failed to fetch token. Code: " + response.code() + ", message: " + response.message());
            }
        } catch (IOException e) {
            Log.w(TAG, "IOException occured while trying to fetch token");
            e.printStackTrace();
        }
    }

    /**
     * Send and receive relevant information to/from server
     * Depends on a valid token from exchangeTokens()
     */
    public static void exchangeData(final Activity activity) {
        Device.register(activity);
        CostCategory.fetchData(activity);
    }
}
