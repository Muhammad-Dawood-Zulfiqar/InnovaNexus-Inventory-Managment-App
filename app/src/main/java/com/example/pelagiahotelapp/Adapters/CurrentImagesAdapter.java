package com.example.pelagiahotelapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pelagiahotelapp.R;

import java.util.List;

public class CurrentImagesAdapter extends RecyclerView.Adapter<CurrentImagesAdapter.ImageViewHolder> {

    private List<String> imageUrls;
    private OnImageRemoveListener removeListener;

    public interface OnImageRemoveListener {
        void onImageRemove(int position);
    }

    public CurrentImagesAdapter(List<String> imageUrls, OnImageRemoveListener removeListener) {
        this.imageUrls = imageUrls;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_current_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .fitCenter()
                .into(holder.ivCurrentImage);

        // Set remove button click listener
        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onImageRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCurrentImage;
        ImageView btnRemove;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCurrentImage = itemView.findViewById(R.id.ivCurrentImage);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}