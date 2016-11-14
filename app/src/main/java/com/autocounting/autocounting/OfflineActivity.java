package com.autocounting.autocounting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.autocounting.autocounting.models.Receipt;
import com.autocounting.autocounting.models.User;
import com.autocounting.autocounting.network.NetworkManager;
import com.autocounting.autocounting.views.adapters.ReceiptListAdapter;
import com.autocounting.autocounting.views.widgets.CameraFab;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

public class OfflineActivity extends AppCompatActivity {

    private static final String TAG = "OfflineActivity";
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        CameraFab fab = (CameraFab) findViewById(R.id.camera_button);
        fab.setup(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.offline_coordinator);
        displayErrorIfNecessary(getIntent().getIntExtra("networkStatus", NetworkManager.OK));

        Log.i(TAG, "Creating list");

        boolean imageWasAdded = getIntent().getBooleanExtra("imageWasAdded", true);
        Log.i(TAG, imageWasAdded ? "true" : "false");
        ListView listView = (ListView) findViewById(R.id.offline_list);
        ReceiptListAdapter receiptListAdapter = new ReceiptListAdapter(this, Receipt.getAll(this), imageWasAdded);
        listView.setAdapter(receiptListAdapter);
    }

    private void displayErrorIfNecessary(int networkStatus) {
        switch (networkStatus) {
            case NetworkManager.INTERNET_UNAVAILABLE:
                Snackbar.make(coordinatorLayout, "Network unavailable", Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_option:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                User.clearSavedData(OfflineActivity.this);
                                // Deprecated, but available for API 19
                                CookieManager.getInstance().removeAllCookie();
                                FirebaseAuth.getInstance().signOut();
                                finish();
                                startActivity(new Intent(OfflineActivity.this, LoginActivity.class));
                            }
                        });
                break;
            case R.id.settings_option:
                startActivity(new Intent(OfflineActivity.this, SettingsActivity.class));
                break;
            default:
                Toast.makeText(this, "An unknown option was selected", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
