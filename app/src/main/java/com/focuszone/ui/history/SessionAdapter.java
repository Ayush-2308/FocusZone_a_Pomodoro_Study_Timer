package com.focuszone.ui.history;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.focuszone.R;
import com.focuszone.data.db.SessionEntity;
import com.focuszone.data.model.SessionType;
import com.focuszone.databinding.ItemDateHeaderBinding;
import com.focuszone.databinding.ItemSessionBinding;
import com.focuszone.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_SESSION = 1;

    private final List<Object> items = new ArrayList<>();

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof HistoryViewModel.DateHeader ? TYPE_HEADER : TYPE_SESSION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(ItemDateHeaderBinding.inflate(inflater, parent, false));
        }
        return new SessionViewHolder(ItemSessionBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((HistoryViewModel.DateHeader) item);
        } else if (holder instanceof SessionViewHolder) {
            ((SessionViewHolder) holder).bind((SessionEntity) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitItems(List<Object> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public Object getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final ItemDateHeaderBinding binding;

        HeaderViewHolder(ItemDateHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(HistoryViewModel.DateHeader header) {
            binding.dateHeaderText.setText(header.getLabel());
        }
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {

        private final ItemSessionBinding binding;

        SessionViewHolder(ItemSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SessionEntity session) {
            SessionType type = SessionType.fromDatabaseValue(session.getType());
            binding.sessionTitle.setText(type.getHistoryLabel());
            binding.sessionDuration.setText(binding.getRoot().getContext().getString(
                    R.string.minutes_short,
                    Math.max(1, session.getDurationSeconds() / 60)
            ));
            binding.sessionTime.setText(TimeUtils.formatTime(session.getCompletedAt()));
            binding.modeIcon.setImageResource(type == SessionType.FOCUS ? R.drawable.ic_focus : R.drawable.ic_break);
            binding.modeIcon.setColorFilter(type.getColor());
            binding.colorStrip.setBackgroundColor(type.getColor());
            binding.sessionCard.setStrokeColor(ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    R.color.surface_stroke
            ));
        }
    }
}
