package com.focuszone.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.focuszone.R;
import com.focuszone.data.db.SessionEntity;
import com.focuszone.data.model.AllTimeStats;
import com.focuszone.databinding.FragmentHistoryBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private SessionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        setupRecyclerView();
        setupTabs();
        setupChart();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.historyRecycler.setAdapter(null);
        binding = null;
    }

    private void setupRecyclerView() {
        adapter = new SessionAdapter();
        binding.historyRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.historyRecycler.setAdapter(adapter);
        binding.historyRecycler.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getItemViewType() == SessionAdapter.TYPE_HEADER) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Object item = adapter.getItem(viewHolder.getBindingAdapterPosition());
                if (item instanceof SessionEntity) {
                    viewModel.deleteSession((SessionEntity) item);
                    Snackbar.make(binding.getRoot(), R.string.session_deleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, v -> viewModel.undoDelete())
                            .show();
                }
            }
        });
        helper.attachToRecyclerView(binding.historyRecycler);
    }

    private void setupTabs() {
        binding.filterTabs.addTab(binding.filterTabs.newTab().setText(R.string.filter_all));
        binding.filterTabs.addTab(binding.filterTabs.newTab().setText(R.string.filter_focus));
        binding.filterTabs.addTab(binding.filterTabs.newTab().setText(R.string.filter_breaks));
        binding.filterTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    viewModel.setFilter(HistoryViewModel.Filter.FOCUS);
                } else if (tab.getPosition() == 2) {
                    viewModel.setFilter(HistoryViewModel.Filter.BREAKS);
                } else {
                    viewModel.setFilter(HistoryViewModel.Filter.ALL);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupChart() {
        binding.weeklyChart.setNoDataText(getString(R.string.no_chart_data));
        binding.weeklyChart.setDrawGridBackground(false);
        binding.weeklyChart.setDrawBarShadow(false);
        binding.weeklyChart.getDescription().setEnabled(false);
        binding.weeklyChart.getLegend().setEnabled(false);
        binding.weeklyChart.setTouchEnabled(false);
        binding.weeklyChart.setExtraOffsets(8f, 8f, 8f, 8f);

        XAxis xAxis = binding.weeklyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.rgb(170, 170, 170));
        xAxis.setGranularity(1f);

        YAxis leftAxis = binding.weeklyChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.rgb(170, 170, 170));

        binding.weeklyChart.getAxisRight().setEnabled(false);
    }

    private void observeViewModel() {
        viewModel.getGroupedSessions().observe(getViewLifecycleOwner(), items -> {
            adapter.submitItems(items);
            boolean empty = items == null || items.isEmpty();
            binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.historyRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
            if (!empty) {
                binding.historyRecycler.setAlpha(0f);
                binding.historyRecycler.setTranslationY(32f);
                binding.historyRecycler.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(240L)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        });

        viewModel.getWeeklyBarData().observe(getViewLifecycleOwner(), this::renderChart);
        viewModel.getAllTimeStats().observe(getViewLifecycleOwner(), this::renderStats);
    }

    private void renderChart(BarData data) {
        binding.weeklyChart.setData(data);
        if (data instanceof HistoryViewModel.WeeklyBarData) {
            binding.weeklyChart.getXAxis().setValueFormatter(
                    new IndexAxisValueFormatter(((HistoryViewModel.WeeklyBarData) data).getLabels())
            );
        }
        float max = data == null || data.getYMax() <= 0f ? 1f : data.getYMax() + 1f;
        binding.weeklyChart.getAxisLeft().setAxisMaximum(max);
        binding.weeklyChart.animateY(800);
        binding.weeklyChart.invalidate();
    }

    private void renderStats(AllTimeStats stats) {
        if (stats == null) {
            stats = AllTimeStats.empty();
        }
        binding.totalSessionsValue.setText(String.valueOf(stats.getTotalSessions()));
        binding.totalHoursValue.setText(String.format(java.util.Locale.US, "%.1f", stats.getTotalFocusHours()));
        binding.longestStreakValue.setText(String.valueOf(stats.getLongestStreak()));
    }
}
