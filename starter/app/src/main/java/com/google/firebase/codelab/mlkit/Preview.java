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
import java.util.Date;
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
    private Canary processingPreview;
    private Activity activity;
    private OnlineAnalyzer onlineAnalyzer;
    private int counter = 0;

    Preview(SurfaceView surfaceView, GraphicOverlay graphicOverlay, Activity activity) {
        this.surfaceView = surfaceView;
        this.graphicOverlay = graphicOverlay;
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
        this.previewIsRunning = false;
        this.cameraConfigured = false;
        this.processingPreview = new Canary();
        this.paint = new Paint();
        this.activity = activity;
        this.onlineAnalyzer = new OnlineAnalyzer(this.graphicOverlay, this.processingPreview);
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
                        if (!processingPreview.getIsActive()) {
                            Date d1 = new Date();
                            Log.e("AAAAA", "AAAAAAAAAAAAAAAAAA" + counter);
                            Log.e("EEEEEEEEEEEEEEEEEEEEE", new StringBuilder().append(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).toString());
                            processingPreview.setIsActive(true);
                            try {
                                onlineAnalyzer.onPreviewFrame(data, camera);
                            } catch (Exception e) {
                                Log.e("CCCCC", "CCCCCCCCCCCCCCCCCC");
                                Log.e("CCCCC", e.toString());

                            }
                            Log.e("BBBBB", "BBBBBBBBBBBBBBBBBBBBBBB" + counter);
                            Log.e("EEEEEEEEEEEEEEEEEEEEE", new StringBuilder().append(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).toString());
                            counter++;
                            Date d2 = new Date();
                            Log.e("BBBBBBB", "" + (d2.getTime()-d1.getTime())/1000.0);
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
