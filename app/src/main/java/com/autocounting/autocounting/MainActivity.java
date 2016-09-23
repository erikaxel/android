package com.autocounting.autocounting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.webkit.WebView;
import android.widget.Toast;

import com.autocounting.autocounting.models.User;
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

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements TurbolinksAdapter, UploadResponseHandler {
    // Change the BASE_URL to an address that your VM or device can hit.
    private static final String BASE_URL = "https://beta.autocounting.no/";
    //    private static final String BASE_URL = "http://192.168.1.107:3000/";
    //    private static final String BASE_URL = "http://10.10.10.180:3000/";
    private static final String INTENT_URL = "intentUrl";

    private String location;
    private TurbolinksView turbolinksView;
    private CameraFab fab;
    private CoordinatorLayout coordinatorLayout;
    private Uri lastReceiptUri;

    private static final String TAG = "MainActivity";

    // -----------------------------------------------------------------------
    // Activity overrides
    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        turbolinksView = (TurbolinksView) findViewById(R.id.turbolinks_view);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.fab_coordinator);
        fab = (CameraFab) findViewById(R.id.camera_button);
        fab.setup(this);


        // For this demo app, we force debug logging on. You will only want to do
        // this for debug builds of your app (it is off by default)
        TurbolinksSession.getDefault(this).setDebugLoggingEnabled(true);

        // For this example we set a default location, unless one is passed in through an intent
        location = SimpleUrlBuilder.buildUrl(BASE_URL, "/receipts", "token=", User.getCurrentUser(this).getSavedToken());

        // Execute the visit
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .view(turbolinksView)
                .visit(location);
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
                                FirebaseAuth.getInstance().signOut();
                                finish();
                                startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                            }
                        });
                break;
            default:
                Toast.makeText(this, "None of the above!", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPingFromServer() {
        Log.d(TAG, "onPingFromServer");

        final String newUrl = BASE_URL + "receipts";
        Log.d(TAG, "Visiting " + newUrl);

        final Activity act = this;
//       Need to update URL on UI-thread, or else it wont work.
        runOnUiThread(new Runnable() {
            public void run() {
                WebView webView = TurbolinksSession.getDefault(act).getWebView();
                String oldUrl = webView.getUrl();
                Log.d(TAG, "Old url" + oldUrl);
                // Only do refresh if we are already at the receipts page
                if (oldUrl.equals(newUrl)) {
                    TurbolinksSession.getDefault(act).visit(newUrl);
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
                    intent.putExtra(INTENT_URL, location);
                    startActivity(intent);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            startFileUpload(data.getData());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startFileUpload(Uri filepath) {
        lastReceiptUri = filepath;
        Bitmap bitmap = ImageHandler.getBitmapFromUri(this, lastReceiptUri);
        new UploadImageTask(this).execute(bitmap);
    }

    // -----------------------------------------------------------------------
    // UploadResponseHandler actions
    // -----------------------------------------------------------------------

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

    // Simply forwards to an error page, but you could alternatively show your own native screen
    // or do whatever other kind of error handling you want.
    private void handleError(int code) {
        if (code == 404) {
            TurbolinksSession.getDefault(this)
                    .activity(this)
                    .adapter(this)
                    .restoreWithCachedSnapshot(false)
                    .view(turbolinksView)
                    .visit(BASE_URL + "/error");
        } else {
            Snackbar
                    .make(coordinatorLayout, "Error loading receipts", Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}