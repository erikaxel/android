package io.lucalabs.expenses.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import io.lucalabs.expenses.models.Receipt;

import java.lang.ref.WeakReference;

/**
 * Asynchronously fetches a Bitmap from a receipt thumbnail, and loads it an ImageView
 * Based on code from https://developer.android.com/training/displaying-bitmaps/process-bitmap.html
 */

public class ImageFetcher extends AsyncTask<Receipt, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private Receipt receipt;

    public ImageFetcher(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    // Get image in background.
    @Override
    protected Bitmap doInBackground(Receipt... params) {
        receipt = params[0];
        return receipt.getThumbnail();
    }

    // Once complete, check if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}