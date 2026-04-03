package com.example.pelagiahotelapp.Main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pelagiahotelapp.Adapters.AdminHotelAdapter;
import com.example.pelagiahotelapp.Features.AdminAddHotel;
import com.example.pelagiahotelapp.ModelClasses.Hotel;
import com.example.pelagiahotelapp.R;
import com.example.pelagiahotelapp.Features.UpdateActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HotelsEditFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private AdminHotelAdapter hotelAdapter;
    private List<Hotel> hotelList;
    private ProgressBar progressBar;
    private LinearLayout LLnoHotels;
    private ExtendedFloatingActionButton fabAddHotel;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private static final String TAG = "HotelsFragment";

    public HotelsEditFragment() {
        // Required empty public constructor
    }

    public static HotelsEditFragment newInstance(String param1, String param2) {
        HotelsEditFragment fragment = new HotelsEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hotels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupFirebase();
        setupClickListeners();
        loadHotels();
    }

    private void initViews(View v) {
        recyclerView = v.findViewById(R.id.recyclerViewHotels);
        progressBar = v.findViewById(R.id.progressBar);
        LLnoHotels = v.findViewById(R.id.LLnoHotels);
        fabAddHotel = v.findViewById(R.id.fabAddHotel);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        hotelList = new ArrayList<>();
        hotelAdapter = new AdminHotelAdapter(hotelList, this::onDeleteHotel, this::onUpdateHotel);
        recyclerView.setAdapter(hotelAdapter);
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void setupClickListeners() {
        fabAddHotel.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AdminAddHotel.class);
            startActivity(intent);
        });
    }

    private void loadHotels() {
        // Check if user is logged in
        if (currentUser == null) {
            showNoHotelsMessage("Please login to view your hotels");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        LLnoHotels.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        String currentUserId = currentUser.getUid();
        Log.d(TAG, "Loading hotels for user: " + currentUserId);

        // MODIFIED: Only get hotels where ownerId matches current user
        db.collection("hotels")
                .whereEqualTo("ownerId", currentUserId) // This line filters by owner
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
                                    Log.d(TAG, "Hotel loaded: " + hotel.getName() + " (Owner: " + hotel.getOwnerId() + ")");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document to Hotel: " + e.getMessage());
                                }
                            }

                            hotelAdapter.notifyDataSetChanged();
                            LLnoHotels.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                        } else {
                            Log.d(TAG, "No hotels found for current user");
                            showNoHotelsMessage("You haven't added any hotels yet. Tap the + button to add your first hotel!");
                        }
                    } else {
                        Log.e(TAG, "Error getting hotels: ", task.getException());
                        LLnoHotels.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });
    }

    private void showNoHotelsMessage() {
        showNoHotelsMessage("No hotels found. Add your first hotel!");
    }

    private void showNoHotelsMessage(String message) {
        
        LLnoHotels.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void onDeleteHotel(Hotel hotel) {
        // ADDED: Check if current user is the owner before allowing delete
        if (currentUser == null || !currentUser.getUid().equals(hotel.getOwnerId())) {
            Toast.makeText(getContext(), "You can only delete your own hotels", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Hotel")
                .setMessage("Are you sure you want to delete '" + hotel.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> deleteHotel(hotel))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onUpdateHotel(Hotel hotel) {
        // ADDED: Check if current user is the owner before allowing update
        if (currentUser == null || !currentUser.getUid().equals(hotel.getOwnerId())) {
            Toast.makeText(getContext(), "You can only update your own hotels", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireActivity(), UpdateActivity.class);

        intent.putExtra("hotel_id", hotel.getId());
        intent.putExtra("hotel_owner_id", hotel.getOwnerId());
        intent.putExtra("hotel_name", hotel.getName());
        intent.putExtra("hotel_location", hotel.getLocation());
        intent.putExtra("hotel_price", hotel.getPrice());
        intent.putExtra("hotel_beds", hotel.getBeds());
        intent.putExtra("hotel_washrooms", hotel.getWashrooms());
        intent.putExtra("hotel_wifi", hotel.isWifi());
        intent.putExtra("hotel_gaming", hotel.isGaming());
        intent.putExtra("hotel_total_rooms", hotel.getTotalRooms());
        intent.putExtra("hotel_popular", hotel.isPopular());
        intent.putExtra("hotel_category", hotel.getCategory());
        intent.putExtra("hotel_description", hotel.getDescription());

        startActivity(intent);
    }

    private void deleteHotel(Hotel hotel) {
        // 1. Ownership Check
        if (currentUser == null || !currentUser.getUid().equals(hotel.getOwnerId())) {
            Toast.makeText(getContext(), "You can only delete your own hotels", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // 2. Prepare Date Logic
        // Make sure this pattern matches your Firestore date string exactly ("yyyy-MM-dd")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();

        // Normalize 'today' to remove time (hours/minutes) for accurate comparison
        try {
            today = sdf.parse(sdf.format(today));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final Date finalToday = today; // Needed for lambda

        // 3. CHECK FOR ACTIVE BOOKINGS FIRST
        db.collection("bookings")
                .whereEqualTo("hotelId", hotel.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean hasActiveBooking = false;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String checkInStr = doc.getString("checkInDate");
                        String status = doc.getString("status");

                        // Skip if booking is already cancelled
                        if (status != null && status.equalsIgnoreCase("cancelled")) {
                            continue;
                        }

                        if (checkInStr != null) {
                            try {
                                Date checkInDate = sdf.parse(checkInStr);

                                // If checkInDate is Today or After Today
                                if (checkInDate != null && !checkInDate.before(finalToday)) {
                                    hasActiveBooking = true;
                                    break; // Found one, no need to check the rest
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (hasActiveBooking) {
                        // STOP: Do not delete
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                                "Cannot delete: This hotel has upcoming bookings.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        // PROCEED: No active bookings found, delete the hotel
                        performActualDelete(hotel);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error checking bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 4. Helper method for the actual deletion (Moved original logic here)
    private void performActualDelete(Hotel hotel) {
        db.collection("hotels")
                .document(hotel.getId())
                .delete()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Hotel deleted successfully", Toast.LENGTH_SHORT).show();

                        // Remove from list and update UI
                        hotelList.remove(hotel);
                        hotelAdapter.notifyDataSetChanged();

                        if (hotelList.isEmpty()) {
                            showNoHotelsMessage("You haven't added any hotels yet. Tap the + button to add your first hotel!");
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Failed to delete hotel: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
    @Override
    public void onResume() {
        super.onResume();
        // Reload hotels when fragment becomes visible again
        loadHotels();
    }
}