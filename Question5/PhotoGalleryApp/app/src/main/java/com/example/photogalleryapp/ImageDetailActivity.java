package com.example.photogalleryapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.Date;

public class ImageDetailActivity extends AppCompatActivity {

    private Uri imageUri, folderUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
        folderUri = Uri.parse(getIntent().getStringExtra("folderUri"));

        ImageView imageView = findViewById(R.id.imageViewDetail);
        TextView textDetails = findViewById(R.id.textDetails);
        Button btnDelete = findViewById(R.id.btnDelete);

        Glide.with(this).load(imageUri).into(imageView);

        DocumentFile file = DocumentFile.fromSingleUri(this, imageUri);
        if (file != null) {
            String name = file.getName();
            long size = file.length();
            long lastModified = file.lastModified();
            String formattedSize = new DecimalFormat("#.##").format(size / 1024.0) + " KB";
            String date = DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date(lastModified)).toString();

            textDetails.setText("Name: " + name + "\nSize: " + formattedSize +
                    "\nDate Taken: " + date + "\nPath: " + imageUri.getPath());
        }

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        DocumentFile fileToDelete = DocumentFile.fromSingleUri(this, imageUri);
                        if (fileToDelete != null && fileToDelete.delete()) {
                            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                            // Send broadcast to refresh the gallery
                            Intent intent = new Intent("com.example.photogalleryapp.REFRESH_GALLERY");
                            intent.putExtra("folderUri", folderUri.toString());
                            sendBroadcast(intent);  // Trigger the update
                            finish();  // Close activity after deletion
                        } else {
                            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
}