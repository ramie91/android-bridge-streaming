package com.example.test;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import okhttp3.*;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Environment;
import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    ExecutorService service;
    private boolean isRecordingActive = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final int RECORD_DURATION = 10000; // 20 seconds in milliseconds
    Recording recording = null;
    VideoCapture<Recorder> videoCapture = null;
    ImageButton capture, toggleFlash, flipCamera;
    PreviewView previewView;
    boolean seg = false;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private String nbTable;
    private String pos;


    private final ActivityResultLauncher<String> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    startCamera(cameraFacing);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        nbTable = intent.getStringExtra("nbTable");
        pos = intent.getStringExtra("pos");
        previewView = findViewById(R.id.viewFinder);
        capture = findViewById(R.id.capture);
        toggleFlash = findViewById(R.id.toggleFlash);
        flipCamera = findViewById(R.id.flipCamera);

        capture.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.CAMERA);
            } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) !=
                    PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.RECORD_AUDIO);
            } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO);
            }
            else {
                if (isRecordingActive) {
                    stopRecording(); // Arrête l'enregistrement si actif
                } else {
                    startRecordingCycle(); // Démarre l'enregistrement si inactif
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }

        flipCamera.setOnClickListener(view -> {
            cameraFacing = (cameraFacing == CameraSelector.LENS_FACING_BACK) ?
                    CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
            startCamera(cameraFacing);
        });

        service = Executors.newSingleThreadExecutor();
    }
    public String formatName(String nbTable, String pos){
        return "table" + nbTable + "_" + pos;
    }
    public void startM3U8Stream(String filename) {
        // Répertoire de stockage pour les segments et la playlist
        @SuppressLint("SdCardPath") String outputDir = "/sdcard/Movies/CameraX-Video/";
        String test = "/data/app/CameraX-Video/";
        String outputDir3 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/CameraX-Video/";
        String outputDir2 = getExternalFilesDir(null) + "/CameraX-Video/";
        String pathFileName = outputDir + filename + ".mp4";
        // Assurez-vous que le répertoire de sortie existe
        File directory = new File(outputDir);
        if (!directory.exists()) {
            if(directory.mkdirs()){
                Log.d("FFmpeg", "Répertoire Crée");
            }
            else{
                Log.d("FFmpeg", "Répertoire non créé");
            }
        }
        String segmentFileFinal;
        String segmentFilePattern;
        // Chemin de sortie pour les segments et le fichier M3U8
        if(seg){
            segmentFileFinal = outputDir + formatName(nbTable,pos) +"segment_00.ts";
            segmentFilePattern = outputDir + formatName(nbTable,pos) +"segment_%02d.ts";
            seg = false;
        }
        else{
            segmentFilePattern = outputDir + formatName(nbTable,pos) +"segment_%03d.ts";
            segmentFileFinal = outputDir + formatName(nbTable,pos) +"segment_000.ts";
            seg = true;
        }

        String playlistFile = outputDir3 + "playlist.m3u8";

        // Commande FFmpeg pour créer le flux M3U8 avec des segments de 20 secondes
        String ffmpegCommand = String.format("-i \"%s\" -c:v copy " +
                        "-f hls -hls_time 10 -hls_list_size 5 " +
                        "-hls_segment_filename \"%s\" \"%s\"",
                pathFileName, segmentFilePattern, playlistFile);
        String ffmpegCommand2 = String.format(
                "-i \"%s\" -c:v libx264 -b:v 800k -preset veryfast -s 1920x1080 " +
                        "-c:a aac -b:a 128k " +
                        "-f hls -hls_time 10 -hls_list_size 5 " +
                        "-hls_segment_filename \"%s\" \"%s\"",
                pathFileName, segmentFilePattern, playlistFile);
        Log.d("FFmpeg", "FFmpeg command: " + ffmpegCommand); // Log la commande FFmpeg
        // Exécution de la commande FFmpeg
        FFmpegKit.executeAsync(ffmpegCommand2, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.d("FFmpeg", "M3U8 stream started successfully!");
                File segmentFile = new File(segmentFileFinal);
                if (segmentFile.exists()) {
                    uploadFile(segmentFile.getAbsolutePath());
                    File m3u8File = new File(playlistFile);
                    if (m3u8File.exists()) {
                        uploadFile(m3u8File.getAbsolutePath());
                    }
                }
                File sourceFile = new File(pathFileName);
                if (sourceFile.exists()) {
                    boolean deleted = sourceFile.delete();
                    if (deleted) {
                        Log.d("FFmpeg", "Source file deleted successfully: " + pathFileName);
                    } else {
                        Log.e("FFmpeg", "Failed to delete source file: " + pathFileName);
                    }
                }

            } else {
                Log.e("FFmpeg", "Failed to start M3U8 stream: " + session.getFailStackTrace());
            }
        });
    }
    public void uploadFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e("Upload", "File not found: " + filePath);
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder().build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("application/octet-stream")))
                .build();

        Request request = new Request.Builder()
                .url("https://orsay-bridge.fr/upload")
                .post(requestBody)
                .header("User-Agent", "PostmanRuntime/7.42.0") // Mettez le même User-Agent que dans Postman
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Upload", "Failed to upload file: " + filePath, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("Upload", "File uploaded successfully: " + filePath);
                    File sourceFile2 = new File(filePath);
                    if (sourceFile2.exists()) {
                        boolean deleted = sourceFile2.delete();
                        if (deleted) {
                            Log.d("FFmpeg", "Source file deleted successfully: " + filePath);
                        } else {
                            Log.e("FFmpeg", "Failed to delete source file: " + filePath);
                        }
                    }
                } else {
                    Log.e("Upload", "Failed to upload file: " + response.message());
                }
            }
        });
    }


    private void startRecordingCycle() {
        if (!isRecordingActive) {
            isRecordingActive = true;
            captureVideo(); // Démarre l'enregistrement

            // Programme le redémarrage de l'enregistrement toutes les RECORD_DURATION millisecondes
            handler.postDelayed(() -> {
                if (isRecordingActive) { // Vérifiez si l'enregistrement est toujours actif
                    startRecordingCycle();
                }
            }, RECORD_DURATION);
        }
    }

    public void captureVideo() {
        capture.setImageResource(R.drawable.round_stop_circle_24);
        if (recording != null) {
            recording.stop();
            recording = null;
            capture.setImageResource(R.drawable.round_stop_circle_24); // Réinitialise l'icône du bouton
        }
        // Marquez l'enregistrement comme actif
        isRecordingActive = true;

        // Créez un nom unique pour chaque enregistrement
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
                .format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(
                getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Démarrez un nouvel enregistrement
        recording = videoCapture.getOutput().prepareRecording(MainActivity.this, options)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(MainActivity.this), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                        capture.setEnabled(true);
                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                            String msg = "Video capture succeeded: "
                                    + ((VideoRecordEvent.Finalize) videoRecordEvent)
                                    .getOutputResults().getOutputUri();
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            System.out.println(msg);
                            startM3U8Stream(name);
                        } else {
                            String msg = "Error: "
                                    + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            isRecordingActive = false; // Marquez l'enregistrement comme inactif à la fin
                        }


                    }
                });

        // Planifiez la prochaine capture si l'enregistrement est actif
        if (isRecordingActive) {
            handler.postDelayed(this::captureVideo, RECORD_DURATION);
        }
    }
    public void stopRecording() {
        if (recording != null) {
            recording.stop();
            recording = null;
        }
        handler.removeCallbacksAndMessages(null);
        isRecordingActive = false;
        capture.setImageResource(R.drawable.round_fiber_manual_record_24); // Réinitialise l'icône
    }

    public void startCamera(int cameraFacing) {
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(MainActivity.this);

        processCameraProvider.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = processCameraProvider.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.unbindAll();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);

                toggleFlash.setOnClickListener(view -> toggleFlash(camera));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(MainActivity.this));
    }

    private void toggleFlash(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                camera.getCameraControl().enableTorch(true);
                toggleFlash.setImageResource(R.drawable.round_flash_off_24);
            } else {
                camera.getCameraControl().enableTorch(false);
                toggleFlash.setImageResource(R.drawable.round_flash_on_24);
            }
        } else {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Flash is not available currently", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null); // Stop the cycle when activity is destroyed
        if (recording != null) {
            recording.stop();
        }
        service.shutdown();
    }
}