package com.autocounting.autocounting.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageHandler {

    private final static int THUMBNAIL_SIZE = 25;

    public static Bitmap makeThumbnail(Bitmap original) {
        return scaleDown(original, THUMBNAIL_SIZE, true);
    }

    public static byte[] makeByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap getBitmapFromUri(Activity activity, Uri imageUri) {
        try {
            return MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap scaleDown(Bitmap original, float maxImageSize, Boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / original.getWidth(),
                (float) maxImageSize / original.getHeight());
        int width = Math.round((float) ratio * original.getWidth());
        int height = Math.round((float) ratio * original.getHeight());

        return Bitmap.createScaledBitmap(original, width,
                height, filter);
    }
}
