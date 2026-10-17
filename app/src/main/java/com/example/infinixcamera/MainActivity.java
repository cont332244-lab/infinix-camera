package com.example.infinixcamera;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int REQUEST_CAMERA = 41;
    private PreviewView previewView;
    private TextView status;
    private Button captureButton, switchButton;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private boolean frontCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraExecutor = Executors.newSingleThreadExecutor();
        buildInterface();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(12, 18, 28));

        TextView title = new TextView(this);
        title.setText("مراقب الكاميرا");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        title.setPadding(12, 18, 12, 6);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        status = new TextView(this);
        status.setText("جارٍ تجهيز الكاميرا...");
        status.setTextColor(Color.rgb(120, 220, 170));
        status.setTextSize(15);
        status.setGravity(Gravity.CENTER);
        status.setPadding(8, 6, 8, 12);
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        previewView = new PreviewView(this);
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        LinearLayout.LayoutParams previewParams =
                new LinearLayout.LayoutParams(-1, 0, 1f);
        previewParams.setMargins(12, 4, 12, 10);
        root.addView(previewView, previewParams);

        captureButton = new Button(this);
        captureButton.setText("التقاط صورة");
        captureButton.setOnClickListener(v -> takePhoto());
        root.addView(captureButton, new LinearLayout.LayoutParams(-1, -2));

        switchButton = new Button(this);
        switchButton.setText("التبديل إلى الكاميرا الأمامية");
        switchButton.setOnClickListener(v -> {
            frontCamera = !frontCamera;
            startCamera();
        });
        root.addView(switchButton, new LinearLayout.LayoutParams(-1, -2));

        TextView note = new TextView(this);
        note.setText("تُحفظ الصور في معرض الهاتف. هذه النسخة تستخدم الكاميرا محلياً.");
        note.setTextColor(Color.LTGRAY);
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        note.setPadding(12, 8, 12, 16);
        root.addView(note, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }

    private void startCamera() {
        status.setText("جارٍ تشغيل الكاميرا...");
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector selector = frontCamera
                        ? CameraSelector.DEFAULT_FRONT_CAMERA
                        : CameraSelector.DEFAULT_BACK_CAMERA;

                provider.unbindAll();
                provider.bindToLifecycle(this, selector, preview, imageCapture);
                status.setText(frontCamera ? "الكاميرا الأمامية تعمل" : "الكاميرا الخلفية تعمل");
                captureButton.setEnabled(true);
            } catch (Exception e) {
                status.setText("تعذر تشغيل الكاميرا. تحقق من الأذونات.");
                Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "انتظر حتى تعمل الكاميرا", Toast.LENGTH_SHORT).show();
            return;
        }
        String filename = "InfinixCamera_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/InfinixCamera");
        }
        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values).build();

        captureButton.setEnabled(false);
        imageCapture.takePicture(options, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults result) {
                        runOnUiThread(() -> {
                            captureButton.setEnabled(true);
                            Toast.makeText(MainActivity.this,
                                    "تم حفظ الصورة في المعرض", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        runOnUiThread(() -> {
                            captureButton.setEnabled(true);
                            Toast.makeText(MainActivity.this,
                                    "فشل حفظ الصورة: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else if (requestCode == REQUEST_CAMERA) {
            status.setText("يجب السماح باستخدام الكاميرا لتشغيل التطبيق.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}
