package com.example.pelagiahotelapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminHotelAdapter hotelAdapter;
    private List<Hotel> hotelList;
    private ProgressBar progressBar;
    private TextView tvNoHotels;
    private FloatingActionButton fabAddHotel;
    private FirebaseFirestore db;

    private static final String TAG = "AdminMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initViews();
        setupClickListeners();
        loadHotels();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewHotels);
        progressBar = findViewById(R.id.progressBar);
        tvNoHotels = findViewById(R.id.tvNoHotels);
        fabAddHotel = findViewById(R.id.fabAddHotel);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        hotelList = new ArrayList<>();
        hotelAdapter = new AdminHotelAdapter(hotelList, this::onDeleteHotel);
        recyclerView.setAdapter(hotelAdapter);
    }

    private void setupClickListeners() {
        fabAddHotel.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminAddHotel.class);
            startActivity(intent);
        });
    }

    private void loadHotels() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoHotels.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        Log.d(TAG, "Loading hotels from Firestore");

        db.collection("hotels")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        hotelList.clear();
                        QuerySnapshot querySnapshot = task.getResult();

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            Log.d(TAG, "Hotels found: " + querySnapshot.size());

                            for (QueryDocumentSnapshot document : querySnapshot) {
                                try {
                                    Hotel hotel = document.toObject(Hotel.class);
                                    hotel.setId(document.getId());
                                    hotelList.add(hotel);
                                    Log.d(TAG, "Hotel loaded: " + hotel.getName());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document to Hotel: " + e.getMessage());
                                }
                            }

                            hotelAdapter.notifyDataSetChanged();
                            tvNoHotels.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                        } else {
                            Log.d(TAG, "No hotels found in Firestore");
                            showNoHotelsMessage();
                        }
                    } else {
                        Log.e(TAG, "Error getting hotels: ", task.getException());
                        tvNoHotels.setText("Error loading hotels: " + task.getException().getMessage());
                        tvNoHotels.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });
    }

    private void showNoHotelsMessage() {
        tvNoHotels.setText("No hotels found. Add your first hotel!");
        tvNoHotels.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void onDeleteHotel(Hotel hotel) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hotel")
                .setMessage("Are you sure you want to delete '" + hotel.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> deleteHotel(hotel))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteHotel(Hotel hotel) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("hotels")
                .document(hotel.getId())
                .delete()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this,
                                "Hotel deleted successfully", Toast.LENGTH_SHORT).show();
                        // Remove from list and update UI
                        hotelList.remove(hotel);
                        hotelAdapter.notifyDataSetChanged();

                        if (hotelList.isEmpty()) {
                            showNoHotelsMessage();
                        }
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Failed to delete hotel: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload hotels when returning from AddHotel activity
        loadHotels();
    }
}