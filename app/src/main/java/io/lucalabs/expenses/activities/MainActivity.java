package io.lucalabs.expenses.activities;

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

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.firebase.FirebaseActivity;
import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.models.User;
import io.lucalabs.expenses.network.NetworkStatus;
import io.lucalabs.expenses.network.database.ReceiptDatabase;
import io.lucalabs.expenses.network.upload.UploadService;
import io.lucalabs.expenses.views.adapters.ReceiptListAdapter;
import io.lucalabs.expenses.views.widgets.CameraFab;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FirebaseActivity {

    private static final String TAG = "MainActivity";
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.w("Receipt", "---------------------------------");
        Log.w("Receipt", Receipt.count(Receipt.class) + " number of receipts in DB");
        Log.w("Receipt", Receipt.find(Receipt.class, "status = 'PENDING' OR status = 'UPLOADED'").size() + " receipts waiting for upload");

        List<Receipt> receiptList = Receipt.listAll(Receipt.class);
        for(Receipt rec : receiptList)
            Log.w("Receipt", rec.getFirebase_ref() + " is " + rec.getStatus());
        Log.w("Receipt", "---------------------------------");
        Query ref = ReceiptDatabase.getUserReference(
                User.getCurrentUser(),
                EnvironmentManager.currentEnvironment(this))
                .orderByChild("expense_report_id")
                .equalTo(null);

        setContentView(R.layout.activity_main);
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
