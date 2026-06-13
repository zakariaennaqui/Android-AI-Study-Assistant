package com.example.ai_study_assistant_android.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.HistoryItem;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);
    }

    private List<HistoryItem> items;
    private final OnItemClickListener listener;

    public HistoryAdapter(List<HistoryItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<HistoryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public HistoryItem getItem(int position) {
        return items.get(position);
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSubject, tvTypeLabel, tvDate;

        ViewHolder(View view) {
            super(view);
            tvSubject = view.findViewById(R.id.tv_subject);
            tvTypeLabel = view.findViewById(R.id.tv_type_label);
            tvDate = view.findViewById(R.id.tv_date);
        }

        void bind(HistoryItem item, OnItemClickListener listener) {
            String type = item.getType() != null ? item.getType() : "SUMMARY";

            int labelColor = itemView.getContext().getColor(R.color.color_on_surface_muted);
            tvTypeLabel.setText(type);
            tvTypeLabel.setTextColor(labelColor);

            String subject = item.getSubject();
            tvSubject.setText((subject != null && !subject.isEmpty()) ? subject : "General");

            String date = item.getCreatedAt();
            tvDate.setText(date != null && date.length() >= 10 ? date.substring(0, 10) : "");

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
