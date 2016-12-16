package io.lucalabs.expenses.network.upload;

import android.content.Context;

public interface UploadResponseHandler {
    void onFileUploadStarted(String filenme);
    void onFileUploadFinished(String result);
    void onFileUploadFailed();
    Context getContext();
}
