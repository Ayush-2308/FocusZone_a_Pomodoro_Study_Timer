package com.focuszone.ui.history;

import android.app.Application;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.focuszone.data.db.SessionEntity;
import com.focuszone.data.model.AllTimeStats;
import com.focuszone.data.repository.SessionRepository;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HistoryViewModel extends AndroidViewModel {

    public enum Filter {
        ALL,
        FOCUS,
        BREAKS
    }

    private final SessionRepository repository;
    private final MediatorLiveData<List<Object>> groupedSessions = new MediatorLiveData<>();
    private final MutableLiveData<BarData> weeklyBarData = new MutableLiveData<>();
    private final MutableLiveData<AllTimeStats> allTimeStats = new MutableLiveData<>(AllTimeStats.empty());

    private LiveData<List<SessionEntity>> activeSource;
    private Filter currentFilter = Filter.ALL;
    private SessionEntity lastDeletedSession;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = SessionRepository.getInstance(application);
        groupedSessions.setValue(new ArrayList<>());
        setFilter(Filter.ALL);
        refresh();
    }

    public LiveData<List<Object>> getGroupedSessions() {
        return groupedSessions;
    }

    public LiveData<BarData> getWeeklyBarData() {
        return weeklyBarData;
    }

    public LiveData<AllTimeStats> getAllTimeStats() {
        return allTimeStats;
    }

    public void setFilter(Filter filter) {
        if (filter == null) {
            filter = Filter.ALL;
        }
        currentFilter = filter;
        if (activeSource != null) {
            groupedSessions.removeSource(activeSource);
        }

        if (filter == Filter.FOCUS) {
            activeSource = repository.getFocusSessions();
        } else if (filter == Filter.BREAKS) {
            activeSource = repository.getBreakSessions();
        } else {
            activeSource = repository.getAllSessions();
        }

        groupedSessions.addSource(activeSource, sessions -> groupedSessions.setValue(toGroupedRows(sessions)));
    }

    public void refresh() {
        repository.getFocusCountsForLastSevenDays(data -> weeklyBarData.postValue(toBarData(data)));
        repository.getAllTimeStats(allTimeStats::postValue);
    }

    public void deleteSession(SessionEntity session) {
        if (session == null) {
            return;
        }
        lastDeletedSession = session;
        repository.deleteSession(session);
        refresh();
    }

    public void undoDelete() {
        if (lastDeletedSession == null) {
            return;
        }
        repository.insertSession(lastDeletedSession, inserted -> refresh());
        lastDeletedSession = null;
    }

    public Filter getCurrentFilter() {
        return currentFilter;
    }

    private List<Object> toGroupedRows(List<SessionEntity> sessions) {
        List<Object> rows = new ArrayList<>();
        if (sessions == null || sessions.isEmpty()) {
            return rows;
        }

        String lastHeader = null;
        for (SessionEntity session : sessions) {
            String header = com.focuszone.utils.TimeUtils.formatRelativeDateHeader(session.getCompletedAt());
            if (!header.equals(lastHeader)) {
                rows.add(new DateHeader(header));
                lastHeader = header;
            }
            rows.add(session);
        }
        return rows;
    }

    private BarData toBarData(LinkedHashMap<String, Integer> values) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet set = new BarDataSet(entries, "Sessions");
        set.setColor(Color.rgb(255, 107, 53));
        set.setValueTextColor(Color.rgb(238, 238, 238));
        set.setValueTextSize(10f);
        set.setDrawIcons(false);
        set.setHighLightAlpha(0);

        BarData data = new BarData(set);
        data.setBarWidth(0.55f);
        data.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(Math.round(value));
            }
        });
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(10f);
        data.setDrawValues(true);
        data.setHighlightEnabled(false);

        data.setValueFormatter(new IndexSafeValueFormatter());
        data.setValueTextColor(Color.WHITE);

        return new WeeklyBarData(data, labels);
    }

    public static class DateHeader {
        private final String label;

        public DateHeader(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private static class IndexSafeValueFormatter extends com.github.mikephil.charting.formatter.ValueFormatter {
        @Override
        public String getBarLabel(BarEntry barEntry) {
            return String.valueOf(Math.round(barEntry.getY()));
        }
    }

    public static class WeeklyBarData extends BarData {
        private final List<String> labels;

        WeeklyBarData(BarData barData, List<String> labels) {
            super(barData.getDataSets());
            this.labels = labels;
            setBarWidth(barData.getBarWidth());
            setValueFormatter(new IndexSafeValueFormatter());
            setHighlightEnabled(false);
            setValueTextColor(Color.WHITE);
            setValueTextSize(10f);
            setDrawValues(true);
        }

        public List<String> getLabels() {
            return labels;
        }
    }
}
