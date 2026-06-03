package com.focuszone.ui.stats;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.focuszone.data.model.AllTimeStats;
import com.focuszone.databinding.FragmentStatsBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.Locale;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        setupWeeklyChart();
        setupPieChart();
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
        binding = null;
    }

    private void setupWeeklyChart() {
        binding.weeklyMinutesChart.getDescription().setEnabled(false);
        binding.weeklyMinutesChart.getLegend().setEnabled(false);
        binding.weeklyMinutesChart.setDrawGridBackground(false);
        binding.weeklyMinutesChart.setDrawBarShadow(false);
        binding.weeklyMinutesChart.setTouchEnabled(false);

        XAxis xAxis = binding.weeklyMinutesChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.rgb(170, 170, 170));
        xAxis.setGranularity(1f);

        YAxis leftAxis = binding.weeklyMinutesChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.rgb(170, 170, 170));
        binding.weeklyMinutesChart.getAxisRight().setEnabled(false);
    }

    private void setupPieChart() {
        binding.focusBreakPie.getDescription().setEnabled(false);
        binding.focusBreakPie.getLegend().setEnabled(false);
        binding.focusBreakPie.setDrawEntryLabels(false);
        binding.focusBreakPie.setHoleColor(Color.TRANSPARENT);
        binding.focusBreakPie.setTransparentCircleAlpha(0);
        binding.focusBreakPie.setUsePercentValues(true);
        binding.focusBreakPie.setCenterTextColor(Color.WHITE);
        binding.focusBreakPie.setCenterTextSize(18f);
        binding.focusBreakPie.setTouchEnabled(false);
    }

    private void observeViewModel() {
        viewModel.getWeeklyMinutesData().observe(getViewLifecycleOwner(), this::renderWeeklyChart);
        viewModel.getMonthlyHeatmapData().observe(getViewLifecycleOwner(), data -> binding.monthHeatmap.setData(data));
        viewModel.getFocusBreakRatio().observe(getViewLifecycleOwner(), this::renderPieChart);
        viewModel.getAllTimeStats().observe(getViewLifecycleOwner(), this::renderAllTimeStats);
    }

    private void renderWeeklyChart(BarData data) {
        binding.weeklyMinutesChart.setData(data);
        if (data instanceof StatsViewModel.WeeklyMinutesBarData) {
            binding.weeklyMinutesChart.getXAxis().setValueFormatter(
                    new IndexAxisValueFormatter(((StatsViewModel.WeeklyMinutesBarData) data).getLabels())
            );
        }
        binding.weeklyMinutesChart.animateY(800);
        binding.weeklyMinutesChart.invalidate();
    }

    private void renderPieChart(PieData data) {
        binding.focusBreakPie.setData(data);
        AllTimeStats stats = viewModel.getAllTimeStats().getValue();
        int percentage = stats == null ? 0 : stats.getFocusPercentage();
        binding.focusBreakPie.setCenterText(percentage + "% Focus");
        binding.focusBreakPie.animateXY(800, 800);
        binding.focusBreakPie.invalidate();
    }

    private void renderAllTimeStats(AllTimeStats stats) {
        if (stats == null) {
            stats = AllTimeStats.empty();
        }
        binding.bestDayValue.setText(stats.getBestDayLabel());
        binding.bestDaySubtitle.setText(String.format(Locale.US, "%d sessions", stats.getBestDaySessions()));
        binding.averageValue.setText(String.format(Locale.US, "%.1f", stats.getAverageSessionsPerDay()));
        binding.cyclesValue.setText(String.valueOf(stats.getTotalCyclesCompleted()));
        binding.hourValue.setText(stats.getMostProductiveHour());
        binding.focusBreakPie.setCenterText(stats.getFocusPercentage() + "% Focus");
    }
}
