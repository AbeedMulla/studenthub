package com.studenthub.ui.settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.studenthub.R;
import com.studenthub.util.PreferencesManager;

/**
 * Activity for picking home or campus location.
 */
public class LocationPickerActivity extends AppCompatActivity {

    public static final String EXTRA_LOCATION_TYPE = "extra_location_type";

    private MaterialToolbar toolbar;
    private MaterialCardView useCurrentLocationCard;
    private CircularProgressIndicator locationProgress;
    private TextInputEditText latitudeInput, longitudeInput;
    private Slider radiusSlider;
    private TextView radiusValue;
    private MaterialButton saveButton;

    private String locationType;
    private FusedLocationProviderClient fusedLocationClient;
    private PreferencesManager prefs;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        prefs = PreferencesManager.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        locationType = getIntent().getStringExtra(EXTRA_LOCATION_TYPE);
        if (locationType == null) locationType = "home";

        initViews();
        setupToolbar();
        loadExistingLocation();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        useCurrentLocationCard = findViewById(R.id.use_current_location);
        locationProgress = findViewById(R.id.location_progress);
        latitudeInput = findViewById(R.id.latitude_input);
        longitudeInput = findViewById(R.id.longitude_input);
        radiusSlider = findViewById(R.id.radius_slider);
        radiusValue = findViewById(R.id.radius_value);
        saveButton = findViewById(R.id.save_button);
    }

    private void setupToolbar() {
        String title = locationType.equals("home") ? 
                getString(R.string.home_location) : getString(R.string.campus_location);
        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadExistingLocation() {
        double lat, lng;
        float radius;

        if (locationType.equals("home")) {
            lat = prefs.getHomeLatitude();
            lng = prefs.getHomeLongitude();
            radius = prefs.getHomeRadius();
        } else {
            lat = prefs.getCampusLatitude();
            lng = prefs.getCampusLongitude();
            radius = prefs.getCampusRadius();
        }

        if (lat != 0 || lng != 0) {
            latitudeInput.setText(String.valueOf(lat));
            longitudeInput.setText(String.valueOf(lng));
        }

        radiusSlider.setValue(radius);
        updateRadiusDisplay(radius);
    }

    private void setupListeners() {
        useCurrentLocationCard.setOnClickListener(v -> requestLocationPermission());

        radiusSlider.addOnChangeListener((slider, value, fromUser) -> updateRadiusDisplay(value));

        saveButton.setOnClickListener(v -> saveLocation());
    }

    private void updateRadiusDisplay(float radius) {
        if (radius >= 1000) {
            radiusValue.setText(String.format("%.1f kilometers", radius / 1000));
        } else {
            radiusValue.setText(String.format("%.0f meters", radius));
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        locationProgress.setVisibility(View.VISIBLE);

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        locationProgress.setVisibility(View.GONE);
                        if (location != null) {
                            latitudeInput.setText(String.valueOf(location.getLatitude()));
                            longitudeInput.setText(String.valueOf(location.getLongitude()));
                        } else {
                            Toast.makeText(this, "Could not get location. Try again.", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        locationProgress.setVisibility(View.GONE);
                        Toast.makeText(this, "Location error: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            locationProgress.setVisibility(View.GONE);
            Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLocation() {
        String latStr = latitudeInput.getText() != null ? latitudeInput.getText().toString() : "";
        String lngStr = longitudeInput.getText() != null ? longitudeInput.getText().toString() : "";

        if (latStr.isEmpty() || lngStr.isEmpty()) {
            Toast.makeText(this, R.string.required_field, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);
            float radius = radiusSlider.getValue();

            if (locationType.equals("home")) {
                prefs.setHomeLocation(lat, lng, radius);
            } else {
                prefs.setCampusLocation(lat, lng, radius);
            }

            Toast.makeText(this, R.string.location_saved, Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid coordinates", Toast.LENGTH_SHORT).show();
        }
    }
}
