package com.focuszone.ui.settings;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.focuszone.BuildConfig;
import com.focuszone.R;
import com.focuszone.databinding.FragmentSettingsBinding;
import com.focuszone.utils.Event;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private boolean bindingValues;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        setupSoundSpinner();
        setupListeners();
        observeViewModel();
        binding.versionText.setText(getString(R.string.version_format, BuildConfig.VERSION_NAME));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupSoundSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sound_names,
                R.layout.item_spinner
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.soundSpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        bindSeekBar(binding.focusSeek, 5, 5, value -> viewModel.setFocusMinutes(value));
        bindSeekBar(binding.shortBreakSeek, 1, 1, value -> viewModel.setShortBreakMinutes(value));
        bindSeekBar(binding.longBreakSeek, 5, 5, value -> viewModel.setLongBreakMinutes(value));
        bindSeekBar(binding.volumeSeek, 0, 1, value -> viewModel.setVolumePercent(value));

        binding.intervalMinus.setOnClickListener(v -> viewModel.setLongBreakInterval(
                Math.max(2, viewModel.getLongBreakInterval().getValue() - 1)
        ));
        binding.intervalPlus.setOnClickListener(v -> viewModel.setLongBreakInterval(
                Math.min(6, viewModel.getLongBreakInterval().getValue() + 1)
        ));

        binding.notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingValues) viewModel.setSessionEndNotificationEnabled(isChecked);
        });
        binding.breakReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingValues) viewModel.setBreakReminderEnabled(isChecked);
        });
        binding.dailyReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingValues) viewModel.setDailyReminderEnabled(isChecked);
        });
        binding.vibrateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingValues) viewModel.setVibrationEnabled(isChecked);
        });
        binding.soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingValues) viewModel.setSoundEnabled(isChecked);
        });
        binding.keepScreenOnSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingValues) viewModel.setKeepScreenOnEnabled(isChecked);
        });
        binding.quotesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingValues) viewModel.setQuotesEnabled(isChecked);
        });
        binding.counterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingValues) viewModel.setSessionCounterEnabled(isChecked);
        });

        binding.dailyReminderTime.setOnClickListener(v -> showTimePicker());
        binding.exportButton.setOnClickListener(v -> viewModel.exportHistory(requireContext()));
        binding.clearHistoryButton.setOnClickListener(v -> showClearHistoryConfirmation());
        binding.privacyButton.setOnClickListener(v -> openUrl(getString(R.string.privacy_policy_url)));
        binding.rateButton.setOnClickListener(v -> openPlayStore());

        binding.soundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!bindingValues) {
                    viewModel.setSelectedSound(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void observeViewModel() {
        viewModel.getFocusMinutes().observe(getViewLifecycleOwner(), value -> {
            binding.focusSeek.setProgress((value - 5) / 5);
            binding.focusValue.setText(getString(R.string.minutes_value, value));
        });
        viewModel.getShortBreakMinutes().observe(getViewLifecycleOwner(), value -> {
            binding.shortBreakSeek.setProgress(value - 1);
            binding.shortBreakValue.setText(getString(R.string.minutes_value, value));
        });
        viewModel.getLongBreakMinutes().observe(getViewLifecycleOwner(), value -> {
            binding.longBreakSeek.setProgress((value - 5) / 5);
            binding.longBreakValue.setText(getString(R.string.minutes_value, value));
        });
        viewModel.getLongBreakInterval().observe(getViewLifecycleOwner(), value ->
                binding.intervalValue.setText(String.valueOf(value))
        );
        viewModel.getVolumePercent().observe(getViewLifecycleOwner(), value -> {
            binding.volumeSeek.setProgress(value);
            binding.volumeValue.setText(getString(R.string.percent_value, value));
        });
        viewModel.getDailyReminderTime().observe(getViewLifecycleOwner(), value ->
                binding.dailyReminderTime.setText(value)
        );
        viewModel.getTotalStoredSessions().observe(getViewLifecycleOwner(), value ->
                binding.totalDataText.setText(getString(R.string.total_data_format, value))
        );
        viewModel.getExportedFile().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                File file = event.getContentIfNotHandled();
                if (file != null) {
                    showExportResult(file);
                }
            }
        });

        viewModel.getSessionEndNotificationEnabled().observe(getViewLifecycleOwner(), value -> setSwitchSilently(() -> binding.notificationSwitch.setChecked(value)));
        viewModel.getBreakReminderEnabled().observe(getViewLifecycleOwner(), value -> setSwitchSilently(() -> binding.breakReminderSwitch.setChecked(value)));
        viewModel.getDailyReminderEnabled().observe(getViewLifecycleOwner(), value -> setSwitchSilently(() -> binding.dailyReminderSwitch.setChecked(value)));
        viewModel.getVibrationEnabled().observe(getViewLifecycleOwner(), value -> setSwitchSilently(() -> binding.vibrateSwitch.setChecked(value)));
        viewModel.getSoundEnabled().observe(getViewLifecycleOwner(), value -> setSwitchSilently(() -> binding.soundSwitch.setChecked(value)));
        viewModel.getKeepScreenOnEnabled().observe(getViewLifecycleOwner(), value -> setSwitchSilently(() -> binding.keepScreenOnSwitch.setChecked(value)));
        viewModel.getQuotesEnabled().observe(getViewLifecycleOwner(), value -> setSwitchSilently(() -> binding.quotesSwitch.setChecked(value)));
        viewModel.getSessionCounterEnabled().observe(getViewLifecycleOwner(), value -> setSwitchSilently(() -> binding.counterSwitch.setChecked(value)));
        viewModel.getSelectedSound().observe(getViewLifecycleOwner(), value -> setSwitchSilently(() -> binding.soundSpinner.setSelection(value)));
    }

    private void bindSeekBar(SeekBar seekBar, int minValue, int step, ValueSetter setter) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setter.set(minValue + progress * step);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void showTimePicker() {
        int hour = viewModel.getDailyReminderHour().getValue();
        int minute = viewModel.getDailyReminderMinute().getValue();
        new TimePickerDialog(
                requireContext(),
                R.style.ThemeOverlay_FocusZone_Dialog,
                (view, selectedHour, selectedMinute) -> viewModel.setDailyReminderTime(selectedHour, selectedMinute),
                hour,
                minute,
                false
        ).show();
    }

    private void showClearHistoryConfirmation() {
        new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_FocusZone_Dialog)
                .setTitle(R.string.clear_history)
                .setMessage(R.string.clear_history_message)
                .setPositiveButton(R.string.clear, (dialog, which) -> viewModel.clearHistory())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showExportResult(File file) {
        if (file == null) {
            Snackbar.make(binding.getRoot(), R.string.export_failed, Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(
                    binding.getRoot(),
                    String.format(Locale.US, "CSV exported: %s", file.getName()),
                    Snackbar.LENGTH_LONG
            ).show();
        }
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void openPlayStore() {
        String packageName = requireContext().getPackageName().replace(".debug", "");
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
        if (marketIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(marketIntent);
        } else {
            openUrl("https://play.google.com/store/apps/details?id=" + packageName);
        }
    }

    private void setSwitchSilently(Runnable runnable) {
        bindingValues = true;
        runnable.run();
        bindingValues = false;
    }

    private interface ValueSetter {
        void set(int value);
    }
}
