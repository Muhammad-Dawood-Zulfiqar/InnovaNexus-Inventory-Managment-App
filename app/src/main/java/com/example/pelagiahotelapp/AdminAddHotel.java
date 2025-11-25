package com.example.pelagiahotelapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.card.MaterialCardView; // Added
import com.google.android.material.materialswitch.MaterialSwitch; // Added
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAddHotel extends AppCompatActivity {

    // UI Components
    private EditText etHotelName, etCity, etCountry,
            etPrice, etBeds, etWashrooms, etTotalRooms, etRating, etDescription;
    private AutoCompleteTextView etCategory;

    // Changed from CheckBox to MaterialSwitch to match new XML
    private MaterialSwitch switchWifi, switchGaming, switchPopular;

    private Button btnAddHotel;
    private ImageView ivHotelImage;
    private MaterialCardView cardImageSelect; // Added to handle image click

    private Uri imageUri;
    private boolean isImageSelected = false;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;

    private static final String TAG = "AdminAddHotel";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_hotel); // Ensure this matches your XML filename

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initViews();
        setupCategoryDropdown();

        setupClickListeners();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        imageUri = result.getData().getData();
                        ivHotelImage.setImageURI(imageUri);
                        isImageSelected = true;
                        Log.d(TAG, "Image selected: " + imageUri.toString());
                    }
                });
    }

    private void initViews() {
        // Basic Information
        etHotelName = findViewById(R.id.etHotelName);
        etCategory = findViewById(R.id.etCategory);
        etDescription = findViewById(R.id.etDescription);

        // Location Details
        etCity = findViewById(R.id.etCity);
        etCountry = findViewById(R.id.etCountry);

        // Pricing & Capacity
        etPrice = findViewById(R.id.etPrice);
        etBeds = findViewById(R.id.etBeds);
        etWashrooms = findViewById(R.id.etWashrooms);
        etTotalRooms = findViewById(R.id.etTotalRooms);

        // Rating
        etRating = findViewById(R.id.etRating);

        // Amenities Switches (Updated IDs to match new XML)
        switchWifi = findViewById(R.id.switchWifi);
        switchGaming = findViewById(R.id.switchGaming);
        switchPopular = findViewById(R.id.switchPopular);

        // Image Card and Button
        cardImageSelect = findViewById(R.id.cardImageSelect); // The card triggers the image picker now
        btnAddHotel = findViewById(R.id.btnAddHotel);
        ivHotelImage = findViewById(R.id.ivHotelImage);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        // Changed: Clicking the Card opens the picker
        cardImageSelect.setOnClickListener(v -> openImagePicker());
        btnAddHotel.setOnClickListener(v -> validateAndAddHotel());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Hotel Image"));
    }

    private void validateAndAddHotel() {
        // Basic Information
        String name = etHotelName.getText().toString().trim();
        String category = etCategory.getText().toString().trim(); // Case sensitive match for dropdown
        String description = etDescription.getText().toString().trim();

        // Location Details
        String city = etCity.getText().toString().trim();
        String country = etCountry.getText().toString().trim();
        String location = city + ", " + country; // Derived field

        // Pricing & Capacity
        String priceStr = etPrice.getText().toString().trim();
        String bedsStr = etBeds.getText().toString().trim();
        String washroomsStr = etWashrooms.getText().toString().trim();
        String totalRoomsStr = etTotalRooms.getText().toString().trim();

        // Rating
        String ratingStr = etRating.getText().toString().trim();

        // Amenities (Use isChecked() on Switches)
        boolean hasWifi = switchWifi.isChecked();
        boolean hasGaming = switchGaming.isChecked();
        boolean isPopular = switchPopular.isChecked();

        // Validation - Basic Information
        if (TextUtils.isEmpty(name)) {
            etHotelName.setError("Hotel name is required");
            etHotelName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(category)) {
            etCategory.setError("Category is required");
            etCategory.requestFocus();
            // Force show dropdown if empty
            etCategory.showDropDown();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }

        // Validation - Location Details
        if (TextUtils.isEmpty(city)) {
            etCity.setError("City is required");
            etCity.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(country)) {
            etCountry.setError("Country is required");
            etCountry.requestFocus();
            return;
        }

        // Validation - Pricing & Capacity (numeric fields)
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(bedsStr)) {
            etBeds.setError("Number of beds is required");
            etBeds.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(washroomsStr)) {
            etWashrooms.setError("Number of washrooms is required");
            etWashrooms.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(totalRoomsStr)) {
            etTotalRooms.setError("Total rooms is required");
            etTotalRooms.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(ratingStr)) {
            etRating.setError("Rating is required");
            etRating.requestFocus();
            return;
        }

        // Numeric validation
        double price, rating;
        int beds, washrooms, totalRooms;

        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                etPrice.setError("Price must be greater than 0");
                etPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            etPrice.requestFocus();
            return;
        }

        try {
            beds = Integer.parseInt(bedsStr);
            if (beds <= 0) {
                etBeds.setError("Beds must be greater than 0");
                etBeds.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etBeds.setError("Invalid beds format");
            etBeds.requestFocus();
            return;
        }

        try {
            washrooms = Integer.parseInt(washroomsStr);
            if (washrooms <= 0) {
                etWashrooms.setError("Washrooms must be greater than 0");
                etWashrooms.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etWashrooms.setError("Invalid washrooms format");
            etWashrooms.requestFocus();
            return;
        }

        try {
            totalRooms = Integer.parseInt(totalRoomsStr);
            if (totalRooms <= 0) {
                etTotalRooms.setError("Total rooms must be greater than 0");
                etTotalRooms.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etTotalRooms.setError("Invalid total rooms format");
            etTotalRooms.requestFocus();
            return;
        }

        try {
            rating = Double.parseDouble(ratingStr);
            if (rating < 0 || rating > 5) {
                etRating.setError("Rating must be between 0 and 5");
                etRating.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etRating.setError("Invalid rating format");
            etRating.requestFocus();
            return;
        }

        if (!isImageSelected) {
            Toast.makeText(this, "Please select a hotel image", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Starting hotel addition process");
        addHotelWithImage(name, category, description, city, country, location, price, beds, washrooms,
                totalRooms, rating, hasWifi, hasGaming, isPopular);
    }

    private void setupCategoryDropdown() {
        // Ensure these match your strings.xml or keeping them here is fine too
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

        // Set click listener to show dropdown when clicked
        etCategory.setOnClickListener(v -> etCategory.showDropDown());

        // Also show on focus
        etCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etCategory.showDropDown();
        });
    }

    private void addHotelWithImage(String name, String category, String description, String city, String country,
                                   String location, double price, int beds, int washrooms,
                                   int totalRooms, double rating, boolean wifi, boolean gaming,
                                   boolean popular) {
        progressDialog.setMessage("Uploading image...");
        progressDialog.show();

        Log.d(TAG, "Starting Cloudinary upload with URI: " + imageUri);

        try {
            MediaManager.get().upload(imageUri)
                    .option("folder", "hotels")
                    .option("public_id", "hotel_" + System.currentTimeMillis())
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "Cloudinary upload started: " + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            int progress = (int) ((bytes * 100) / totalBytes);
                            progressDialog.setMessage("Uploading image... " + progress + "%");
                            Log.d(TAG, "Upload progress: " + progress + "%");
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Log.d(TAG, "Cloudinary upload successful: " + requestId);

                            String imageUrl = (String) resultData.get("url");
                            if (imageUrl != null) {
                                if (imageUrl.startsWith("http://")) {
                                    imageUrl = imageUrl.replace("http://", "https://");
                                }
                                Log.d(TAG, "Image URL from Cloudinary: " + imageUrl);

                                // Need to run UI updates/Firestore on main thread usually,
                                // but Firestore handles background threads well.
                                saveHotelToFirestore(name, category, description, city, country, location,
                                        price, beds, washrooms, totalRooms, rating,
                                        wifi, gaming, popular, imageUrl);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(AdminAddHotel.this,
                                        "Image upload failed: No URL returned", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            progressDialog.dismiss();
                            Log.e(TAG, "Cloudinary error: " + error.getDescription());
                            Toast.makeText(AdminAddHotel.this,
                                    "Image upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            progressDialog.dismiss();
                            Toast.makeText(AdminAddHotel.this,
                                    "Image upload failed, please try again", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .dispatch();

        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "Exception during Cloudinary upload: " + e.getMessage(), e);
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveHotelToFirestore(String name, String category, String description, String city, String country,
                                      String location, double price, int beds, int washrooms,
                                      int totalRooms, double rating, boolean wifi, boolean gaming,
                                      boolean popular, String images) {

        // Just updating the message, dialog is already showing
        runOnUiThread(() -> progressDialog.setMessage("Saving hotel details..."));

        String hotelId = db.collection("hotels").document().getId();

        Map<String, Object> hotel = new HashMap<>();
        hotel.put("id", hotelId);
        hotel.put("name", name);
        hotel.put("category", category);
        hotel.put("description", description);
        hotel.put("location", location);
        hotel.put("price", price);
        hotel.put("beds", beds);
        hotel.put("washrooms", washrooms);
        hotel.put("totalRooms", totalRooms);
        hotel.put("rating", rating);
        hotel.put("wifi", wifi);
        hotel.put("gaming", gaming);
        hotel.put("popular", popular);
        hotel.put("images", images);
        hotel.put("createdAt", FieldValue.serverTimestamp());

        db.collection("hotels")
                .document(hotelId)
                .set(hotel)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminAddHotel.this, "Hotel added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AdminAddHotel.this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}