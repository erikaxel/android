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
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.autocounting.autocounting.models.User;
import com.autocounting.autocounting.network.NetworkManager;
import com.autocounting.autocounting.views.widgets.CameraFab;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

public class OfflineActivity extends AppCompatActivity {

    private static final String TAG = "OfflineActivity";
    private CoordinatorLayout coordinatorLayout;
    private TextView receiptsStatusNotifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        Button goOnlineButton = (Button) findViewById(R.id.go_online_button);
        goOnlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OfflineActivity.this, MainActivity.class));
            }
        });

        CameraFab fab = (CameraFab) findViewById(R.id.camera_button);
        fab.setup(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.offline_coordinator);

        displayErrorIfNecessary(getIntent().getIntExtra("networkStatus", NetworkManager.OK));
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
        Log.i(TAG, "OnResume");
        receiptsStatusNotifier = (TextView) findViewById(R.id.receipts_status_text);
        setReceiptsStatus(receiptsStatusNotifier);
    }

    private void setReceiptsStatus(TextView receiptsStatusNotifier) {
        Log.i(TAG, "SetReceiptStatus");
        int numberOfReceipts = getQueueFolder().list().length;
        Log.i(TAG, "receipts waiting to be uploaded: " + numberOfReceipts);
        if (numberOfReceipts > 0)
            if(numberOfReceipts == 1)
                receiptsStatusNotifier.setText(getString(R.string.receipts_status__is_one_text));
            else
                receiptsStatusNotifier.setText(getString(R.string.receipts_status_text, numberOfReceipts));
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
                Toast.makeText(this, "An unknown option was selected (or you forgot to add break;)", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private File getQueueFolder() {
        Log.i(TAG, "GetQueueFolder");
        File imageFolder = new File(Environment.getExternalStorageDirectory(), "receipt_queue");

        if (!imageFolder.exists())
            imageFolder.mkdirs();

        return imageFolder;
    }
}
