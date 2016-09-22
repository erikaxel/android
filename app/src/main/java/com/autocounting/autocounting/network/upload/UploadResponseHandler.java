package com.autocounting.autocounting.network.upload;

import android.content.Context;

public interface UploadResponseHandler {
    void onFileUploadFinished(String result);
    void onFileUploadFailed();
    Context getContext();
}
