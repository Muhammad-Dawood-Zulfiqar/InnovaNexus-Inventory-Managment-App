package com.example.pelagiahotelapp.Features;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pelagiahotelapp.Adapters.BookingAdapter;
import com.example.pelagiahotelapp.ModelClasses.Booking;
import com.example.pelagiahotelapp.ModelClasses.Hotel;
import com.example.pelagiahotelapp.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HotelBookingDetailActivity extends AppCompatActivity {
    private Map<String, String> userNamesMap = new HashMap<>();
    private Map<String, String> userEmailsMap = new HashMap<>();
    private MaterialToolbar toolbar;
    private ImageView ivHotelImage;
    private TextView tvHotelName, tvHotelLocation, tvTotalBookings, tvBookingCount, tvNoBookings;
    private RecyclerView rvBookings;
    private ProgressBar progressBar;

    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;

    private String hotelId, hotelName, hotelLocation, hotelImage;
    private int totalBookings = 0;

    private static final String TAG = "HotelBookingDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_booking_detail);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get hotel data from intent
        getHotelDataFromIntent();

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Load hotel details and bookings
        loadHotelDetails();
        loadBookingsWithUserData();





    }

    private void getHotelDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            hotelId = intent.getStringExtra("hotel_id");
            Log.d(TAG, "Received hotel ID: " + hotelId);
        } else {
            Toast.makeText(this, "Error: No hotel data received", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivHotelImage = findViewById(R.id.ivHotelImage);
        tvHotelName = findViewById(R.id.tvHotelName);
        tvHotelLocation = findViewById(R.id.tvHotelLocation);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        tvBookingCount = findViewById(R.id.tvBookingCount);
        tvNoBookings = findViewById(R.id.tvNoBookings);
        rvBookings = findViewById(R.id.rvBookings);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookingList, this);
        rvBookings.setAdapter(bookingAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadHotelDetails() {
        if (hotelId == null || hotelId.isEmpty()) {
            return;
        }

        // Fetch hotel details from Firestore
        db.collection("hotels")
                .document(hotelId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Hotel hotel = task.getResult().toObject(Hotel.class);
                        if (hotel != null) {
                            hotelName = hotel.getName();
                            hotelLocation = hotel.getLocation();
                            if (hotel.getImages() != null && !hotel.getImages().isEmpty()) {
                                hotelImage = hotel.getImages().get(0);
                            }

                            // Update UI with hotel details
                            updateHotelUI(hotel);
                        }
                    } else {
                        Log.e(TAG, "Error loading hotel details: ", task.getException());
                    }
                });
    }

    private void updateHotelUI(Hotel hotel) {
        tvHotelName.setText(hotel.getName());
        tvHotelLocation.setText(hotel.getLocation());

        // Load hotel image
        if (hotel.getImages() != null && !hotel.getImages().isEmpty()) {
            Glide.with(this)
                    .load(hotel.getImages().get(0))
                    .placeholder(R.drawable.hotel_image_placeholder)
                    .error(R.drawable.hotel_image_placeholder)
                    .into(ivHotelImage);
        }
    }

    private void loadBookings() {
        if (hotelId == null || hotelId.isEmpty()) {
            showNoBookings();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvBookings.setVisibility(GONE);
        tvNoBookings.setVisibility(GONE);

        Log.d(TAG, "Loading bookings for hotel: " + hotelId);

        // Query bookings for this hotel, ordered by booking date (newest first)
        db.collection("bookings")
                .whereEqualTo("hotelId", hotelId)
                .orderBy("bookingDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(GONE);

                    if (task.isSuccessful()) {
                        bookingList.clear();
                        totalBookings = 0;

                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            Log.d(TAG, "Bookings found: " + task.getResult().size());

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Booking booking = document.toObject(Booking.class);
                                    booking.setBookingId(document.getId());
                                    bookingList.add(booking);
                                    totalBookings++;
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting booking document: " + e.getMessage());
                                }
                            }

                            updateBookingsUI();
                            bookingAdapter.updateData(bookingList);
                            rvBookings.setVisibility(View.VISIBLE);
                            tvNoBookings.setVisibility(GONE);

                        } else {
                            Log.d(TAG, "No bookings found for this hotel");
                            showNoBookings();
                        }
                    } else {
                        Log.e(TAG, "Error loading bookings: ", task.getException());
                        Toast.makeText(this, "Error loading bookings", Toast.LENGTH_SHORT).show();
                        showNoBookings();
                    }
                });
    }

    private void updateBookingsUI() {
        // Update booking count
        tvTotalBookings.setText(totalBookings + " booking" + (totalBookings != 1 ? "s" : ""));
        tvBookingCount.setText(String.valueOf(totalBookings));
        progressBar.setVisibility(GONE);
        // Update toolbar subtitle with count
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(totalBookings + " booking" + (totalBookings != 1 ? "s" : ""));
        }
    }

    private void showNoBookings() {
        rvBookings.setVisibility(GONE);
        tvNoBookings.setVisibility(View.VISIBLE);
        tvTotalBookings.setText("No bookings");
        tvBookingCount.setText("0");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle("No bookings");
        }
    }
    private void fetchUserData(List<String> userIds) {
        Log.d(TAG, "Fetching user data for " + userIds.size() + " users: " + userIds);

        if (userIds.isEmpty()) {
            Log.d(TAG, "No user IDs to fetch");
            bookingAdapter.updateUserData(userNamesMap, userEmailsMap);
            bookingAdapter.updateData(bookingList);
            updateBookingsUI();
            rvBookings.setVisibility(View.VISIBLE);
            progressBar.setVisibility(GONE);
            return;
        }

        userNamesMap.clear();
        userEmailsMap.clear();

        // Since your users collection uses document ID as userId, we'll fetch each document directly
        final int totalUsers = userIds.size();
        final AtomicInteger completedFetches = new AtomicInteger(0);

        for (String userId : userIds) {
            Log.d(TAG, "Fetching user document for ID: " + userId);

            db.collection("users")
                    .document(userId) // Use userId as the document ID
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            DocumentSnapshot userDoc = task.getResult();
                            Log.d(TAG, "User document data: " + userDoc.getData());

                            String userName = userDoc.getString("name");
                            String userEmail = userDoc.getString("email");

                            if (userName != null && !userName.isEmpty()) {
                                userNamesMap.put(userId, userName);
                                Log.d(TAG, "✓ Found username: " + userName + " for userId: " + userId);
                            } else {
                                userNamesMap.put(userId, "Guest");
                                Log.w(TAG, "✗ No username found for userId: " + userId);
                            }

                            if (userEmail != null && !userEmail.isEmpty()) {
                                userEmailsMap.put(userId, userEmail);
                                Log.d(TAG, "✓ Found email: " + userEmail + " for userId: " + userId);
                            } else {
                                userEmailsMap.put(userId, "No email provided");
                                Log.w(TAG, "✗ No email found for userId: " + userId);
                            }
                        } else {
                            Log.e(TAG, "Failed to fetch user document for userId: " + userId);
                            if (task.getException() != null) {
                                Log.e(TAG, "Error: " + task.getException().getMessage());
                            }
                            // Set default values if fetch fails
                            userNamesMap.put(userId, "Guest");
                            userEmailsMap.put(userId, "No email provided");
                        }

                        // Check if all fetches are complete
                        int completed = completedFetches.incrementAndGet();
                        Log.d(TAG, "Completed " + completed + "/" + totalUsers + " user fetches");

                        if (completed == totalUsers) {
                            // All user fetches completed, update UI
                            runOnUiThread(() -> {
                                Log.d(TAG, "All user fetches completed. Updating UI...");
                                Log.d(TAG, "Final userNamesMap: " + userNamesMap);
                                Log.d(TAG, "Final userEmailsMap: " + userEmailsMap);

                                bookingAdapter.updateUserData(userNamesMap, userEmailsMap);
                                bookingAdapter.updateData(bookingList);
                                updateBookingsUI();
                                rvBookings.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(GONE);
                            });
                        }
                    });
        }
    }
    private void loadBookingsWithUserData() {
        if (hotelId == null || hotelId.isEmpty()) {
            showNoBookings();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvBookings.setVisibility(GONE);
        tvNoBookings.setVisibility(GONE);

        Log.d(TAG, "Loading bookings for hotel: " + hotelId);

        // Query bookings for this hotel, ordered by booking date (newest first)
        db.collection("bookings")
                .whereEqualTo("hotelId", hotelId)
                .orderBy("bookingDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookingList.clear();
                        totalBookings = 0;

                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            Log.d(TAG, "Bookings found: " + task.getResult().size());

                            List<String> userIds = new ArrayList<>();

                            // First, collect all bookings and user IDs
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Booking booking = document.toObject(Booking.class);
                                    booking.setBookingId(document.getId());
                                    bookingList.add(booking);
                                    totalBookings++;

                                    // Collect user IDs for fetching user data
                                    if (booking.getUserId() != null) {
                                        Log.d(TAG, "Found booking with userId: " + booking.getUserId());
                                        if (!userIds.contains(booking.getUserId())) {
                                            userIds.add(booking.getUserId());
                                        }
                                    } else {
                                        Log.e(TAG, "Booking has null userId: " + document.getId());
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting booking document: " + e.getMessage());
                                }
                            }

                            Log.d(TAG, "Collected " + userIds.size() + " unique user IDs: " + userIds);

                            // Fetch user data for all collected user IDs
                            if (!userIds.isEmpty()) {
                                fetchUserData(userIds);
                            } else {
                                // No users to fetch, just show bookings
                                Log.d(TAG, "No user IDs found in bookings");
                                updateBookingsUI();
                                bookingAdapter.updateData(bookingList);
                                rvBookings.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(GONE);
                            }

                        } else {
                            Log.d(TAG, "No bookings found for this hotel");
                            showNoBookings();
                        }
                    } else {
                        Log.e(TAG, "Error loading bookings: ", task.getException());
                        Toast.makeText(this, "Error loading bookings", Toast.LENGTH_SHORT).show();
                        showNoBookings();
                    }
                });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hotel_bookings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            loadBookings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}