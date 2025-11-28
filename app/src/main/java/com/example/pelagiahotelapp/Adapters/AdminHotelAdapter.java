package com.example.pelagiahotelapp.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pelagiahotelapp.ModelClasses.Hotel;
import com.example.pelagiahotelapp.Utilities.ImageLoader;
import com.example.pelagiahotelapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdminHotelAdapter extends RecyclerView.Adapter<AdminHotelAdapter.HotelViewHolder> {

    private List<Hotel> hotelList;
    private Context context;
    private OnDeleteClickListener onDeleteClickListener;
    private OnUpdateClickListner onUpdateClickListner;
    public interface OnDeleteClickListener {
        void onDeleteClick(Hotel hotel);
    }

    public interface OnUpdateClickListner{
        void onUpdateClick(Hotel hotel);

    }


    public AdminHotelAdapter(List<Hotel> hotelList, OnDeleteClickListener onDeleteClickListener,OnUpdateClickListner onUpdateClickListner) {
        this.hotelList = hotelList;
        this.onDeleteClickListener = onDeleteClickListener;
        this.onUpdateClickListner=onUpdateClickListner;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);

        holder.tvHotelName.setText(hotel.getName());
        holder.tvHotelLocation.setText(hotel.getLocation());
        holder.tvHotelPrice.setText(String.format("$%.2f / night", hotel.getPrice()));

        // Truncate description if too long
        String description = hotel.getDescription();
        if (description != null && description.length() > 100) {
            description = description.substring(0, 100) + "...";
        }
        holder.tvHotelDescription.setText(description != null ? description : "No description available");

        // Load hotel image using your ImageLoader class
        if (hotel.getImages() != null && !hotel.getImages().isEmpty()) {
            Log.d("Cloudinary","going inside ImageLoader to load image");
            ImageLoader.loadHotelImage(hotel.getImages().get(0), holder.ivHotelImage);
        } else {
            holder.ivHotelImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Set delete button click listener
        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(hotel);
            }
        });
        holder.btnUpdate.setOnClickListener(v -> {
            if(onDeleteClickListener!=null)
            {

onUpdateClickListner.onUpdateClick(hotel);            }
        });
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvHotelLocation, tvHotelPrice, tvHotelDescription;
        ImageView ivHotelImage;
        MaterialButton btnDelete;
        MaterialButton btnUpdate;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvHotelLocation = itemView.findViewById(R.id.tvHotelLocation);
            tvHotelPrice = itemView.findViewById(R.id.tvHotelPrice);
            tvHotelDescription = itemView.findViewById(R.id.tvHotelDescription);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnUpdate=itemView.findViewById(R.id.btnUpdate);
        }
    }
}