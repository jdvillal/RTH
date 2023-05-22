package com.espol.rth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Range;
import android.view.View;

import com.espol.rth.databinding.ActivityCamBufferBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CamBufferActivity extends AppCompatActivity {
    static {
        System.loadLibrary("rth");
    }

    private ActivityCamBufferBinding binding;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private ImageAnalysis imageAnalysis;

    private long lastTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_buffer);

        this.set_fullscreen();

        binding = ActivityCamBufferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bufferTV.setText("Grabando...");

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraAnalysis(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());
    }

    @SuppressLint({"RestrictedApi", "UnsafeOptInUsageError"})
    public void bindCameraAnalysis(ProcessCameraProvider cameraProvider){
        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        @SuppressLint("UnsafeOptInUsageError") Camera2Interop.Extender ext = new Camera2Interop.Extender<>(builder);
        //ext.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
        ext.setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<Integer>(60, 60));
        this.imageAnalysis = builder.build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        imageAnalysis.setAnalyzer(
                getExecutor(),
                //ex,
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        //long startTime = SystemClock.elapsedRealtime();
                        long tmstmp = imageProxy.getImageInfo().getTimestamp();
                        imageProxy.close();
                        long elapsed = (tmstmp - lastTimestamp)/1000000;
                        lastTimestamp = tmstmp;
                        update_fpsTV(1000/elapsed);
                        Log.d("ANALYSER =====> ", String.valueOf(elapsed));
                        //long endTime = SystemClock.elapsedRealtime();
                        //long elapsedMilliSeconds = endTime - startTime;
                        //Log.d("ANALYSER ", "Log.d Time elapsed "+ elapsedMilliSeconds);

                    }
                });

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);
    }

    private void update_fpsTV(long fps){
        this.binding.fpsTV.setText(fps + "FPS");
    }


    public void set_fullscreen(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }


    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    public native String stringFromJNI();

}