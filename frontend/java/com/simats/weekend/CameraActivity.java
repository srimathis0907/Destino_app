package com.simats.weekend;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    private PreviewView viewFinder;
    private ImageButton captureButton;
    private ImageButton switchCameraButton;
    private Button modeButton;
    private TextView timerText;
    private ImageButton pauseResumeButton; // NEW

    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private Camera camera;

    private boolean isPhotoMode = true;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    private boolean isRecordingPaused = false; // NEW

    private Handler timerHandler;
    private long recordTime = 0;
    private final Runnable timerRunnable = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            recordTime++;
            long minutes = TimeUnit.SECONDS.toMinutes(recordTime);
            long seconds = recordTime % 60;
            timerText.setText(String.format("%02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 1000);
        }
    };


    private final ActivityResultLauncher<Intent> previewLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setResult(RESULT_OK, result.getData());
                    finish();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        viewFinder = findViewById(R.id.viewFinder);
        captureButton = findViewById(R.id.capture_button);
        switchCameraButton = findViewById(R.id.switch_camera_button);
        modeButton = findViewById(R.id.mode_button);
        timerText = findViewById(R.id.timer_text);
        pauseResumeButton = findViewById(R.id.pause_resume_button); // NEW

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, 10);
        }

        setupListeners();
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupListeners() {
        captureButton.setOnClickListener(v -> {
            if (isPhotoMode) {
                takePhoto();
            } else {
                captureVideo();
            }
        });

        switchCameraButton.setOnClickListener(v -> {
            cameraSelector = (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) ?
                    CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
            startCamera();
        });

        modeButton.setOnClickListener(v -> {
            isPhotoMode = !isPhotoMode;
            if (isPhotoMode) {
                modeButton.setText("Video");
                captureButton.setBackgroundResource(R.drawable.ic_shutter);
            } else {
                modeButton.setText("Photo");
                captureButton.setBackgroundResource(R.drawable.ic_record_video);
            }
            startCamera();
        });

        // NEW: Pause/Resume button listener
        pauseResumeButton.setOnClickListener(v -> {
            if (recording == null) return;

            if (isRecordingPaused) {
                recording.resume();
            } else {
                recording.pause();
            }
        });

        ScaleGestureDetector.SimpleOnScaleGestureListener listener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (camera != null) {
                    float currentZoomRatio = camera.getCameraInfo().getZoomState().getValue().getZoomRatio();
                    float delta = detector.getScaleFactor();
                    camera.getCameraControl().setZoomRatio(currentZoomRatio * delta);
                }
                return true;
            }
        };
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, listener);
        viewFinder.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void takePhoto() {
        // This method remains unchanged
        if (imageCapture == null) return;
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Weekend-App");
        }
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                intent.putExtra("media_uri", outputFileResults.getSavedUri().toString());
                intent.putExtra("is_photo", true);
                previewLauncher.launch(intent);
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    private void captureVideo() {
        if (videoCapture == null) return;
        captureButton.setEnabled(false);

        Recording curRecording = recording;
        if (curRecording != null) {
            curRecording.stop();
            recording = null;
            return;
        }

        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Weekend-App");
        }

        MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions.Builder(
                getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI).setContentValues(contentValues).build();

        recording = videoCapture.getOutput()
                .prepareRecording(this, mediaStoreOutputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), recordEvent -> {
                    // UPDATED: Handle new Pause and Resume events
                    if (recordEvent instanceof VideoRecordEvent.Start) {
                        captureButton.setEnabled(true);
                        captureButton.setBackgroundResource(R.drawable.ic_stop_video);
                        pauseResumeButton.setVisibility(View.VISIBLE);
                        pauseResumeButton.setImageResource(R.drawable.ic_pause);
                        isRecordingPaused = false;
                        timerHandler = new Handler(Looper.getMainLooper());
                        recordTime = 0;
                        timerText.setVisibility(View.VISIBLE);
                        timerHandler.post(timerRunnable);
                    } else if (recordEvent instanceof VideoRecordEvent.Pause) {
                        isRecordingPaused = true;
                        pauseResumeButton.setImageResource(R.drawable.ic_resume);
                        timerHandler.removeCallbacks(timerRunnable);
                    } else if (recordEvent instanceof VideoRecordEvent.Resume) {
                        isRecordingPaused = false;
                        pauseResumeButton.setImageResource(R.drawable.ic_pause);
                        timerHandler.post(timerRunnable);
                    } else if (recordEvent instanceof VideoRecordEvent.Finalize) {
                        captureButton.setEnabled(true);
                        captureButton.setBackgroundResource(R.drawable.ic_record_video);
                        pauseResumeButton.setVisibility(View.GONE);
                        isRecordingPaused = false;

                        if (timerHandler != null) {
                            timerHandler.removeCallbacks(timerRunnable);
                            timerText.setVisibility(View.GONE);
                        }

                        if (!((VideoRecordEvent.Finalize) recordEvent).hasError()) {
                            Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                            intent.putExtra("media_uri", ((VideoRecordEvent.Finalize) recordEvent).getOutputResults().getOutputUri().toString());
                            intent.putExtra("is_photo", false);
                            previewLauncher.launch(intent);
                        } else {
                            recording = null;
                            Log.e(TAG, "Video capture ends with error: " + ((VideoRecordEvent.Finalize) recordEvent).getError());
                        }
                    }
                });
    }

    // This method remains unchanged
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
                cameraProvider.unbindAll();
                if (isPhotoMode) {
                    imageCapture = new ImageCapture.Builder().build();
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                } else {
                    Recorder recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HD)).build();
                    videoCapture = VideoCapture.withOutput(recorder);
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
                }
            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // This method remains unchanged
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // This method remains unchanged
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}