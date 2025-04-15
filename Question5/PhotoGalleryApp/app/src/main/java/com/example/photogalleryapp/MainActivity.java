package com.example.photogalleryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PhotoPrefs";
    private static final String KEY_FOLDER_URI = "folder_uri";

    private Uri selectedFolderUri;
    private Uri fullResImageUri;

    private Button btnTakePhoto, btnResetFolder, btnOpenGallery;
    private ImageView imageView;

    // Folder Picker for Camera Save
    private final ActivityResultLauncher<Intent> folderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri treeUri = result.getData().getData();
                    if (treeUri != null) {
                        getContentResolver().takePersistableUriPermission(treeUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        selectedFolderUri = treeUri;
                        saveFolderUri(treeUri);
                        launchCamera();
                    }
                }
            });

    // Gallery Folder Picker
    private final ActivityResultLauncher<Intent> galleryFolderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri treeUri = result.getData().getData();
                    if (treeUri != null) {
                        getContentResolver().takePersistableUriPermission(treeUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        Intent galleryIntent = new Intent(this, GalleryActivity.class);
                        galleryIntent.putExtra("folderUri", treeUri.toString());
                        startActivity(galleryIntent);
                    }
                }
            });

    // Camera Launcher
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (fullResImageUri != null) {
                        imageView.setImageURI(fullResImageUri);
                        Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    // Permission Request
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean granted = result.getOrDefault(Manifest.permission.CAMERA, false);
                if (granted != null && granted) {
                    proceedToFolderOrCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnResetFolder = findViewById(R.id.btnResetFolder);
        btnOpenGallery = findViewById(R.id.btnOpenGallery);
        imageView = findViewById(R.id.imageView);

        selectedFolderUri = loadFolderUri();

        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            } else {
                proceedToFolderOrCamera();
            }
        });

        btnResetFolder.setOnClickListener(v -> {
            clearFolderUri();
            Toast.makeText(this, "Folder selection cleared", Toast.LENGTH_SHORT).show();
        });

        btnOpenGallery.setOnClickListener(v -> openGalleryFolderPicker());
    }

    private void proceedToFolderOrCamera() {
        if (selectedFolderUri == null) {
            openFolderPicker();
        } else {
            launchCamera();
        }
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        folderPickerLauncher.launch(intent);
    }

    private void openGalleryFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        galleryFolderPickerLauncher.launch(intent);
    }

    private void launchCamera() {
        if (selectedFolderUri == null) {
            Toast.makeText(this, "Folder not selected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
            DocumentFile folder = DocumentFile.fromTreeUri(this, selectedFolderUri);

            if (folder == null || !folder.canWrite()) {
                Toast.makeText(this, "Cannot write to folder", Toast.LENGTH_SHORT).show();
                return;
            }

            DocumentFile newFile = folder.createFile("image/jpeg", fileName);
            if (newFile == null) {
                Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
                return;
            }

            fullResImageUri = newFile.getUri();

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fullResImageUri);
            cameraLauncher.launch(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error preparing image file", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFolderUri(Uri uri) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putString(KEY_FOLDER_URI, uri.toString()).apply();
    }

    private Uri loadFolderUri() {
        String uriString = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_FOLDER_URI, null);
        return uriString != null ? Uri.parse(uriString) : null;
    }

    private void clearFolderUri() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .remove(KEY_FOLDER_URI).apply();
        selectedFolderUri = null;
    }
}
