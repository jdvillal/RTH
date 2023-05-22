package com.espol.rth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.view.PreviewView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.espol.rth.databinding.ActivityCamPreviewBinding;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import java.util.concurrent.ExecutionException;

public class CamPreviewActivity extends AppCompatActivity {

    private ActivityCamPreviewBinding binding;

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_preview);

        this.set_fullscreen();

        binding = ActivityCamPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.previewTV.setText("Acomoda tu cámara en un buen ángulo y presiona el botón inferior cuando estes listo para empezar a conducir.");

        //this.previewView = (PreviewView) findViewById(R.id.previewView);
        this.previewView = binding.previewView;
        this.previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        Log.d("CamPreview ==========>", previewView.toString());

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

        binding.readyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoBufferActivity();
            }
        });

    }

    private void gotoBufferActivity(){
        Intent i = new Intent(this, CamBufferActivity.class);
        startActivity(i);
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

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);

    }
}