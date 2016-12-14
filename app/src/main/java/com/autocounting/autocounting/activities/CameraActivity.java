package com.autocounting.autocounting.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.autocounting.autocounting.R;
import com.autocounting.autocounting.managers.PermissionManager;
import com.autocounting.autocounting.models.Receipt;
import com.autocounting.autocounting.utils.ImageHandler;
import com.autocounting.autocounting.utils.ImageSaver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    private static final int NUMBER_OF_IMAGES_CAPTURED = 1;

    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private int state;

    private File imageFolder;

    private FloatingActionButton cameraButton;

    private CameraDevice cameraDevice;
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private CaptureRequest captureRequest;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private CameraCaptureSession.CaptureCallback sessionCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            process(result);
        }

        private void process(CaptureResult result) {
            switch (state) {
                case STATE_PREVIEW:
                    // Keep running
                    break;
                case STATE_WAIT_LOCK:
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);

                    if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                            afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                        new MediaActionSound().play(MediaActionSound.SHUTTER_CLICK);
                        state = STATE_PREVIEW;
                        captureImage();
                    } else {
                        Log.i(TAG, "afState is " + afState);
                    }
                    break;
            }
        }
    };

    private TextureView textureView;
    private Size previewSize;
    private String cameraId;

    private HandlerThread handlerThread;
    private Handler handler;
    private File imageFile;
    private ImageReader imageReader;
    private final ImageReader.OnImageAvailableListener onImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.i(TAG, "Saving image");
                    handler.post(new ImageSaver(CameraActivity.this, reader.acquireNextImage()));
                }
            };

    private TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    setupCamera(width, height);
                    connectCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        createImageFolder();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        textureView = (TextureView) findViewById(R.id.camera_preview);

        cameraButton = (FloatingActionButton) findViewById(R.id.camera_capture_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraButton.setEnabled(false);
                takePhoto();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();

        if (textureView.isAvailable()) {
            setupCamera(textureView.getWidth(), textureView.getHeight());
            connectCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager
                        .getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = cameraCharacteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                Size largestImageSize = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new Comparator<Size>() {
                            @Override
                            public int compare(Size a, Size b) {
                                return Long.signum(a.getWidth() * a.getHeight() - b.getWidth() * b.getHeight());
                            }
                        }
                );

                previewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);

                imageReader = ImageReader.newInstance(
                        largestImageSize.getWidth(),
                        largestImageSize.getHeight(),
                        ImageFormat.JPEG,
                        NUMBER_OF_IMAGES_CAPTURED);
                imageReader.setOnImageAvailableListener(onImageAvailableListener, handler);

                this.cameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            handleCameraAccessException(e);
        }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestActivityPermissions();
            return;
        }

        try {
            cameraManager.openCamera(cameraId, stateCallback, handler);

        } catch (CameraAccessException e) {
            handleCameraAccessException(e);
        }
    }

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    private Size getPreferredPreviewSize(Size[] mapSizes, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size sizeOption : mapSizes) {
            if (width > height) {
                if (sizeOption.getWidth() > width && sizeOption.getHeight() > height) {
                    sizeList.add(sizeOption);
                }
            } else {
                if (sizeOption.getWidth() > height && sizeOption.getHeight() > width) {
                    sizeList.add(sizeOption);
                }
            }
        }

        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return mapSizes[0];
    }

    private void createCameraPreviewSession() {
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            if (cameraDevice == null)
                                return;
                            try {
                                captureRequest = captureRequestBuilder.build();
                                cameraCaptureSession = session;
                                cameraCaptureSession.setRepeatingRequest(
                                        captureRequest,
                                        sessionCaptureCallback,
                                        handler
                                );
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            // Handle
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void takePhoto() {
        Log.i(TAG, "Take photo");
        try {
            Log.i(TAG, "Create image file");
            imageFile = new Receipt().makeFile(imageFolder);
        } catch (IOException e) {
            Log.i(TAG, "Failed to create image file");
            e.printStackTrace();
        }

        lockFocus();
    }

    private void createImageFolder() {
        File imageFile = Environment.getExternalStorageDirectory();
        imageFolder = new File(imageFile, "receipt_queue");

        if (!imageFolder.exists())
            imageFolder.mkdirs();
    }

    private void lockFocus() {
        Log.i(TAG, "Locking focus ...");
        state = STATE_WAIT_LOCK;
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

        try {
            Log.i(TAG, "Capture session: Capture");
            cameraCaptureSession.capture(captureRequestBuilder.build(), sessionCaptureCallback, handler);
        } catch (CameraAccessException e) {
            Log.i(TAG, "Capture session to capture ...");
            handleCameraAccessException(e);
        }
    }

    private void captureImage() {
        Log.i(TAG, "Capture Image");

        Handler uiHandler = new Handler(getMainLooper());

        try {
            CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics cameraCharacteristics = cameraManager
                    .getCameraCharacteristics(cameraId);

            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION));
            captureRequestBuilder.set(CaptureRequest.JPEG_QUALITY, (byte) ImageHandler.JPEG_COMPRESSION_RATE);

            cameraCaptureSession.capture(
                    captureRequestBuilder.build(),
                    sessionCaptureCallback,
                    handler
            );

            Log.i(TAG, "Upload and go to main");
            startActivity(new Intent(this, MainActivity.class));

        } catch (CameraAccessException e) {
            handleCameraAccessException(e);
        }
    }

    private void handleCameraAccessException(Exception e) {
        e.printStackTrace();
        startActivity(new Intent(this, MainActivity.class));
        Toast.makeText(getApplicationContext(), "Camera is not available", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void startBackgroundThread() {
        Log.i(TAG, "Starting background thread");
        handlerThread = new HandlerThread("camera-handler");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        Log.i(TAG, "Stopping Background thread");
        handlerThread.quitSafely();

        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Permissions
    private void requestActivityPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionManager.CAMERA_AND_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PermissionManager.CAMERA_AND_STORAGE) {
            createCameraPreviewSession();
            connectCamera();
        }
    }
}
