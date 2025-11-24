package com.example.pelagiahotelapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAddHotel extends AppCompatActivity {

    private EditText etHotelName, etCategory, etCity, etCountry, etLocation,
            etPrice, etBeds, etWashrooms, etTotalRooms, etRating, etDescription;
    private CheckBox cbWifi, cbGaming, cbPopular;
    private Button btnSelectImage, btnAddHotel;
    private ImageView ivHotelImage;

    private Uri imageUri;
    private boolean isImageSelected = false;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;

    private static final String TAG = "AdminAddHotel";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_hotel);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initViews();
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
        etLocation = findViewById(R.id.etLocation);

        // Pricing & Capacity
        etPrice = findViewById(R.id.etPrice);
        etBeds = findViewById(R.id.etBeds);
        etWashrooms = findViewById(R.id.etWashrooms);
        etTotalRooms = findViewById(R.id.etTotalRooms);

        // Rating
        etRating = findViewById(R.id.etRating);

        // Amenities Checkboxes
        cbWifi = findViewById(R.id.cbWifi);
        cbGaming = findViewById(R.id.cbGaming);
        cbPopular = findViewById(R.id.cbPopular);

        // Image and Button
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnAddHotel = findViewById(R.id.btnAddHotel);
        ivHotelImage = findViewById(R.id.ivHotelImage);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        btnSelectImage.setOnClickListener(v -> openImagePicker());
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
        String category = etCategory.getText().toString().trim().toUpperCase();
        String description = etDescription.getText().toString().trim();

        // Location Details
        String city = etCity.getText().toString().trim();
        String country = etCountry.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        // Pricing & Capacity
        String priceStr = etPrice.getText().toString().trim();
        String bedsStr = etBeds.getText().toString().trim();
        String washroomsStr = etWashrooms.getText().toString().trim();
        String totalRoomsStr = etTotalRooms.getText().toString().trim();

        // Rating
        String ratingStr = etRating.getText().toString().trim();

        // Amenities
        boolean hasWifi = cbWifi.isChecked();
        boolean hasGaming = cbGaming.isChecked();
        boolean isPopular = cbPopular.isChecked();

        // Validation - Basic Information
        if (TextUtils.isEmpty(name)) {
            etHotelName.setError("Hotel name is required");
            etHotelName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(category)) {
            etCategory.setError("Category is required (e.g., LUXURY, BUDGET)");
            etCategory.requestFocus();
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

        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location description is required");
            etLocation.requestFocus();
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
                            Log.d(TAG, "Cloudinary result data: " + resultData.toString());

                            String imageUrl = (String) resultData.get("url");
                            if (imageUrl != null) {
                                Log.d(TAG, "Image URL from Cloudinary: " + imageUrl);
                                progressDialog.setMessage("Saving hotel details...");

                                // Create images array with the uploaded image
                                List<String> images = new ArrayList<>();
                                images.add(imageUrl);

                                saveHotelToFirestore(name, category, description, city, country, location,
                                        price, beds, washrooms, totalRooms, rating,
                                        wifi, gaming, popular, images);
                            } else {
                                progressDialog.dismiss();
                                Log.e(TAG, "Cloudinary URL is null");
                                Toast.makeText(AdminAddHotel.this,
                                        "Image upload failed: No URL returned", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            progressDialog.dismiss();
                            Log.e(TAG, "Cloudinary upload error: " + error.getDescription() + ", Code: " + error.getCode());
                            Toast.makeText(AdminAddHotel.this,
                                    "Image upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            progressDialog.dismiss();
                            Log.e(TAG, "Cloudinary upload rescheduled: " + error.getDescription());
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
                                      boolean popular, List<String> images) {
        String hotelId = db.collection("hotels").document().getId();

        Map<String, Object> hotel = new HashMap<>();
        // Use field names that match the Hotel class exactly
        hotel.put("id", hotelId);
        hotel.put("name", name);
        hotel.put("category", category);
        hotel.put("description", description);
        hotel.put("city", city);
        hotel.put("country", country);
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

        // Timestamp (optional - you can remove if not needed)
        hotel.put("createdAt", FieldValue.serverTimestamp());

        Log.d(TAG, "Saving hotel to Firestore: " + hotel.toString());

        db.collection("hotels")
                .document(hotelId)
                .set(hotel)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Hotel saved successfully to Firestore: " + hotelId);
                        Toast.makeText(AdminAddHotel.this,
                                "Hotel added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.e(TAG, "Firestore save failed: " + task.getException().getMessage());
                        Toast.makeText(AdminAddHotel.this,
                                "Failed to add hotel: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void clearForm() {
        // Clear all fields
        etHotelName.setText("");
        etCategory.setText("");
        etDescription.setText("");
        etCity.setText("");
        etCountry.setText("");
        etLocation.setText("");
        etPrice.setText("");
        etBeds.setText("");
        etWashrooms.setText("");
        etTotalRooms.setText("");
        etRating.setText("");

        // Uncheck checkboxes
        cbWifi.setChecked(false);
        cbGaming.setChecked(false);
        cbPopular.setChecked(false);

        // Reset image
        ivHotelImage.setImageResource(R.drawable.hotel_image_placeholder);
        isImageSelected = false;
        imageUri = null;

        Log.d(TAG, "Form cleared");
    }
}