package com.studenthub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.studenthub.R;
import com.studenthub.data.repository.DataRepository;
import com.studenthub.ui.assignments.AssignmentsFragment;
import com.studenthub.ui.auth.LoginActivity;
import com.studenthub.ui.focus.FocusFragment;
import com.studenthub.ui.home.HomeFragment;
import com.studenthub.ui.schedule.ScheduleFragment;
import com.studenthub.ui.settings.SettingsFragment;
import com.studenthub.util.NetworkUtils;

/**
 * Main activity containing bottom navigation and fragment container.
 * Handles authentication state and network status.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private View offlineBanner;
    private FirebaseAuth auth;
    private NetworkUtils networkUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        networkUtils = NetworkUtils.getInstance(this);

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            navigateToLogin();
            return;
        }

        initViews();
        setupBottomNavigation();
        observeNetworkStatus();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Trigger sync on app start
        triggerSync();
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bottom_navigation);
        offlineBanner = findViewById(R.id.offline_banner);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_schedule) {
                fragment = new ScheduleFragment();
            } else if (itemId == R.id.nav_assignments) {
                fragment = new AssignmentsFragment();
            } else if (itemId == R.id.nav_focus) {
                fragment = new FocusFragment();
            } else if (itemId == R.id.nav_settings) {
                fragment = new SettingsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }

    private void observeNetworkStatus() {
        networkUtils.getConnectionStatus().observe(this, isConnected -> {
            offlineBanner.setVisibility(isConnected ? View.GONE : View.VISIBLE);

            // Trigger sync when connection is restored
            if (isConnected) {
                triggerSync();
            }
        });
    }

    private void triggerSync() {
        DataRepository.getInstance(this).sync(new DataRepository.OnSyncCallback() {
            @Override
            public void onSuccess() {
                // Sync completed silently
            }

            @Override
            public void onError(Exception e) {
                // Handle sync error silently, will retry later
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verify user is still logged in
        if (auth.getCurrentUser() == null) {
            navigateToLogin();
        }
    }

    /**
     * Navigate to a specific tab programmatically.
     */
    public void navigateToTab(int tabId) {
        bottomNav.setSelectedItemId(tabId);
    }
}
