package com.google.firebase.codelab.mlkit;

import android.graphics.Canvas;
import android.hardware.Camera;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.util.List;
import java.util.Random;

public class Preview implements SurfaceHolder.Callback {
    private boolean previewIsRunning;
    private boolean cameraConfigured;
    private Camera camera;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    Preview(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
        this.previewIsRunning = false;
        this.cameraConfigured = false;
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
                // draw(holder);

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
                        // You could apply some pixel operations directly here.
                        Log.d("Camera", "Camera image aquired");
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

    private void draw(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            Random random = new Random();
            canvas.drawRGB(255, 128, 128);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }
        return(result);
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
