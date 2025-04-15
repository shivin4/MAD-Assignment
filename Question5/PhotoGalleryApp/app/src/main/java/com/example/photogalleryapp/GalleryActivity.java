package com.example.photogalleryapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class GalleryActivity extends AppCompatActivity {

    private Uri folderUri;
    private DocumentFile[] files;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        folderUri = Uri.parse(getIntent().getStringExtra("folderUri"));
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        loadImages();  // Load images initially
        imageAdapter = new ImageAdapter(files);
        recyclerView.setAdapter(imageAdapter);

        // Register the broadcast receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(refreshReceiver, new IntentFilter("com.example.photogalleryapp.REFRESH_GALLERY"), Context.RECEIVER_NOT_EXPORTED);
        }
    }

    // BroadcastReceiver to handle gallery refresh
    private final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Uri folderUri = Uri.parse(intent.getStringExtra("folderUri"));
            if (folderUri != null) {
                GalleryActivity.this.folderUri = folderUri;
                loadImages();  // Reload images after deletion
                imageAdapter.setFiles(files);  // Update adapter data
            }
        }
    };


    // Load images from the folder and update the files array
    private void loadImages() {
        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);
        if (folder != null && folder.isDirectory()) {
            files = folder.listFiles();  // Get all files from the folder
        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

        private DocumentFile[] files;

        ImageAdapter(DocumentFile[] files) {
            this.files = files;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.imageView);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DocumentFile file = files[position];
            if (file.getType() != null && file.getType().startsWith("image/")) {
                Glide.with(holder.imageView.getContext())
                        .load(file.getUri())
                        .into(holder.imageView);

                holder.imageView.setOnClickListener(v -> {
                    Intent intent = new Intent(GalleryActivity.this, ImageDetailActivity.class);
                    intent.putExtra("imageUri", file.getUri().toString());
                    intent.putExtra("folderUri", folderUri.toString());
                    startActivity(intent);
                });
            }
        }
        void setFiles(DocumentFile[] newFiles) {
            this.files = newFiles;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return files != null ? files.length : 0;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(refreshReceiver);  // Unregister receiver
    }
}
