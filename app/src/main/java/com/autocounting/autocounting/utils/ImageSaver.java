package com.autocounting.autocounting.utils;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.autocounting.autocounting.CameraActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageSaver implements Runnable {

    private static final String TAG = "ImageSaver";
    private final CameraActivity contextActivity;
    private final Image image;
    private final File imageFile;

    public ImageSaver(CameraActivity contextActivity, Image image, File imageFile) {
        this.contextActivity = contextActivity;
        this.image = image;
        this.imageFile = imageFile;
    }

    @Override
    public void run() {
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(imageFile);
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if(PreferenceManager.getDefaultSharedPreferences(contextActivity).getBoolean("save_to_album_pref", false))
                try {
                    saveCopyToAlbum(imageFile, createImageFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            image.close();

            contextActivity.onImageSaved();

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
        contextActivity.sendBroadcast(mediaStoreUpdateIntent);
    }

    // Duplicate
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File image = File.createTempFile(imageFileName, ".jpg", Environment.getExternalStorageDirectory());
        return image;
    }
}