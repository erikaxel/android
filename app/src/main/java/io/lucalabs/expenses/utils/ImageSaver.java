package io.lucalabs.expenses.utils;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import io.lucalabs.expenses.models.Receipt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageSaver implements Runnable {

    private static final String TAG = "ImageSaver";
    private final Context mContext;
    private final Image mImage;
    private final String mExpenseReportRef;

    public ImageSaver(Context context, Image image, String expenseReportRef) {
        mContext = context;
        mImage = image;
        mExpenseReportRef = expenseReportRef;
    }

    @Override
    public void run() {
        ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        Receipt rec = new Receipt(bytes, mContext, mExpenseReportRef);

        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("save_to_album_pref", false)) {
            try {saveCopyToAlbum(bytes);
            } catch (IOException e) {
                Log.w(TAG, "Couldn't save copy");
                e.printStackTrace();
            }
        }
        mImage.close();
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
