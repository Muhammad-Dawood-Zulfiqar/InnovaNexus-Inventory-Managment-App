package com.example.pelagiahotelapp.HelperActivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pelagiahotelapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPicker extends AppCompatActivity {

    private MapView mapView;
    private IMapController mapController;
    private Marker locationMarker;
    private TextView tvSelectedLocation;
    private EditText etSearchLocation;
    private Button btnSearch, btnDone;

    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private String selectedAddress = "";
    private String selectedCity = "";
    private String selectedCountry = "";

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        // Initialize OSMDroid
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));

        initViews();
        setupMap();
        setupListeners();

        // Get current location or default
        getCurrentLocation();
    }

    private void initViews() {
        mapView = findViewById(R.id.mapView);
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        etSearchLocation = findViewById(R.id.etSearchLocation);
        btnSearch = findViewById(R.id.btnSearch);
        btnDone = findViewById(R.id.btnDone);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        mapController = mapView.getController();
        mapController.setZoom(15.0);

        // Add click listener for map
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                handleMapTap(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapView.getOverlays().add(0, mapEventsOverlay);
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> searchLocation());

        etSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                searchLocation();
                return true;
            }
            return false;
        });

        btnDone.setOnClickListener(v -> returnLocationToCaller());
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                            mapController.setCenter(currentLocation);
                            handleMapTap(currentLocation);
                        } else {
                            setDefaultLocation();
                        }
                    })
                    .addOnFailureListener(e -> {
                        setDefaultLocation();
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            setDefaultLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    private void setDefaultLocation() {
        GeoPoint defaultLocation = new GeoPoint(31.5820459, 74.3293763); // Default location
        mapController.setCenter(defaultLocation);
        handleMapTap(defaultLocation);
    }

    private void handleMapTap(GeoPoint geoPoint) {
        selectedLatitude = geoPoint.getLatitude();
        selectedLongitude = geoPoint.getLongitude();

        // Update marker
        if (locationMarker != null) {
            mapView.getOverlays().remove(locationMarker);
        }

        locationMarker = new Marker(mapView);
        locationMarker.setPosition(geoPoint);
        locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        locationMarker.setTitle("Selected Location");
        mapView.getOverlays().add(locationMarker);

        // Get address from location
        getAddressFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude());

        mapView.invalidate();
    }

    private void getAddressFromLocation(double lat, double lng) {
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);

                    // --- NEW CODE STARTS HERE ---
                    // 1. Get Country
                    selectedCountry = address.getCountryName();

                    // 2. Get City (Locality).
                    // Fallback to SubAdminArea if Locality is null (common in some areas)
                    selectedCity = address.getLocality();
                    if (selectedCity == null) {
                        selectedCity = address.getSubAdminArea();
                    }
                    // --- NEW CODE ENDS HERE ---

                    // Existing Address Building Logic
                    StringBuilder addressBuilder = new StringBuilder();
                    if (address.getThoroughfare() != null) {
                        addressBuilder.append(address.getThoroughfare());
                    }
                    if (address.getLocality() != null) {
                        if (addressBuilder.length() > 0) addressBuilder.append(", ");
                        addressBuilder.append(address.getLocality());
                    }
                    if (addressBuilder.length() == 0) {
                        addressBuilder.append(address.getAddressLine(0));
                    }

                    selectedAddress = addressBuilder.toString();
                    tvSelectedLocation.setText("📍 " + selectedAddress);
                } else {
                    // Reset if not found
                    selectedCity = "";
                    selectedCountry = "";
                    selectedAddress = String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f", lat, lng);
                    tvSelectedLocation.setText("📍 " + selectedAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
                selectedAddress = String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f", lat, lng);
                tvSelectedLocation.setText("📍 " + selectedAddress);
            }
        } else {
            selectedAddress = String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f", lat, lng);
            tvSelectedLocation.setText("📍 " + selectedAddress);
        }
    }
    private void searchLocation() {
        String locationName = etSearchLocation.getText().toString().trim();
        if (locationName.isEmpty()) {
            Toast.makeText(this, "Please enter a location to search", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(locationName, 5);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    GeoPoint geoPoint = new GeoPoint(address.getLatitude(), address.getLongitude());

                    mapController.setCenter(geoPoint);
                    handleMapTap(geoPoint);

                    Toast.makeText(this, "Location found: " + address.getLocality(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Location not found. Try being more specific.", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error searching location. Check your connection.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Geocoder not available on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void returnLocationToCaller() {
        if (selectedLatitude == 0.0 && selectedLongitude == 0.0) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", selectedLatitude);
        resultIntent.putExtra("longitude", selectedLongitude);
        resultIntent.putExtra("address", selectedAddress);

        // --- ADDED THESE LINES ---
        resultIntent.putExtra("city", selectedCity != null ? selectedCity : "");
        resultIntent.putExtra("country", selectedCountry != null ? selectedCountry : "");
        // -------------------------

        setResult(RESULT_OK, resultIntent);
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}