package com.example.pelagiahotelapp.Adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pelagiahotelapp.ModelClasses.Booking;
import com.example.pelagiahotelapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private static Context context;
    private Map<String, String> userNamesMap; // userId -> userName
    private Map<String, String> userEmailsMap; // userId -> userEmail

    public BookingAdapter(List<Booking> bookingList, Context context) {
        this.bookingList = bookingList;
        this.context = context;
        this.userNamesMap = new HashMap<>();
        this.userEmailsMap = new HashMap<>();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_admin, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.bind(booking, userNamesMap, userEmailsMap);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public void updateData(List<Booking> newBookingList) {
        this.bookingList = newBookingList;
        notifyDataSetChanged();
    }

    // Method to update user data
    public void updateUserData(Map<String, String> userNamesMap, Map<String, String> userEmailsMap) {
        this.userNamesMap = userNamesMap != null ? userNamesMap : new HashMap<>();
        this.userEmailsMap = userEmailsMap != null ? userEmailsMap : new HashMap<>();
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvGuestName, tvGuestEmail, tvCheckIn, tvCheckOut, tvGuests, tvTotalPrice, tvStatus;
        private ImageView ivStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGuestName = itemView.findViewById(R.id.tvGuestName);
            tvGuestEmail = itemView.findViewById(R.id.tvGuestEmail);
            tvCheckIn = itemView.findViewById(R.id.tvCheckIn);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOut);
            tvGuests = itemView.findViewById(R.id.tvGuests);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivStatus = itemView.findViewById(R.id.ivStatus);
        }

        public void bind(Booking booking, Map<String, String> userNamesMap, Map<String, String> userEmailsMap) {
            // Get user data from maps
            String userName = "Guest";
            String userEmail = "No email provided";

            if (booking.getUserId() != null) {
                if (userNamesMap.containsKey(booking.getUserId())) {
                    userName = userNamesMap.get(booking.getUserId());
                }
                if (userEmailsMap.containsKey(booking.getUserId())) {
                    userEmail = userEmailsMap.get(booking.getUserId());
                }
            }

            // Display user name and email
            tvGuestName.setText(userName);
            tvGuestEmail.setText(userEmail);

            // Date information
            if (booking.getCheckInDate() != null) {
                tvCheckIn.setText("Check-in: " + booking.getCheckInDate());
            }

            if (booking.getCheckOutDate() != null) {
                tvCheckOut.setText("Check-out: " + booking.getCheckOutDate());
            }

            // Guests and nights
            tvGuests.setText(booking.getGuests() + " guest" + (booking.getGuests() > 1 ? "s" : ""));

            TextView tvNights = itemView.findViewById(R.id.tvNights);
            if (tvNights != null) {
                tvNights.setText(booking.getNights() + " night" + (booking.getNights() > 1 ? "s" : ""));
            }

            // Price
            tvTotalPrice.setText(String.format("$%.2f", booking.getTotalPrice()));

            // Status with dynamic colors
            String status = booking.getStatus() != null ? booking.getStatus() : "confirmed";
            tvStatus.setText(status.toUpperCase());

            // Set status-specific colors
            int statusColor;
            switch (status.toLowerCase()) {
                case "confirmed":
                    statusColor = ContextCompat.getColor(context, R.color.green);
                    break;
                case "pending":
                    statusColor = ContextCompat.getColor(context, R.color.orange);
                    break;
                case "cancelled":
                    statusColor = ContextCompat.getColor(context, R.color.red);
                    break;
                default:
                    statusColor = ContextCompat.getColor(context, R.color.gray);
            }

            // Update status background color
            GradientDrawable statusBackground = (GradientDrawable) tvStatus.getBackground();
            if (statusBackground != null) {
                statusBackground.setColor(statusColor);
            }

            // Status icon
            int statusIcon;
            switch (status.toLowerCase()) {
                case "confirmed":
                    statusIcon = R.drawable.ic_confirmed;
                    break;
                case "pending":
                    statusIcon = R.drawable.ic_pending;
                    break;
                case "cancelled":
                    statusIcon = R.drawable.ic_delete;
                    break;
                default:
                    statusIcon = R.drawable.ic_no_info;
            }
            ivStatus.setImageResource(statusIcon);
            ivStatus.setColorFilter(statusColor);

            // Optional: Booking date
            TextView tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            if (tvBookingDate != null && booking.getBookingDate() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                String bookingDate = sdf.format(new Date(booking.getBookingDate()));
                tvBookingDate.setText("Booked: " + bookingDate);
                tvBookingDate.setVisibility(View.VISIBLE);
            }
        }    }
}