package com.autocounting.autocounting.network.upload;

import android.content.Context;

public interface UploadResponseHandler {
    void onFileUploadStarted(String filenme);
    void onFileUploadFinished(String result);
    void onFileUploadFailed();
    Context getContext();
}
