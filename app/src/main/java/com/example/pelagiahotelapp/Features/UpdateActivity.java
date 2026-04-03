package com.example.pelagiahotelapp.Features;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.pelagiahotelapp.Adapters.CurrentImagesAdapter;
import com.example.pelagiahotelapp.Adapters.SelectedImagesAdapter;
import com.example.pelagiahotelapp.HelperActivities.LocationPicker;
import com.example.pelagiahotelapp.ModelClasses.Hotel;
import com.example.pelagiahotelapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText etHotelName, etPrice, etBeds,
            etWashrooms, etTotalRooms, etDescription;

    // Location UI
    private TextInputEditText etCity, etCountry, etAddress;
    private MaterialButton btnSelectLocation;

    private AutoCompleteTextView etCategory;
    private MaterialSwitch switchWifi, switchGaming;
    private MaterialButton btnUpdateHotel;
    private MaterialCardView cardImageSelect;
    private RecyclerView rvCurrentImages, rvNewImages;

    // Data
    private Hotel currentHotel;
    private List<String> currentImageUrls = new ArrayList<>();
    private List<Uri> newImageUris = new ArrayList<>();
    private CurrentImagesAdapter currentImagesAdapter;
    private SelectedImagesAdapter newImagesAdapter;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;

    // Location Data Storage
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private String selectedAddressString = "";

    private static final String TAG = "UpdateActivity";

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> locationPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get hotel data from intent
        getHotelDataFromIntent();

        // Initialize views
        initViews();
        setupCategoryDropdown();
        setupRecyclerViews();
        setupClickListeners();

        // Populate data after views are initialized
        populateHotelData();

        // Image Picker Launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleImageSelection(result.getData());
                    }
                });

        // Location Picker Launcher
        locationPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        selectedLatitude = data.getDoubleExtra("latitude", 0.0);
                        selectedLongitude = data.getDoubleExtra("longitude", 0.0);
                        selectedAddressString = data.getStringExtra("address");
                        String city = data.getStringExtra("city");
                        String country = data.getStringExtra("country");

                        // Update UI
                        etAddress.setText(selectedAddressString);
                        etCity.setText(city);
                        etCountry.setText(country);
                    }
                });
    }

    private void getHotelDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            currentHotel = new Hotel();
            currentHotel.setId(intent.getStringExtra("hotel_id"));
            currentHotel.setOwnerId(intent.getStringExtra("hotel_owner_id"));
            currentHotel.setName(intent.getStringExtra("hotel_name"));

            // Map Location Data
            currentHotel.setLocation(intent.getStringExtra("hotel_location")); // Address
            currentHotel.setCity(intent.getStringExtra("hotel_city"));
            currentHotel.setCountry(intent.getStringExtra("hotel_country"));
            currentHotel.setLatitude(intent.getDoubleExtra("hotel_latitude", 0.0));
            currentHotel.setLongitude(intent.getDoubleExtra("hotel_longitude", 0.0));

            currentHotel.setPrice(intent.getDoubleExtra("hotel_price", 0.0));
            currentHotel.setBeds(intent.getIntExtra("hotel_beds", 0));
            currentHotel.setWashrooms(intent.getIntExtra("hotel_washrooms", 0));
            currentHotel.setWifi(intent.getBooleanExtra("hotel_wifi", false));
            currentHotel.setGaming(intent.getBooleanExtra("hotel_gaming", false));
            currentHotel.setTotalRooms(intent.getIntExtra("hotel_total_rooms", 0));
            currentHotel.setPopular(intent.getBooleanExtra("hotel_popular", false));
            currentHotel.setCategory(intent.getStringExtra("hotel_category"));
            currentHotel.setDescription(intent.getStringExtra("hotel_description"));

            // IMPORTANT: You need to pass the ArrayList of images from your adapter
            // using intent.putStringArrayListExtra("hotel_images", list);
            if(intent.hasExtra("hotel_images")){
                currentImageUrls = intent.getStringArrayListExtra("hotel_images");
                currentHotel.setImages(currentImageUrls);
            } else {
                currentImageUrls = new ArrayList<>();
            }
        }
    }

    private void initViews() {
        // Basic Information
        etHotelName = findViewById(R.id.etHotelName);
        etCategory = findViewById(R.id.etCategory);
        etDescription = findViewById(R.id.etDescription);

        // Location Details (Updated IDs to match modified XML)
        etCity = findViewById(R.id.etCity);
        etCountry = findViewById(R.id.etCountry);
        etAddress = findViewById(R.id.etAddress);
        btnSelectLocation = findViewById(R.id.btnSelectLocation);

        // Pricing & Capacity
        etPrice = findViewById(R.id.etPrice);
        etBeds = findViewById(R.id.etBeds);
        etWashrooms = findViewById(R.id.etWashrooms);
        etTotalRooms = findViewById(R.id.etTotalRooms);

        // Amenities Switches
        switchWifi = findViewById(R.id.switchWifi);
        switchGaming = findViewById(R.id.switchGaming);

        // Image Selection
        cardImageSelect = findViewById(R.id.cardImageSelect);
        btnUpdateHotel = findViewById(R.id.btnUpdateHotel);
        rvCurrentImages = findViewById(R.id.rvCurrentImages);
        rvNewImages = findViewById(R.id.rvNewImages);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);
    }

    private void setupRecyclerViews() {
        // Current Images RecyclerView
        rvCurrentImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        currentImagesAdapter = new CurrentImagesAdapter(currentImageUrls, position -> {
            currentImageUrls.remove(position);
            currentImagesAdapter.notifyDataSetChanged();
            updateCurrentImagesUI();
        });
        rvCurrentImages.setAdapter(currentImagesAdapter);

        // New Images RecyclerView
        rvNewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        newImagesAdapter = new SelectedImagesAdapter(newImageUris, position -> {
            newImageUris.remove(position);
            newImagesAdapter.notifyDataSetChanged();
            updateNewImagesUI();
        });
        rvNewImages.setAdapter(newImagesAdapter);
    }

    private void setupClickListeners() {
        cardImageSelect.setOnClickListener(v -> openImagePicker());

        // Open Location Picker
        btnSelectLocation.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateActivity.this, LocationPicker.class);
            locationPickerLauncher.launch(intent);
        });

        btnUpdateHotel.setOnClickListener(v -> validateAndUpdateHotel());
    }

    private void setupCategoryDropdown() {
        String[] categories = {
                "City", "Beach", "Mountain", "Village",
                "Luxury", "Resort", "Business", "Budget"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        etCategory.setAdapter(adapter);
        etCategory.setThreshold(1);
        etCategory.setOnClickListener(v -> etCategory.showDropDown());
        etCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etCategory.showDropDown();
        });
    }

    private void populateHotelData() {
        if (currentHotel != null) {
            etHotelName.setText(currentHotel.getName());

            if (currentHotel.getCategory() != null) {
                etCategory.setText(currentHotel.getCategory(), false);
            }

            etDescription.setText(currentHotel.getDescription());

            // Populate Location Data
            etCity.setText(currentHotel.getCity());
            etCountry.setText(currentHotel.getCountry());
            etAddress.setText(currentHotel.getLocation());

            // Set internal variables to existing data
            // (So if user clicks update without changing location, we keep the old one)
            selectedLatitude = currentHotel.getLatitude();
            selectedLongitude = currentHotel.getLongitude();
            selectedAddressString = currentHotel.getLocation();

            // Pricing & Capacity
            etPrice.setText(String.valueOf(currentHotel.getPrice()));
            etBeds.setText(String.valueOf(currentHotel.getBeds()));
            etWashrooms.setText(String.valueOf(currentHotel.getWashrooms()));
            etTotalRooms.setText(String.valueOf(currentHotel.getTotalRooms()));

            // Amenities
            switchWifi.setChecked(currentHotel.isWifi());
            switchGaming.setChecked(currentHotel.isGaming());

            updateCurrentImagesUI();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select New Hotel Images"));
    }

    private void handleImageSelection(Intent data) {
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                newImageUris.add(imageUri);
            }
        } else if (data.getData() != null) {
            Uri imageUri = data.getData();
            newImageUris.add(imageUri);
        }

        newImagesAdapter.notifyDataSetChanged();
        updateNewImagesUI();
    }

    private void updateCurrentImagesUI() {
        rvCurrentImages.setVisibility(currentImageUrls.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateNewImagesUI() {
        rvNewImages.setVisibility(newImageUris.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void validateAndUpdateHotel() {
        // Basic Information
        String name = etHotelName.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Location Details - Get from Views (which are populated by Picker or existing data)
        String city = etCity.getText().toString().trim();
        String country = etCountry.getText().toString().trim();
        // Use the selectedAddressString which holds the address
        String location = selectedAddressString.isEmpty() ? city + ", " + country : selectedAddressString;

        // Pricing & Capacity
        String priceStr = etPrice.getText().toString().trim();
        String bedsStr = etBeds.getText().toString().trim();
        String washroomsStr = etWashrooms.getText().toString().trim();
        String totalRoomsStr = etTotalRooms.getText().toString().trim();

        // Amenities
        boolean hasWifi = switchWifi.isChecked();
        boolean hasGaming = switchGaming.isChecked();
        boolean isPopular = false;

        // Validation
        if (TextUtils.isEmpty(name)) {
            etHotelName.setError("Required");
            etHotelName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(city) || TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Location details missing. Please select location.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure lat/long exists
        if(selectedLatitude == 0.0 && selectedLongitude == 0.0) {
            Toast.makeText(this, "Invalid location coordinates.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Numeric validation
        double price;
        int beds, washrooms, totalRooms;

        try {
            price = Double.parseDouble(priceStr);
            beds = Integer.parseInt(bedsStr);
            washrooms = Integer.parseInt(washroomsStr);
            totalRooms = Integer.parseInt(totalRoomsStr);


        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please check all numeric fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentImageUrls.isEmpty() && newImageUris.isEmpty()) {
            Toast.makeText(this, "Hotel must have at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Starting hotel update process");

        // Pass lat/long to update method
        updateHotelWithImages(name, category, description, city, country, location,
                selectedLatitude, selectedLongitude, price, beds, washrooms,
                totalRooms, hasWifi, hasGaming, isPopular);
    }

    private void updateHotelWithImages(String name, String category, String description, String city, String country,
                                       String location, double latitude, double longitude,
                                       double price, int beds, int washrooms,
                                       int totalRooms,boolean wifi, boolean gaming,
                                       boolean popular) {

        if (newImageUris.isEmpty()) {
            // No new images, just update the hotel data
            updateHotelInFirestore(name, category, description, city, country, location,
                    latitude, longitude, price, beds, washrooms,
                    totalRooms,  wifi, gaming, popular, currentImageUrls);
        } else {
            // Upload new images first
            progressDialog.setMessage("Uploading new images...");
            progressDialog.show();

            List<String> newImageUrls = new ArrayList<>();
            AtomicInteger uploadCount = new AtomicInteger(0);
            int totalNewImages = newImageUris.size();

            for (int i = 0; i < newImageUris.size(); i++) {
                Uri imageUri = newImageUris.get(i);
                final int currentIndex = i;

                try {
                    MediaManager.get().upload(imageUri)
                            .option("folder", "hotels")
                            .option("public_id", "hotel_" + System.currentTimeMillis() + "_" + currentIndex)
                            .callback(new UploadCallback() {
                                @Override
                                public void onStart(String requestId) { }

                                @Override
                                public void onProgress(String requestId, long bytes, long totalBytes) { }

                                @Override
                                public void onSuccess(String requestId, Map resultData) {
                                    String imageUrl = (String) resultData.get("url");
                                    if (imageUrl != null) {
                                        if (imageUrl.startsWith("http://")) {
                                            imageUrl = imageUrl.replace("http://", "https://");
                                        }
                                        newImageUrls.add(imageUrl);
                                    }

                                    int completed = uploadCount.incrementAndGet();
                                    if (completed == totalNewImages) {
                                        // All new images uploaded, combine with existing and update
                                        List<String> allImageUrls = new ArrayList<>(currentImageUrls);
                                        allImageUrls.addAll(newImageUrls);

                                        runOnUiThread(() -> {
                                            progressDialog.setMessage("Updating hotel details...");
                                            updateHotelInFirestore(name, category, description, city, country, location,
                                                    latitude, longitude, price, beds, washrooms, totalRooms,
                                                    wifi, gaming, popular, allImageUrls);
                                        });
                                    }
                                }

                                @Override
                                public void onError(String requestId, ErrorInfo error) {
                                    progressDialog.dismiss();
                                    runOnUiThread(() -> Toast.makeText(UpdateActivity.this,
                                            "Image upload failed", Toast.LENGTH_SHORT).show());
                                }

                                @Override
                                public void onReschedule(String requestId, ErrorInfo error) { }
                            })
                            .dispatch();

                } catch (Exception e) {
                    progressDialog.dismiss();
                    return;
                }
            }
        }
    }

    private void updateHotelInFirestore(String name, String category, String description, String city, String country,
                                        String location, double latitude, double longitude,
                                        double price, int beds, int washrooms,
                                        int totalRooms, boolean wifi, boolean gaming,
                                        boolean popular, List<String> imageUrls) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("category", category);
        updates.put("description", description);
        updates.put("city", city);
        updates.put("country", country);
        updates.put("location", location);

        // Add Latitude and Longitude to updates
        updates.put("latitude", latitude);
        updates.put("longitude", longitude);

        updates.put("price", price);
        updates.put("beds", beds);
        updates.put("washrooms", washrooms);
        updates.put("totalRooms", totalRooms);
        updates.put("wifi", wifi);
        updates.put("gaming", gaming);
        updates.put("popular", popular);
        updates.put("images", imageUrls);
        // Do not update createdAt, usually you add "updatedAt" here if you want

        db.collection("hotels")
                .document(currentHotel.getId())
                .update(updates)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(UpdateActivity.this, "Hotel updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(UpdateActivity.this, "Update failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}