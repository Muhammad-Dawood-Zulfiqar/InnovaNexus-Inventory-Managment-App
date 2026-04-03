package com.example.pelagiahotelapp.Main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pelagiahotelapp.Adapters.RequestAdapter;
import com.example.pelagiahotelapp.ModelClasses.Booking;
import com.example.pelagiahotelapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RequestFragment extends Fragment {

    private RecyclerView rvRequests;
    private RequestAdapter requestAdapter;
    private List<Booking> requestList;
    private ProgressBar progressBar;
    private View emptyState;
    private TextView tvRequestCount;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private static final String TAG = "RequestFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupFirebase();
        loadPendingRequests();
    }

    private void initViews(View view) {
        rvRequests = view.findViewById(R.id.rvRequests);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        tvRequestCount = view.findViewById(R.id.tvRequestCount);

        // Setup RecyclerView
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        requestList = new ArrayList<>();

        requestAdapter = new RequestAdapter(requestList, new RequestAdapter.RequestActionListener() {
            @Override
            public void onConfirmBooking(Booking booking) {
                confirmBooking(booking);
            }

            @Override
            public void onCancelBooking(Booking booking) {
                cancelBooking(booking);
            }
        });
        rvRequests.setAdapter(requestAdapter);
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void loadPendingRequests() {
        if (currentUser == null) {
            showEmptyState();
            Toast.makeText(getContext(), "Please login to view requests", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        String currentUserId = currentUser.getUid();

        Log.d(TAG, "Loading pending booking requests for admin: " + currentUserId);

        // First, get all hotels owned by this admin
        db.collection("hotels")
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .addOnCompleteListener(hotelTask -> {
                    if (hotelTask.isSuccessful() && hotelTask.getResult() != null) {
                        List<String> hotelIds = new ArrayList<>();

                        for (QueryDocumentSnapshot hotelDoc : hotelTask.getResult()) {
                            hotelIds.add(hotelDoc.getId());
                        }

                        if (hotelIds.isEmpty()) {
                            progressBar.setVisibility(View.GONE);
                            showEmptyState();
                            return;
                        }

                        // Now get only PENDING bookings for these hotels
                        db.collection("bookings")
                                .whereIn("hotelId", hotelIds)
                                .whereEqualTo("status", "pending") // ONLY PENDING REQUESTS
                                .get()
                                .addOnCompleteListener(bookingTask -> {
                                    progressBar.setVisibility(View.GONE);

                                    if (bookingTask.isSuccessful()) {
                                        requestList.clear();

                                        if (bookingTask.getResult() != null && !bookingTask.getResult().isEmpty()) {
                                            Log.d(TAG, "Pending bookings found: " + bookingTask.getResult().size());

                                            for (QueryDocumentSnapshot document : bookingTask.getResult()) {
                                                try {
                                                    Booking booking = document.toObject(Booking.class);
                                                    booking.setBookingId(document.getId());
                                                    requestList.add(booking);
                                                    Log.d(TAG, "Pending booking loaded for hotel: " + booking.getHotelName());
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error converting booking document: " + e.getMessage());
                                                }
                                            }

                                            requestAdapter.updateData(requestList);
                                            updateUI();

                                        } else {
                                            Log.d(TAG, "No pending bookings found for admin's hotels");
                                            showEmptyState();
                                        }
                                    } else {
                                        Log.e(TAG, "Error loading pending bookings: ", bookingTask.getException());
                                        Toast.makeText(getContext(), "Error loading requests", Toast.LENGTH_SHORT).show();
                                        showEmptyState();
                                    }
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        showEmptyState();
                        Log.e(TAG, "Error loading admin hotels: ", hotelTask.getException());
                    }
                });
    }

    private void updateUI() {
        // Update request count
        int pendingCount = requestList.size();
        tvRequestCount.setText(String.valueOf(pendingCount));

        // Show/hide empty state
        if (pendingCount == 0) {
            showEmptyState();
        } else {
            rvRequests.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        rvRequests.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        tvRequestCount.setText("0");
    }

    private void confirmBooking(Booking booking) {
        if (booking.getBookingId() == null) {
            Toast.makeText(getContext(), "Error: Invalid booking", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Confirming booking: " + booking.getBookingId());

        // Update booking status to confirmed
        db.collection("bookings")
                .document(booking.getBookingId())
                .update("status", "confirmed")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Booking confirmed: " + booking.getBookingId());
                        Toast.makeText(getContext(), "Booking confirmed successfully", Toast.LENGTH_SHORT).show();

                        // Remove from local list and update UI
                        requestList.remove(booking);
                        requestAdapter.updateData(requestList);
                        updateUI();

                    } else {
                        Log.e(TAG, "Error confirming booking: ", task.getException());
                        Toast.makeText(getContext(), "Failed to confirm booking", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancelBooking(Booking booking) {
        if (booking.getBookingId() == null) {
            Toast.makeText(getContext(), "Error: Invalid booking", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Cancelling booking: " + booking.getBookingId());

        // Update booking status to cancelled
        db.collection("bookings")
                .document(booking.getBookingId())
                .update("status", "cancelled")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Booking cancelled: " + booking.getBookingId());
                        Toast.makeText(getContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();

                        // Remove from local list and update UI
                        requestList.remove(booking);
                        requestAdapter.updateData(requestList);
                        updateUI();

                    } else {
                        Log.e(TAG, "Error cancelling booking: ", task.getException());
                        Toast.makeText(getContext(), "Failed to cancel booking", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload requests when fragment becomes visible
        if (currentUser != null) {
            loadPendingRequests();
        }
    }
}