package io.lucalabs.expenses.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.lucalabs.expenses.network.Routes;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Handles fetching and storing of cost categories
 */
public class CostCategory {
    private int id;
    private String name;
    private double rate;
    private String local_name;

    private static final String TAG = CostCategory.class.getSimpleName();

    /**
     * Fetch cost categories from Rails Server
     */
    public static boolean fetchData(final Activity activity) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Routes.costCategoriesUrl(activity))
                .addHeader("Authorization", "Bearer " + User.getServerToken(activity).getToken())
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.i(TAG, "fetching cost categories. Code: " + response.code() + ", message: " + response.message());
            if (response.isSuccessful()) {
                saveData(activity, response.body().string());
                return true;
            } else {
                Log.w(TAG, "Couldn't fetch cost categories. Response: " + response.code() + ", message: " + response.message());
                Log.w(TAG, "Token expires at: " + User.getServerToken(activity).getExpires_at());
                return false;
            }
        } catch (IOException e) {
            Log.i(TAG, "IOException occurred while trying to fetch cost categories");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves cost categories as JSON to SharedPreferences
     */
    private static void saveData(Context context, String responseString) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("costCategories", responseString);
        editor.apply();
    }

    /**
     * Deletes saved cost categories from SharedPreferences
     */
    public static void clearData(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("costCategories", null);
        editor.apply();
    }

    public static List<CostCategory> getAll(Context context) {
        String costCategoryData = PreferenceManager.getDefaultSharedPreferences(context).getString("costCategories", "");
        List<CostCategory> costCategories = new ArrayList<>();

        try {
            Gson gson = new Gson();
            JSONArray jsonArray = new JSONObject(costCategoryData).getJSONArray("data");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                CostCategory costCategory = gson.fromJson(jsonObject.getString("attributes"), CostCategory.class);
                costCategory.setId(jsonObject.getInt("id"));
                costCategories.add(costCategory);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return costCategories;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getLocal_name() {
        return local_name;
    }

    public void setLocal_name(String local_name) {
        this.local_name = local_name;
    }
}
