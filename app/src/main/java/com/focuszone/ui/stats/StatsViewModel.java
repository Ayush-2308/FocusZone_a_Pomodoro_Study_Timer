package com.focuszone.ui.stats;

import android.app.Application;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.focuszone.data.model.AllTimeStats;
import com.focuszone.data.repository.SessionRepository;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatsViewModel extends AndroidViewModel {

    private final SessionRepository repository;
    private final MutableLiveData<BarData> weeklyMinutesData = new MutableLiveData<>();
    private final MutableLiveData<Map<Integer, Integer>> monthlyHeatmapData = new MutableLiveData<>();
    private final MutableLiveData<PieData> focusBreakRatio = new MutableLiveData<>();
    private final MutableLiveData<AllTimeStats> allTimeStats = new MutableLiveData<>(AllTimeStats.empty());

    public StatsViewModel(@NonNull Application application) {
        super(application);
        repository = SessionRepository.getInstance(application);
        refresh();
    }

    public LiveData<BarData> getWeeklyMinutesData() {
        return weeklyMinutesData;
    }

    public LiveData<Map<Integer, Integer>> getMonthlyHeatmapData() {
        return monthlyHeatmapData;
    }

    public LiveData<PieData> getFocusBreakRatio() {
        return focusBreakRatio;
    }

    public MutableLiveData<AllTimeStats> getAllTimeStats() {
        return allTimeStats;
    }

    public void refresh() {
        repository.getFocusMinutesForCurrentWeek(data -> weeklyMinutesData.postValue(toWeeklyMinutesBarData(data)));
        repository.getMonthlyHeatmapData(monthlyHeatmapData::postValue);
        repository.getAllTimeStats(stats -> {
            allTimeStats.postValue(stats);
            focusBreakRatio.postValue(toPieData(stats));
        });
    }

    private BarData toWeeklyMinutesBarData(LinkedHashMap<String, Integer> values) {
        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (todayIndex < 0) {
            todayIndex = 6;
        }

        int index = 0;
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            colors.add(index == todayIndex ? Color.rgb(255, 107, 53) : Color.rgb(85, 85, 85));
            index++;
        }

        BarDataSet set = new BarDataSet(entries, "Focus minutes");
        set.setColors(colors);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(10f);
        set.setDrawIcons(false);
        set.setHighLightAlpha(0);

        BarData data = new BarData(set);
        data.setBarWidth(0.55f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(10f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return String.valueOf(Math.round(barEntry.getY()));
            }
        });
        return new WeeklyMinutesBarData(data, new ArrayList<>(values.keySet()));
    }

    private PieData toPieData(AllTimeStats stats) {
        List<PieEntry> entries = new ArrayList<>();
        int focusSeconds = Math.max(0, stats.getTotalFocusSeconds());
        int breakSeconds = Math.max(0, stats.getTotalBreakSeconds());

        if (focusSeconds == 0 && breakSeconds == 0) {
            entries.add(new PieEntry(1f, "No data"));
        } else {
            entries.add(new PieEntry(focusSeconds, "Focus"));
            entries.add(new PieEntry(Math.max(1, breakSeconds), "Break"));
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(Color.rgb(255, 107, 53), Color.rgb(95, 95, 95));
        set.setDrawIcons(false);
        set.setSliceSpace(2f);
        set.setSelectionShift(0f);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(11f);

        PieData data = new PieData(set);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "";
            }
        });
        return data;
    }

    public static class WeeklyMinutesBarData extends BarData {
        private final List<String> labels;

        WeeklyMinutesBarData(BarData data, List<String> labels) {
            super(data.getDataSets());
            this.labels = labels;
            setBarWidth(data.getBarWidth());
            setValueFormatter(data.getDataSetByIndex(0).getValueFormatter());
            setValueTextColor(Color.WHITE);
            setValueTextSize(10f);
        }

        public List<String> getLabels() {
            return labels;
        }
    }
}
