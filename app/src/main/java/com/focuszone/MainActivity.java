package com.focuszone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.focuszone.databinding.ActivityMainBinding;
import com.focuszone.service.TimerService;
import com.focuszone.utils.Constants;
import com.google.android.material.color.DynamicColors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupPermissionLauncher();
        setupNavigation();
        requestNotificationPermissionIfNeeded();
        startTimerServiceForStateRestore();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void setupPermissionLauncher() {
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (!granted) {
                        binding.bottomNav.announceForAccessibility(
                                getString(R.string.notification_permission_denied)
                        );
                    }
                }
        );
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment is missing from activity_main.xml");
        }

        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void startTimerServiceForStateRestore() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.setAction(Constants.ACTION_RESTORE_TIMER);
        startService(serviceIntent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null || navController == null) {
            return;
        }

        String action = intent.getAction();
        Uri data = intent.getData();
        boolean openTimer = Constants.ACTION_OPEN_TIMER.equals(action)
                || (Intent.ACTION_VIEW.equals(action)
                && data != null
                && "focuszone".equals(data.getScheme())
                && "timer".equals(data.getHost()));

        if (openTimer && navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() != R.id.timerFragment) {
            navController.navigate(R.id.timerFragment);
        }

        intent.setAction(null);
        intent.setData(null);
    }

    @NonNull
    public NavController getNavController() {
        if (navController == null) {
            throw new IllegalStateException("NavController is not initialized yet.");
        }
        return navController;
    }
}
