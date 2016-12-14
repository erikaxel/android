package com.autocounting.autocounting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.widget.ListView;
import android.widget.Toast;

import com.autocounting.autocounting.R;
import com.autocounting.autocounting.activities.firebase.FirebaseActivity;
import com.autocounting.autocounting.managers.EnvironmentManager;
import com.autocounting.autocounting.models.User;
import com.autocounting.autocounting.network.NetworkStatus;
import com.autocounting.autocounting.network.database.ReceiptDatabase;
import com.autocounting.autocounting.network.upload.UploadService;
import com.autocounting.autocounting.views.adapters.ReceiptListAdapter;
import com.autocounting.autocounting.views.widgets.CameraFab;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class MainActivity extends FirebaseActivity {

    private static final String TAG = "MainActivity";
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseReference ref = ReceiptDatabase.getUserReference(
                User.getCurrentUser(),
                EnvironmentManager.currentEnvironment(this));

        setContentView(R.layout.activity_offline);
        ((ListView) findViewById(R.id.offline_list)).setAdapter(new ReceiptListAdapter(this, ref));
        ((CameraFab) findViewById(R.id.camera_button)).setup(this);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.offline_coordinator);

        displayErrorIfNecessary(getIntent().getIntExtra("networkStatus", NetworkStatus.OK));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, UploadService.class));
    }

    private void displayErrorIfNecessary(int networkStatus) {
        switch (networkStatus) {
            case NetworkStatus.INTERNET_UNAVAILABLE:
                Snackbar.make(coordinatorLayout, "Network unavailable", Snackbar.LENGTH_LONG).show();
                break;
            case NetworkStatus.SERVER_ERROR:
                Snackbar.make(coordinatorLayout, "An error occurred on server.", Snackbar.LENGTH_LONG).show();
                break;
        }
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
                                User.clearSavedData(MainActivity.this);
                                // Deprecated, but available for API 19
                                CookieManager.getInstance().removeAllCookie();
                                FirebaseAuth.getInstance().signOut();
                                finish();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            }
                        });
                break;
            case R.id.settings_option:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            default:
                Toast.makeText(this, "An unknown option was selected", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
