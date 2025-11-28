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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.pelagiahotelapp.Adapters.SelectedImagesAdapter;
import com.example.pelagiahotelapp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminAddHotel extends AppCompatActivity {

    // UI Components
    private EditText etHotelName, etCity, etCountry,
            etPrice, etBeds, etWashrooms, etTotalRooms, etRating, etDescription;
    private AutoCompleteTextView etCategory;
    private MaterialSwitch switchWifi, switchGaming, switchPopular;
    private Button btnAddHotel;
    private MaterialCardView cardImageSelect;
    private RecyclerView rvSelectedImages;
    private ImageView ivHotelImage;

    // Multiple images
    private List<Uri> selectedImageUris = new ArrayList<>();
    private SelectedImagesAdapter imagesAdapter;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private static final String TAG = "AdminAddHotel";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_hotel);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize views
        initViews();
        setupCategoryDropdown();
        setupImagesRecyclerView();
        setupClickListeners();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleImageSelection(result.getData());
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
        etRating = findViewById(R.id.etRating);

        // Amenities Switches
        switchWifi = findViewById(R.id.switchWifi);
        switchGaming = findViewById(R.id.switchGaming);
        switchPopular = findViewById(R.id.switchPopular);

        // Image Selection
        cardImageSelect = findViewById(R.id.cardImageSelect);
        btnAddHotel = findViewById(R.id.btnAddHotel);
        ivHotelImage = findViewById(R.id.ivHotelImage);
        rvSelectedImages = findViewById(R.id.rvSelectedImages); // Add this RecyclerView to your XML

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);
    }

    private void setupImagesRecyclerView() {
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesAdapter = new SelectedImagesAdapter(selectedImageUris, new SelectedImagesAdapter.OnImageRemoveListener() {
            @Override
            public void onImageRemove(int position) {
                selectedImageUris.remove(position);
                imagesAdapter.notifyDataSetChanged();
                updateImageSelectionUI();
            }
        });
        rvSelectedImages.setAdapter(imagesAdapter);
    }

    private void setupClickListeners() {
        cardImageSelect.setOnClickListener(v -> openImagePicker());
        btnAddHotel.setOnClickListener(v -> validateAndAddHotel());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Hotel Images"));
    }

    private void handleImageSelection(Intent data) {
        if (data.getClipData() != null) {
            // Multiple images selected
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                selectedImageUris.add(imageUri);
            }
        } else if (data.getData() != null) {
            // Single image selected
            Uri imageUri = data.getData();
            selectedImageUris.add(imageUri);
        }

        imagesAdapter.notifyDataSetChanged();
        updateImageSelectionUI();
        Log.d(TAG, "Images selected: " + selectedImageUris.size());
    }

    private void updateImageSelectionUI() {
        if (selectedImageUris.isEmpty()) {
            ivHotelImage.setVisibility(View.VISIBLE);
            rvSelectedImages.setVisibility(View.GONE);
        } else {
            ivHotelImage.setVisibility(View.GONE);
            rvSelectedImages.setVisibility(View.VISIBLE);
        }
    }

    private void validateAndAddHotel() {
        // Check if user is logged in
        if (currentUser == null) {
            Toast.makeText(this, "Please login to add hotel", Toast.LENGTH_SHORT).show();
            return;
        }

        // Basic Information
        String name = etHotelName.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Location Details
        String city = etCity.getText().toString().trim();
        String country = etCountry.getText().toString().trim();
        String location = city + ", " + country;

        // Pricing & Capacity
        String priceStr = etPrice.getText().toString().trim();
        String bedsStr = etBeds.getText().toString().trim();
        String washroomsStr = etWashrooms.getText().toString().trim();
        String totalRoomsStr = etTotalRooms.getText().toString().trim();
        String ratingStr = etRating.getText().toString().trim();

        // Amenities
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

        // Validation - Pricing & Capacity
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

        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "Please select at least one hotel image", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Starting hotel addition process");
        addHotelWithImages(name, category, description, city, country, location, price, beds, washrooms,
                totalRooms, rating, hasWifi, hasGaming, isPopular);
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

        etCategory.setOnClickListener(v -> etCategory.showDropDown());
        etCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etCategory.showDropDown();
        });
    }

    private void addHotelWithImages(String name, String category, String description, String city, String country,
                                    String location, double price, int beds, int washrooms,
                                    int totalRooms, double rating, boolean wifi, boolean gaming,
                                    boolean popular) {
        progressDialog.setMessage("Uploading images...");
        progressDialog.show();

        List<String> imageUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);
        int totalImages = selectedImageUris.size();

        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri imageUri = selectedImageUris.get(i);
            final int currentIndex = i;

            try {
                MediaManager.get().upload(imageUri)
                        .option("folder", "hotels")
                        .option("public_id", "hotel_" + System.currentTimeMillis() + "_" + currentIndex)
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {
                                Log.d(TAG, "Cloudinary upload started for image " + (currentIndex + 1));
                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {
                                int progress = (int) ((bytes * 100) / totalBytes);
                                int currentUpload = uploadCount.get() + 1;
                                progressDialog.setMessage("Uploading image " + currentUpload + " of " + totalImages + "... " + progress + "%");
                            }

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                Log.d(TAG, "Cloudinary upload successful for image " + (currentIndex + 1));

                                String imageUrl = (String) resultData.get("url");
                                if (imageUrl != null) {
                                    if (imageUrl.startsWith("http://")) {
                                        imageUrl = imageUrl.replace("http://", "https://");
                                    }
                                    imageUrls.add(imageUrl);
                                    Log.d(TAG, "Image URL added: " + imageUrl);
                                }

                                int completed = uploadCount.incrementAndGet();
                                if (completed == totalImages) {
                                    // All images uploaded
                                    runOnUiThread(() -> {
                                        progressDialog.setMessage("Saving hotel details...");
                                        saveHotelToFirestore(name, category, description, city, country, location,
                                                price, beds, washrooms, totalRooms, rating,
                                                wifi, gaming, popular, imageUrls);
                                    });
                                }
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                progressDialog.dismiss();
                                Log.e(TAG, "Cloudinary error for image " + (currentIndex + 1) + ": " + error.getDescription());
                                runOnUiThread(() -> Toast.makeText(AdminAddHotel.this,
                                        "Image upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show());
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                                progressDialog.dismiss();
                                runOnUiThread(() -> Toast.makeText(AdminAddHotel.this,
                                        "Image upload failed, please try again", Toast.LENGTH_SHORT).show());
                            }
                        })
                        .dispatch();

            } catch (Exception e) {
                progressDialog.dismiss();
                Log.e(TAG, "Exception during Cloudinary upload for image " + (currentIndex + 1) + ": " + e.getMessage(), e);
                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private void saveHotelToFirestore(String name, String category, String description, String city, String country,
                                      String location, double price, int beds, int washrooms,
                                      int totalRooms, double rating, boolean wifi, boolean gaming,
                                      boolean popular, List<String> imageUrls) {

        String hotelId = db.collection("hotels").document().getId();
        String ownerId = currentUser.getUid(); // Get current user ID

        Map<String, Object> hotel = new HashMap<>();
        hotel.put("id", hotelId);
        hotel.put("ownerId", ownerId); // Store owner ID
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
        hotel.put("images", imageUrls); // Store as array of strings
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