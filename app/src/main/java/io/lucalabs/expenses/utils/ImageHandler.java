package io.lucalabs.expenses.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.IOException;

/**
 * Utility class used to scale, compress and manipulate images.
 */
public class ImageHandler {

    private final static int THUMBNAIL_SIZE = 100;
    public final static int JPEG_COMPRESSION_RATE = 80;

    /**
     * Scales an image to thumbnail size
     */
    public static Bitmap makeThumbnail(Bitmap original) {
        return scaleDown(original, THUMBNAIL_SIZE, true);
    }

    /**
     * Rotates landscape images 90 degrees.
     */
    public static Bitmap correctRotation(Bitmap image) throws IOException {
        if(image.getWidth() > image.getHeight())
            return rotateImage(image, 90);
        else
            return image;
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
