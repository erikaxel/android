package com.autocounting.autocounting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.autocounting.autocounting.models.User;
import com.autocounting.autocounting.network.RouteManager;
import com.autocounting.autocounting.network.upload.ReceiptEvent;
import com.autocounting.autocounting.network.upload.UploadImageTask;
import com.autocounting.autocounting.network.upload.UploadResponseHandler;
import com.autocounting.autocounting.utils.ImageHandler;
import com.autocounting.autocounting.utils.SimpleUrlBuilder;
import com.autocounting.autocounting.views.widgets.CameraFab;
import com.basecamp.turbolinks.TurbolinksAdapter;
import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksView;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements TurbolinksAdapter, UploadResponseHandler {

    private String location;
    private TurbolinksView turbolinksView;
    private CameraFab fab;
    private CoordinatorLayout coordinatorLayout;
    private Uri lastReceiptUri;
    private FirebaseAuth auth;
    private RouteManager routeManager;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_READ_WRITE = 22;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        turbolinksView = (TurbolinksView) findViewById(R.id.turbolinks_view);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.fab_coordinator);
        fab = (CameraFab) findViewById(R.id.camera_button);
        fab.setup(this);

        routeManager = new RouteManager(this);
        refreshAuth();

        TurbolinksSession.getDefault(this).setDebugLoggingEnabled(true);
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
                Toast.makeText(this, "An unknown option was selected (or you forgot to add break;)", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPingFromServer() {
        runOnUiThread(new Runnable() {
            public void run() {
                WebView webView = TurbolinksSession.getDefault(MainActivity.this).getWebView();
                String oldUrl = webView.getUrl();
                Log.d(TAG, "Old url" + oldUrl);
                // Only do refresh if we are already at the receipts page
                if (oldUrl.equals(routeManager.receiptsUrl())) {
                    TurbolinksSession.getDefault(MainActivity.this).visit(routeManager.receiptsUrl());
                }
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // Since the webView is shared between activities, we need to tell Turbolinks
        // to load the location from the previous activity upon restarting
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .restoreWithCachedSnapshot(true)
                .view(turbolinksView)
                .visit(location);
    }

    // -----------------------------------------------------------------------
    // TurbolinksAdapter interface
    // -----------------------------------------------------------------------

    @Override
    public void onPageFinished() {
    }

    @Override
    public void onReceivedError(int errorCode) {
        handleError(errorCode);
    }

    @Override
    public void pageInvalidated() {

    }

    @Override
    public void requestFailedWithStatusCode(int statusCode) {
        handleError(statusCode);
    }

    @Override
    public void visitCompleted() {
    }

    // The starting point for any href clicked inside a Turbolinks enabled site. In a simple case
    // you can just open another activity, or in more complex cases, this would be a good spot for
    // routing logic to take you to the right place within your app.
    @Override
    public void visitProposedToLocationWithAction(String location, String action) {
        try {
            URL url = new URL(location);
            Log.d("visitProposedToLocation", "location:" + location + " action:" + action + " path:" + url.getPath());

            switch (url.getPath()) {
                case "/take_picture":
                    break;
                case "/scan_receipt":
                    break;
                default:
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("intentUrl", location);
                    startActivity(intent);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            startFileUpload(data.getData());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startFileUpload(Uri filepath) {
        lastReceiptUri = filepath;
        Bitmap bitmap = null;
        try {
            bitmap = ImageHandler.correctRotation(ImageHandler.getBitmapFromUri(this, lastReceiptUri));
        } catch (IOException e) {
            // Uploaded unrotated image
            new UploadImageTask(this).execute(ImageHandler.getBitmapFromUri(this, lastReceiptUri));
            e.printStackTrace();
        }
        new UploadImageTask(this).execute(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fab.takePicture();
                } else {
                    Toast.makeText(this, "This app depends on read/write permission to function", Toast.LENGTH_LONG).show();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might need
        }
    }

    // -----------------------------------------------------------------------
    // UploadResponseHandler actions
    // -----------------------------------------------------------------------

    @Override
    public void onFileUploadStarted(String filename) {
        new ReceiptEvent(this, filename).receiptAdded();
    }

    @Override
    public void onFileUploadFinished(String result) {
        System.out.println("Finished");
        System.out.println(result);
    }

    @Override
    public void onFileUploadFailed() {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Couldn't upload photo", Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startFileUpload(lastReceiptUri);
                    }
                });

        snackbar.show();
    }

    @Override
    public Context getContext() {
        return this;
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    // Should probably not do a full refresh every time.
    private void refreshAuth() {
        auth = FirebaseAuth.getInstance();
        auth.getCurrentUser().getToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                new User(MainActivity.this, task.getResult().getToken(), auth.getCurrentUser().getUid()).save();
                Log.i("VISIT", "Visiting ...");
                visitPage();
            }
        });
    }

    private void visitPage() {
        location = getIntent().getStringExtra("intentUrl");

        if(location == null)
            location = SimpleUrlBuilder.buildUrl(routeManager.baseUrl(), "/receipts", "token=", User.getCurrentUser(this).getSavedToken());

        // Execute the visit
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .view(turbolinksView)
                .visit(location);
    }

    private void handleError(int code) {
        if (code == 404) {
            TurbolinksSession.getDefault(this)
                    .activity(this)
                    .adapter(this)
                    .restoreWithCachedSnapshot(false)
                    .view(turbolinksView)
                    .visit(routeManager.errorUrl());
        } else {
            Log.w("NETWORK", "Failed to reach server");
            Snackbar
                    .make(coordinatorLayout, "Failed to reach server", Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}