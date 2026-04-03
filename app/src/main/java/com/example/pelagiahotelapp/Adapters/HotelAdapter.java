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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.pelagiahotelapp.ModelClasses.Hotel;
import com.example.pelagiahotelapp.R;

import java.util.List;
import java.util.Locale;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> hotelList;
    private final Context context;
    private OnHotelClickListener onHotelClickListener;
    private static final String TAG = "HotelAdapter";
    private long lastClickTime = 0;
    private static final long CLICK_TIME_INTERVAL = 1000; // 1 second

    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel);
    }

    public HotelAdapter(Context context, List<Hotel> hotelList, OnHotelClickListener onHotelClickListener) {
        this.context = context;
        this.hotelList = hotelList;
        this.onHotelClickListener = onHotelClickListener;
        Log.d(TAG, "Adapter created with " + hotelList.size() + " hotels and click listener: " + (onHotelClickListener != null));
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hotel_item, parent, false);
        Log.d(TAG, "Creating ViewHolder");
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);
        Log.d(TAG, "Binding hotel at position " + position + ": " + hotel.getName());
        holder.bind(hotel);

        // Set click listener with click prevention
        holder.itemView.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < CLICK_TIME_INTERVAL) {
                Log.d(TAG, "Click too fast, ignoring");
                return;
            }
            lastClickTime = currentTime;

            Log.d(TAG, "═══════════════════════════════════════");
            Log.d(TAG, "★ ★ ★ SINGLE CLICK DETECTED! Position: " + position);
            Log.d(TAG, "★ ★ ★ Hotel: " + hotel.getName());
            Log.d(TAG, "═══════════════════════════════════════");

            if (onHotelClickListener != null) {
                onHotelClickListener.onHotelClick(hotel);
            }
        });

        // Remove any focus or selection states
        holder.itemView.setSelected(false);
        holder.itemView.setPressed(false);
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    public void updateData(List<Hotel> newHotelList) {
        Log.d(TAG, "Updating data with " + newHotelList.size() + " hotels");
        this.hotelList = newHotelList;
        notifyDataSetChanged();
    }

    class HotelViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivHotel;
        private final TextView tvHotelName, tvLocation, tvPrice;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHotel = itemView.findViewById(R.id.ivHotel);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            // Important: Make the item view focusable but not in touch mode
            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(false);
            itemView.setClickable(true);

            Log.d(TAG, "ViewHolder created");
        }

        public void bind(Hotel hotel) {
            // Image loading
            if (hotel.getImages() != null && !hotel.getImages().isEmpty()) {
                String imageUrl = hotel.getImages().get(0);
                Glide.with(context)
                        .load(imageUrl)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.hotel_image_placeholder)
                                .error(R.drawable.hotel_image_placeholder)
                                .transform(new RoundedCorners(16)))
                        .into(ivHotel);
            } else {
                ivHotel.setImageResource(R.drawable.hotel_image_placeholder);
            }

            // Set hotel data
            tvHotelName.setText(hotel.getName());
            tvLocation.setText(hotel.getLocation());
            tvPrice.setText(String.format(Locale.US, "$%.0f / Night", hotel.getPrice()));

            // Clear any focus from child views
            tvHotelName.setFocusable(false);
            tvLocation.setFocusable(false);
            tvPrice.setFocusable(false);
            ivHotel.setFocusable(false);
        }
    }
}