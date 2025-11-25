package com.example.pelagiahotelapp;

import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

public class ImageLoader {

    private static final String TAG = "ImageLoader";

    // Common request options for hotel images
    private static final RequestOptions REQUEST_OPTIONS = new RequestOptions()
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop();

    public static void loadHotelImage(String imageUrl, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d(TAG, "Loading image with Glide: " + imageUrl);

            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .apply(REQUEST_OPTIONS)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);

        } else {
            Log.w(TAG, "Image URL is null or empty");
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    // Enhanced version with timeout settings
    public static void loadHotelImageWithRetry(String imageUrl, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d(TAG, "Loading image with Glide (with retry): " + imageUrl);

            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .apply(REQUEST_OPTIONS)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Glide load failed: " + (e != null ? e.getMessage() : "Unknown error"));
                            if (e != null) {
                                for (Throwable t : e.getRootCauses()) {
                                    Log.e(TAG, "Root cause: " + t.getMessage());
                                }
                            }
                            return false; // Let Glide handle the error placeholder
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "✅ Image loaded successfully with Glide");
                            return false;
                        }
                    })
                    .into(imageView);

        } else {
            Log.w(TAG, "Image URL is null or empty");
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    // Test method to verify Glide is working
    public static void testGlideLoading(ImageView imageView) {
        String testUrl = "https://via.placeholder.com/400x300/4CAF50/FFFFFF?text=Test+Image";
        Log.d(TAG, "Testing Glide with: " + testUrl);

        Glide.with(imageView.getContext())
                .load(testUrl)
                .apply(REQUEST_OPTIONS)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "❌ Glide test failed: " + (e != null ? e.getMessage() : "Unknown"));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "✅ Glide test successful");
                        return false;
                    }
                })
                .into(imageView);
    }
}