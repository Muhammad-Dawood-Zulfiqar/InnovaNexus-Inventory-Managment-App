package com.example.pelagiahotelapp;


import android.net.Uri;
import android.widget.ImageView;

import com.cloudinary.Transformation;
import com.squareup.picasso.Picasso;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import java.util.Map;

public class ImageLoader {

    public static void loadHotelImage(String imageUrl, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Use Picasso to load Cloudinary image with transformations
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .fit()
                    .centerCrop()
                    .into(imageView);
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    // Method to upload image to Cloudinary (if needed in future)

        // ... existing methods ...

        // Method to upload image from URI for admin
        public static void uploadImage(String fileUri, UploadCallback callback) {
            MediaManager.get().upload(Uri.parse(fileUri))
                    .option("folder", "hotels") // Organize images in Cloudinary
                    .option("transformation", new Transformation()
                            .width(800)
                            .height(600)
                            .crop("fill")
                            .quality("auto:good"))
                    .callback(callback)
                    .dispatch();
        }

        // Method to upload image from file path (alternative)
        public static void uploadImageFromPath(String filePath, UploadCallback callback) {
            MediaManager.get().upload(filePath)
                    .option("folder", "hotels")
                    .option("transformation", new Transformation()
                            .width(800)
                            .height(600)
                            .crop("fill")
                            .quality("auto:good"))
                    .callback(callback)
                    .dispatch();
        }

}