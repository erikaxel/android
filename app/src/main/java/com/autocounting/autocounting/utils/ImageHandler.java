package com.autocounting.autocounting.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageHandler {

    private final static int THUMBNAIL_SIZE = 100;
    private final static int MEDIUM_SIZE = 300;
    private final static int MAX_SIZE = 1000; // Not used

    public final static int JPEG_COMPRESSION_RATE = 80;

    public static Bitmap makeThumbnail(Bitmap original) {
        return scaleDown(original, THUMBNAIL_SIZE, true);
    }

    public static Bitmap makeMedium(Bitmap original) {
        return scaleDown(original, MEDIUM_SIZE, true);
    }

    public static Bitmap correctRotation(Bitmap image) throws IOException {
        if(image.getWidth() > image.getHeight())
            return rotateImage(image, 90);
        else
            return image;
    }

    public static Bitmap scaleOriginal(Bitmap original){
        return scaleDown(original, MAX_SIZE, true);
    }

    public static byte[] makeByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap getBitmapFromFile(Context context, File file) {
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(),
                    Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
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
