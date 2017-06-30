package io.lucalabs.expenses.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.lucalabs.expenses.models.Receipt;

public class ImageSaver implements Runnable {

    private static final String TAG = "ImageSaver";
    private final Context mContext;
    private final byte[] mImage;
    private final String mExpenseReportRef;
    private boolean mSaveToAlbumOnly = false;

    public ImageSaver(Context context, byte[] image, String expenseReportRef) {
        mContext = context;
        mImage = image;
        mExpenseReportRef = expenseReportRef;
    }

    public ImageSaver(Context context, byte[] image, String expenseReportRef, boolean saveToAlbumOnly) {
        this(context, image, expenseReportRef);
        mSaveToAlbumOnly = saveToAlbumOnly;
    }

    @Override
    public void run() {
        if (!mSaveToAlbumOnly)
            new Receipt(mImage, mContext, mExpenseReportRef);

        boolean saveToAlbum = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("save_to_album_pref", false) || mSaveToAlbumOnly;

        if (saveToAlbum) {
            try {
                saveCopyToAlbum(mImage);
            } catch (IOException e) {
                Log.w(TAG, "Couldn't save copy");
                e.printStackTrace();
            }
        }
    }

    private void saveCopyToAlbum(byte[] bytes) throws IOException {
        File imageFile = new Receipt().makeFile(Environment.getExternalStorageDirectory());

        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
        fileOutputStream.write(bytes);
        fileOutputStream.close();

        Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(imageFile.getAbsolutePath())));
        mContext.sendBroadcast(mediaStoreUpdateIntent);
    }
}
