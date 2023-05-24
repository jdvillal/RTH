package com.espol.rth;

import static androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888;
import static androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.imagecapture.RgbaImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.HardwareBuffer;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;

import com.espol.rth.databinding.ActivityCamBufferBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        //builder.setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888);//set by default
        builder.setOutputImageFormat(OUTPUT_IMAGE_FORMAT_YUV_420_888);
        builder.setTargetResolution(new Size(2560, 1440));
        builder.setMaxResolution(new Size(2560, 1440));
        //builder.setTargetAspectRatio(AspectRatio.RATIO_16_9);
        builder.setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER);
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

                        long elapsed = (tmstmp - lastTimestamp)/1000000;
                        lastTimestamp = tmstmp;
                        update_fpsTV(1000/elapsed);
                        Log.d("ANALYSER =====> ", "W: "+ String.valueOf(imageProxy.getImage().getWidth())+" H:"+String.valueOf(imageProxy.getImage().getHeight()));
                        Log.d("ANALYSER =====>", "format: " + imageProxy.getImage().getFormat() + " " + ImageFormat.YUV_420_888);
                        Image image = null;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            image = imageProxy.getImage();
                            YuvImage  yuvImage = toByteArray(image);
                            int width = image.getWidth();
                            int height = image.getHeight();
                            long timestamp = image.getTimestamp();
                            //to_byte_buffer(image);
                            //toBitmap(image);
                            addFrame(yuvImage.getYuvData(), yuvImage.getYuvData().length, width, height, timestamp);
                            imageProxy.close();
                            //Log.d("ANALYSER =====> ", String.valueOf(byte_array.length) + "Bytes");



                        }else{
                            imageProxy.close();
                        }

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


    public /*Bitmap*/ YuvImage toByteArray(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer(); // Y
        ByteBuffer vuBuffer = planes[2].getBuffer(); // VU

        int ySize = yBuffer.remaining();
        int vuSize = vuBuffer.remaining();

        byte[] nv21 = new byte[ySize + vuSize];
        yBuffer.get(nv21, 0, ySize);
        vuBuffer.get(nv21, ySize, vuSize);
        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        //yuvImage.getYuvData();
        Log.d("ANALYSIS INNER ====< ", "nv21 size:  "+ yuvImage.getYuvData().length + " bytes" + "  arr size: " +yuvImage.getYuvData().length);
        //ByteArrayOutputStream out = new ByteArrayOutputStream();
        return yuvImage;
        //new YuvImage();

        /*start time elapse*/
        //long startTime = SystemClock.elapsedRealtime();
        /*end*/

        //yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);

        /*End time elapse */
        //long endTime = SystemClock.elapsedRealtime();
        //long elapsedMilliSeconds = endTime - startTime;
        //Log.d("ANALYSER TIME ELAPSE ====> ", "Inner Time elapsed "+ elapsedMilliSeconds);
        /* End */

        //byte[] imageBytes = out.toByteArray();
        //return imageBytes;
        //return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private void img_pixels(ImageProxy imageProxy){
        //int c = imageProxy.getPlanes()[0].getBuffer(0);
    }

    private Bitmap toBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        ByteBuffer b = buffer.duplicate();
        //Log.d("ANALYSER INNER 2 ===> ", String.valueOf(planes[0].buffer[0]));
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth()+rowPadding/pixelStride,
                image.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        Log.d("BITMAP ===> ", String.valueOf(b.array().length));
        return bitmap;
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
    public native int addFrame(byte[] array, int size, int width, int height, long timestamp);

}