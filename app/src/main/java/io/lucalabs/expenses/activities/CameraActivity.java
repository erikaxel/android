/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.lucalabs.expenses.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.views.fragments.CameraFragment;
import io.lucalabs.expenses.views.fragments.PhotoPreviewFragment;

public class CameraActivity extends Activity {

    private boolean mShowingPreview;
    private String mExpenseReportRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mExpenseReportRef = getIntent().getStringExtra("expense_report_ref");

        setToFullscreen();
        if (savedInstanceState == null) {

            showCamera();
        }
    }

    private void showCamera() {
        mShowingPreview = false;
        getFragmentManager().beginTransaction()
                .replace(R.id.container, CameraFragment.newInstance())
                .commit();
    }

    public void showPreview(byte[] previewImage) {
        mShowingPreview = true;
        PhotoPreviewFragment photoPreviewFragment = PhotoPreviewFragment.newInstance();
        photoPreviewFragment.setPreviewImage(previewImage);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, photoPreviewFragment)
                .commit();
    }

    /**
     * Sets activity to fullscreen/immersive mode
     */
    private void setToFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public String getExpenseReportRef(){
        return mExpenseReportRef;
    }

    @Override
    public void onBackPressed() {
        if (mShowingPreview)
            showCamera();
        else super.onBackPressed();
    }
}