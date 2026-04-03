package com.example.pelagiahotelapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pelagiahotelapp.ModelClasses.Booking;
import com.example.pelagiahotelapp.R;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<Booking> requestList;
    private RequestActionListener actionListener;

    public interface RequestActionListener {
        void onConfirmBooking(Booking booking);
        void onCancelBooking(Booking booking);
    }

    public RequestAdapter(List<Booking> requestList, RequestActionListener actionListener) {
        this.requestList = requestList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Booking booking = requestList.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public void updateData(List<Booking> newRequestList) {
        this.requestList = newRequestList;
        notifyDataSetChanged();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHotelName, tvCheckIn, tvCheckOut, tvGuests, tvTotalPrice, tvNights;
        private ImageView ivHotelImage;
        private Button btnConfirm, btnCancel;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvCheckIn = itemView.findViewById(R.id.tvCheckIn);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOut);
            tvGuests = itemView.findViewById(R.id.tvGuests);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvNights = itemView.findViewById(R.id.tvNights);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        public void bind(Booking booking) {
            // Set hotel information
            tvHotelName.setText(booking.getHotelName());



            // Set booking details
            if (booking.getCheckInDate() != null) {
                tvCheckIn.setText("Check-in: " + booking.getCheckInDate());
            } else {
                tvCheckIn.setText("Check-in: Not specified");
            }

            if (booking.getCheckOutDate() != null) {
                tvCheckOut.setText("Check-out: " + booking.getCheckOutDate());
            } else {
                tvCheckOut.setText("Check-out: Not specified");
            }

            tvGuests.setText("Guests: " + booking.getGuests());
            tvNights.setText("Nights: " + booking.getNights());
            tvTotalPrice.setText(String.format("$%.2f", booking.getTotalPrice()));

            // Load hotel image
            if (booking.getHotelImage() != null && !booking.getHotelImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(booking.getHotelImage())
                        .placeholder(R.drawable.hotel_image_placeholder)
                        .error(R.drawable.hotel_image_placeholder)
                        .into(ivHotelImage);
            } else {
                ivHotelImage.setImageResource(R.drawable.hotel_image_placeholder);
            }

            // Button listeners
            btnConfirm.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onConfirmBooking(booking);
                }
            });

            btnCancel.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onCancelBooking(booking);
                }
            });
        }
    }
}