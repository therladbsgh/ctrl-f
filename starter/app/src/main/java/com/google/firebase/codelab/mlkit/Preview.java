package com.google.firebase.codelab.mlkit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Random;

public class Preview implements SurfaceHolder.Callback {
    private boolean previewIsRunning;
    private boolean cameraConfigured;
    private Camera camera;

    private GraphicOverlay graphicOverlay;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private boolean processingPreview;
    private Activity activity;
    private OnlineAnalyzer onlineAnalyzer;

    Preview(SurfaceView surfaceView, GraphicOverlay graphicOverlay, Activity activity) {
        this.surfaceView = surfaceView;
        this.graphicOverlay = graphicOverlay;
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
        this.previewIsRunning = false;
        this.cameraConfigured = false;
        this.processingPreview = false;
        this.paint = new Paint();
        this.activity = activity;
        this.onlineAnalyzer = new OnlineAnalyzer(graphicOverlay);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            camera = Camera.open();
        }catch(RuntimeException e){
            Log.e("Log", "init_camera: " + e);
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!previewIsRunning && (camera != null)) {
            try {

                Camera.Parameters parameters=camera.getParameters();
                List<String> focusModes = parameters.getSupportedFocusModes();
                if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else
                if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                camera.setDisplayOrientation(90); // Hardcoded. Fix this
                camera.setParameters(parameters);
                cameraConfigured=true;
                camera.setPreviewDisplay(holder);
                camera.setPreviewCallback(new Camera.PreviewCallback() {
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        if (!processingPreview) {
                            processingPreview = true;
                            onlineAnalyzer.onPreviewFrame(data, camera);
                            processingPreview = false;
                        }
                    }
                });
                camera.startPreview();
                previewIsRunning = true;
            } catch (Exception e) {
                Log.e("CameraOn", "init_camera: " + e);
                return;
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this.activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (previewIsRunning && (camera != null)) {
            camera.stopPreview();
            previewIsRunning = false;
            camera.release();
            camera = null;
            cameraConfigured = false;
        }
    }
}
