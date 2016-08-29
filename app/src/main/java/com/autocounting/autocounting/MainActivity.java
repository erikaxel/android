package com.autocounting.autocounting;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksAdapter;
import com.basecamp.turbolinks.TurbolinksView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.Message;

public class MainActivity extends AppCompatActivity implements TurbolinksAdapter {
    // Change the BASE_URL to an address that your VM or device can hit.
    private static final String BASE_URL = "https://beta.autocounting.no/";
//    private static final String BASE_URL = "http://192.168.1.107:3000/";
    //    private static final String BASE_URL = "http://10.10.10.180:3000/";
    private static final String INTENT_URL = "intentUrl";

    private static AblyRealtime ably;

    private String location;
    private TurbolinksView turbolinksView;

    private static final String TAG = "MainActivity";

    // -----------------------------------------------------------------------
    // Activity overrides
    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the custom TurbolinksView object in your layout
        turbolinksView = (TurbolinksView) findViewById(R.id.turbolinks_view);

        // For this demo app, we force debug logging on. You will only want to do
        // this for debug builds of your app (it is off by default)
        TurbolinksSession.getDefault(this).setDebugLoggingEnabled(true);

        // For this example we set a default location, unless one is passed in through an intent
        location = getIntent().getStringExtra(INTENT_URL) != null ? getIntent().getStringExtra(INTENT_URL) : BASE_URL;

        if (ably == null) {
            try {
                Log.d(TAG, "Starting Ably");

                //    TODO Probably unsafe to store the API key here in code, but will be ok for demo.
                ably = new AblyRealtime("_CstnA.02JI4Q:Z8FOnr7bIpnrBxi_");

                Channel channel = ably.channels.get("autocounting");
                try {
                    channel.subscribe(new Channel.MessageListener() {
                        @Override
                        public void onMessage(Message messages) {
                            Log.d(TAG, "ably.onMessage: " + messages.toString());
                            onPingFromServer();
                        }
                    });
                } catch (AblyException e) {
                    e.printStackTrace();
                }
            } catch (AblyException e) {
                Log.e(TAG, "Couldn't start Ably");
                e.printStackTrace();
            }
        } {
            Log.d(TAG, "Ably not null");
        }

        // Execute the visit
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .view(turbolinksView)
                .visit(location);
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
                if(oldUrl.equals(newUrl)) {
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

    //    TODO: Need to get rid of all statics
    private static boolean auto_ocr = false;

    // The starting point for any href clicked inside a Turbolinks enabled site. In a simple case
    // you can just open another activity, or in more complex cases, this would be a good spot for
    // routing logic to take you to the right place within your app.
    @Override
    public void visitProposedToLocationWithAction(String location, String action) {
        try {
            URL url = new URL(location);
            Log.d("visitProposedToLocation", "location:" + location + " action:" + action + " path:" + url.getPath());

            if (url.getPath().equals("/take_picture")) {
                auto_ocr = false;
                dispatchTakePictureIntent();
            } else if (url.getPath().equals("/scan_receipt")) {
                auto_ocr = true;
                dispatchTakePictureIntent();
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(INTENT_URL, location);
                this.startActivity(intent);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    static String mCurrentPhotoPath;
    static Uri photoURI;

    private File createImageFile() throws IOException {
        // Create an image file name
        Log.d(TAG, "createImageFile()");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("createImageFile", storageDir.getAbsolutePath());
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.d(TAG, "mCurrentPhotoPath saved: " + mCurrentPhotoPath);
        Log.d(TAG, "this: " + this);
        return image;
    }

//    static final int REQUEST_TAKE_PHOTO = 1;
//
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode==REQUEST_TAKE_PHOTO)
//        {
//            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
//            try {
//                File destination = createImageFile();
//                FileOutputStream fo;
//                fo = new FileOutputStream(destination);
//                fo.write(bytes.toByteArray());
//                fo.close();
//                new uploadFileToServerTask().execute(destination.getAbsolutePath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }


    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Log.d(TAG, "dispatchTakePictureIntent()");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.autocounting.fileprovider",
                        photoFile);
                Log.d("dispatchTakePicture", photoURI.getPath());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
//            Uri photoURI = FileProvider.getUriForFile(this,
//                    "com.autocounting.fileprovider",
//                    f);
//            photoURI = FileProvider.getUriForFile(this,
//                    "com.autocounting.fileprovider",
//                    photoFile);
//            Uri photoURI2 = (Uri)data.getExtras().get(MediaStore.EXTRA_OUTPUT);

//            Log.d("onActivityResult", photoURI2.getPath());
//            Log.d(TAG, "onActivityResult mCurrentPhotoPath: "  + mCurrentPhotoPath);
//            Log.d(TAG, "this: " + this);
//            File f = new File(mCurrentPhotoPath);
//            Log.d(TAG, "onActivityResult file exists?: "  + f.exists());
//            Uri photoURI2 = FileProvider.getUriForFile(this,
//                    "com.autocounting.fileprovider",
//                    f);
//            Log.d("onActivityResult", photoURI2.getPath());
            new uploadFileToServerTask().execute(photoURI);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void afterFileUpload(String newId) {
        if (!newId.equals("-1")) {
            TurbolinksSession.getDefault(this).visit(BASE_URL + "/receipts/" + newId);
        }
    }

    private class uploadFileToServerTask extends AsyncTask<Uri, String, String> {
        @Override
        protected String doInBackground(Uri... args) {
            try {
                MultipartHelper multipart = new MultipartHelper(BASE_URL + "receipts.json", "UTF-8");

                if (auto_ocr) {
                    multipart.addFormField("use_ocr", "1");
                }

                InputStream fileInputStream = getContentResolver().openInputStream(args[0]);
                multipart.addFilePart("receipt[picture]", fileInputStream, args[0].getPath());

                List<String> response = multipart.finish();
                JSONArray arr = new JSONArray(response.toString());
                int id = arr.getJSONObject(0).getInt("id");
                Log.d(TAG, "Found id " + id);

//                Log.v("rht", "SERVER REPLIED:");
//                for (String line : response) {
//                    Log.v("rht", "Line : " + line);
//                }
                return "" + id;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "-1";
        }

        @Override
        protected void onPostExecute(String result) {
            afterFileUpload(result);
        }

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
        }
    }
}