package com.example.pelagiahotelapp.Main;

import android.content.Intent;
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

import com.example.pelagiahotelapp.Adapters.HotelAdapter;
import com.example.pelagiahotelapp.Features.HotelBookingDetailActivity;
import com.example.pelagiahotelapp.ModelClasses.Hotel;
import com.example.pelagiahotelapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HotelBookingsFragment extends Fragment {

    private RecyclerView recyclerView;
    private HotelAdapter hotelAdapter;
    private List<Hotel> hotelList;
    private ProgressBar progressBar;
    private TextView tvNoHotels;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private static final String TAG = "HotelBookingsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        initViews(view);
        setupFirebase();
        loadAdminHotels();
    }

    private void initViews(View view) {
        Log.d(TAG, "Initializing views");

        recyclerView = view.findViewById(R.id.recyclerViewHotels);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoHotels = view.findViewById(R.id.tvNoHotels);

        // Check if views are found
        if (recyclerView == null) Log.e(TAG, "recyclerView is NULL!");
        if (progressBar == null) Log.e(TAG, "progressBar is NULL!");
        if (tvNoHotels == null) Log.e(TAG, "tvNoHotels is NULL!");

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        hotelList = new ArrayList<>();

        // Create click listener with logging
        HotelAdapter.OnHotelClickListener clickListener = new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelClick(Hotel hotel) {
                Log.d(TAG, ">>>>>> INSIDE onHotelClick! Hotel: " + hotel.getName() + " <<<<<<");
                openHotelDetails(hotel);
            }
        };

        Log.d(TAG, "ClickListener created: " + (clickListener != null));

        // Setup adapter
        hotelAdapter = new HotelAdapter(getContext(), hotelList, clickListener);
        recyclerView.setAdapter(hotelAdapter);

        Log.d(TAG, "Adapter setup complete. HotelList size: " + hotelList.size());
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "Firebase setup. Current user: " + (currentUser != null ? currentUser.getUid() : "null"));
    }

    private void loadAdminHotels() {
        Log.d(TAG, "loadAdminHotels called");

        if (currentUser == null) {
            Log.e(TAG, "Current user is null!");
            showNoHotelsMessage("Please login to view your hotels");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvNoHotels.setVisibility(View.GONE);

        String currentUserId = currentUser.getUid();
        Log.d(TAG, "Loading hotels for user: " + currentUserId);

        db.collection("hotels")
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "Firestore query completed. Success: " + task.isSuccessful());

                    if (task.isSuccessful()) {
                        hotelList.clear();
                        int documentCount = task.getResult() != null ? task.getResult().size() : 0;
                        Log.d(TAG, "Documents found: " + documentCount);

                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Hotel hotel = document.toObject(Hotel.class);
                                    hotel.setId(document.getId());
                                    hotelList.add(hotel);
                                    Log.d(TAG, "Added hotel: " + hotel.getName() + " ID: " + hotel.getId());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document: " + e.getMessage());
                                }
                            }

                            Log.d(TAG, "Calling adapter updateData with " + hotelList.size() + " hotels");
                            hotelAdapter.updateData(hotelList);

                            tvNoHotels.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                        } else {
                            Log.d(TAG, "No hotels found in Firestore");
                            showNoHotelsMessage("You haven't added any hotels yet");
                        }
                    } else {
                        Log.e(TAG, "Firestore error: " + task.getException());
                        showNoHotelsMessage("Error loading hotels: " + task.getException().getMessage());
                    }
                });
    }

    private void openHotelDetails(Hotel hotel) {
        Log.d(TAG, ">>>>>> openHotelDetails called for: " + hotel.getName() + " <<<<<<");

        if (hotel == null) {
            Log.e(TAG, "Hotel is null in openHotelDetails!");
            return;
        }

        try {
            Log.d(TAG, "Starting HotelBookingDetailActivity...");
            Intent intent = new Intent(getContext(), HotelBookingDetailActivity.class);
            intent.putExtra("hotel_id", hotel.getId());
            intent.putExtra("hotel_name", hotel.getName());
            intent.putExtra("hotel_location", hotel.getLocation());
            intent.putExtra("hotel_price", hotel.getPrice());
            startActivity(intent);

            Toast.makeText(getContext(), "Opening: " + hotel.getName(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Activity started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error starting activity: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showNoHotelsMessage(String message) {
        Log.d(TAG, "Showing message: " + message);
        tvNoHotels.setText(message);
        tvNoHotels.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        if (currentUser != null) {
            loadAdminHotels();
        }
    }
}