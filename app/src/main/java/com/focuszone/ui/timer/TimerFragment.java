package com.focuszone.ui.timer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.focuszone.R;
import com.focuszone.data.model.SessionSummary;
import com.focuszone.data.model.TimerState;
import com.focuszone.databinding.FragmentTimerBinding;
import com.focuszone.utils.Event;
import com.google.android.material.snackbar.Snackbar;

public class TimerFragment extends Fragment {

    private FragmentTimerBinding binding;
    private TimerViewModel viewModel;
    private TimerState lastState = TimerState.IDLE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTimerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TimerViewModel.class);

        setupButtons();
        observeViewModel();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.bindTimerService(requireContext());
    }

    @Override
    public void onStop() {
        viewModel.unbindTimerService(requireContext());
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupButtons() {
        bindScaleAnimation(binding.primaryButton);
        bindScaleAnimation(binding.resetButton);
        bindScaleAnimation(binding.skipButton);

        binding.primaryButton.setOnClickListener(v -> {
            TimerState state = viewModel.getState().getValue();
            if (state == TimerState.RUNNING) {
                viewModel.pauseTimer();
            } else if (state == TimerState.PAUSED) {
                viewModel.resumeTimer();
            } else {
                viewModel.startTimer();
            }
        });

        binding.resetButton.setOnClickListener(v -> showResetConfirmation());
        binding.skipButton.setOnClickListener(v -> viewModel.skipSession());
    }

    private void observeViewModel() {
        viewModel.getRemainingSeconds().observe(getViewLifecycleOwner(), seconds -> {
            long total = viewModel.getTotalSeconds().getValue() == null
                    ? seconds
                    : viewModel.getTotalSeconds().getValue();
            binding.circularTimer.setTimer(total, seconds);
        });

        viewModel.getTotalSeconds().observe(getViewLifecycleOwner(), total -> {
            Long remaining = viewModel.getRemainingSeconds().getValue();
            binding.circularTimer.setTimer(total, remaining == null ? total : remaining);
        });

        viewModel.getCurrentMode().observe(getViewLifecycleOwner(), mode -> {
            binding.circularTimer.setMode(mode);
            binding.modeText.setText(mode);
        });

        viewModel.getCurrentSession().observe(getViewLifecycleOwner(), session -> {
            String counter = getString(R.string.session_counter, session, viewModel.getLongBreakInterval());
            binding.circularTimer.setSessionCounter(counter);
        });

        viewModel.getState().observe(getViewLifecycleOwner(), this::renderState);

        viewModel.getTodaySummary().observe(getViewLifecycleOwner(), this::renderSummary);

        viewModel.getQuote().observe(getViewLifecycleOwner(), quote -> {
            binding.quoteText.setText(quote);
            AlphaAnimation animation = new AlphaAnimation(0f, 1f);
            animation.setDuration(300L);
            binding.quoteText.startAnimation(animation);
        });

        viewModel.getShowQuotes().observe(getViewLifecycleOwner(), visible ->
                binding.quoteText.setVisibility(visible ? View.VISIBLE : View.GONE));

        viewModel.getShowCounter().observe(getViewLifecycleOwner(), visible ->
                binding.circularTimer.setShowCounter(visible));

        viewModel.getCompletionEvent().observe(getViewLifecycleOwner(), event -> {
            if (event == null || event.isHandled()) {
                return;
            }
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
                binding.circularTimer.pulse();
            }
        });
    }

    private void renderState(TimerState state) {
        if (state == null) {
            state = TimerState.IDLE;
        }

        if (state == TimerState.RUNNING) {
            binding.primaryButton.setText(R.string.pause);
            binding.primaryButton.setIconResource(R.drawable.ic_pause);
            binding.resetButton.setEnabled(true);
            binding.skipButton.setEnabled(true);
        } else if (state == TimerState.PAUSED) {
            binding.primaryButton.setText(R.string.resume);
            binding.primaryButton.setIconResource(R.drawable.ic_play);
            binding.resetButton.setEnabled(true);
            binding.skipButton.setEnabled(true);
        } else {
            binding.primaryButton.setText(R.string.start);
            binding.primaryButton.setIconResource(R.drawable.ic_play);
            binding.resetButton.setEnabled(false);
            binding.skipButton.setEnabled(false);
        }

        if (lastState == TimerState.RUNNING && state == TimerState.COMPLETED) {
            Snackbar.make(binding.getRoot(), R.string.session_complete_message, Snackbar.LENGTH_LONG).show();
        }
        lastState = state;
    }

    private void renderSummary(SessionSummary summary) {
        if (summary == null) {
            return;
        }
        binding.todaySessionsValue.setText(String.valueOf(summary.getTodaySessions()));
        binding.todayMinutesValue.setText(String.valueOf(summary.getTodayMinutes()));
        binding.currentStreakValue.setText(String.valueOf(summary.getCurrentStreak()));
    }

    private void showResetConfirmation() {
        new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_FocusZone_Dialog)
                .setTitle(R.string.reset_timer_title)
                .setMessage(R.string.reset_timer_message)
                .setPositiveButton(R.string.reset, (dialog, which) -> viewModel.resetTimer())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void bindScaleAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            if (!v.isEnabled()) {
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80L).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(120L).start();
            }
            return false;
        });
    }
}
