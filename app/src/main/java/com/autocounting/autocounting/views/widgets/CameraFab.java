package com.autocounting.autocounting.views.widgets;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.View;

import com.autocounting.autocounting.activities.CameraActivity;
import com.autocounting.autocounting.managers.PermissionManager;

/*
 * The camera button seen in MainActivity.
 */
public class CameraFab extends FloatingActionButton implements View.OnClickListener {

    private final static String TAG = "CameraButton";
    private Activity contextActivity;

    public CameraFab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(Activity contextActivity) {
        this.contextActivity = contextActivity;
        setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (!PermissionManager.hasAll(getContext(),
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestActivityPermissions();
            return;
        }

        Intent toCameraIntent = new Intent(getContext(), CameraActivity.class);
        getContext().startActivity(toCameraIntent);
    }

    // Permissions (result is handled in MainActivity)
    private void requestActivityPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(contextActivity, new String[]{
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionManager.CAMERA_AND_STORAGE);
        }
    }
}
