package com.autocounting.autocounting.utils;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.autocounting.autocounting.models.Receipt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ImageSaver implements Runnable {

    private static final String TAG = "ImageSaver";
    private final Context context;
    private final Image image;
    private final File imageFile;

    public ImageSaver(Context context, Image image, File imageFile) {
        this.context = context;
        this.image = image;
        this.imageFile = imageFile;
    }

    @Override
    public void run() {
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        Receipt rec = new Receipt(bytes, context);
        rec.save();
        Log.i(TAG, "Receipt saved " + Receipt.count(Receipt.class));

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(imageFile);
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("save_to_album_pref", false))
                try {
                    saveCopyToAlbum(imageFile, new Receipt().makeFile(Environment.getExternalStorageDirectory()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            image.close();

            if (fileOutputStream != null)
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void saveCopyToAlbum(File source, File destination) throws IOException {
        Log.i(TAG, "Saving copy ...");
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(destination.getAbsolutePath())));
        context.sendBroadcast(mediaStoreUpdateIntent);
    }
}
