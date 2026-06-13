package com.example.ai_study_assistant_android.ui.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.review.FailedQuizEntry;
import com.example.ai_study_assistant_android.review.SummaryReviewEntry;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ReviewListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 1;
    private static final int TYPE_QUIZ = 2;
    private static final int TYPE_QUIZ_EMPTY = 3;
    private static final int TYPE_SUMMARY = 4;
    private static final int TYPE_SUMMARY_EMPTY = 5;

    public interface Listener {
        void onOpenQuiz(FailedQuizEntry entry);

        void onOpenSummary(SummaryReviewEntry entry);

        void onRemoveSummary(SummaryReviewEntry entry);
    }

    private final List<Object> rows = new ArrayList<>();
    private final Listener listener;

    public ReviewListAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<FailedQuizEntry> failed, List<SummaryReviewEntry> summaries) {
        rows.clear();
        rows.add(R.string.review_section_quizzes);
        if (failed.isEmpty()) {
            rows.add(new EmptyMarker(R.string.review_quiz_empty));
        } else {
            for (FailedQuizEntry e : failed) {
                rows.add(e);
            }
        }
        rows.add(R.string.review_section_summaries);
        if (summaries.isEmpty()) {
            rows.add(new EmptyMarker(R.string.review_summary_empty));
        } else {
            for (SummaryReviewEntry e : summaries) {
                rows.add(e);
            }
        }
        notifyDataSetChanged();
    }

    private static final class EmptyMarker {
        final int messageRes;

        EmptyMarker(int messageRes) {
            this.messageRes = messageRes;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object o = rows.get(position);
        if (o instanceof Integer) {
            return TYPE_HEADER;
        }
        if (o instanceof FailedQuizEntry) {
            return TYPE_QUIZ;
        }
        if (o instanceof EmptyMarker) {
            Object prev = position > 0 ? rows.get(position - 1) : null;
            if (prev instanceof Integer) {
                int title = (Integer) prev;
                if (title == R.string.review_section_quizzes) {
                    return TYPE_QUIZ_EMPTY;
                }
            }
            return TYPE_SUMMARY_EMPTY;
        }
        if (o instanceof SummaryReviewEntry) {
            return TYPE_SUMMARY;
        }
        return TYPE_QUIZ_EMPTY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_review_header, parent, false);
            return new HeaderVH(v);
        }
        if (viewType == TYPE_QUIZ) {
            View v = inf.inflate(R.layout.item_review_quiz, parent, false);
            return new QuizVH(v);
        }
        if (viewType == TYPE_QUIZ_EMPTY || viewType == TYPE_SUMMARY_EMPTY) {
            View v = inf.inflate(R.layout.item_review_empty, parent, false);
            return new EmptyVH(v);
        }
        View v = inf.inflate(R.layout.item_review_summary, parent, false);
        return new SummaryVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object o = rows.get(position);
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).title.setText((Integer) o);
        } else if (holder instanceof QuizVH) {
            FailedQuizEntry e = (FailedQuizEntry) o;
            QuizVH q = (QuizVH) holder;
            String subj = e.subject != null && !e.subject.isEmpty() ? e.subject : q.itemView.getContext().getString(R.string.review_untitled);
            q.tvTitle.setText(subj);
            q.tvDetail.setText(q.itemView.getContext().getString(
                    R.string.review_quiz_subtitle_format, e.lastScore, e.lastTotal));
            q.itemView.setOnClickListener(v -> listener.onOpenQuiz(e));
        } else if (holder instanceof EmptyVH) {
            EmptyMarker m = (EmptyMarker) o;
            ((EmptyVH) holder).text.setText(m.messageRes);
        } else if (holder instanceof SummaryVH) {
            SummaryReviewEntry e = (SummaryReviewEntry) o;
            SummaryVH s = (SummaryVH) holder;
            String subj = e.subject != null && !e.subject.isEmpty() ? e.subject : s.itemView.getContext().getString(R.string.review_untitled);
            s.tvTitle.setText(subj);
            s.btnRemove.setOnClickListener(v -> listener.onRemoveSummary(e));
            s.itemView.setOnClickListener(v -> listener.onOpenSummary(e));
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        final TextView title;

        HeaderVH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_section_title);
        }
    }

    static class QuizVH extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvDetail;

        QuizVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDetail = itemView.findViewById(R.id.tv_detail);
        }
    }

    static class EmptyVH extends RecyclerView.ViewHolder {
        final TextView text;

        EmptyVH(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_empty);
        }
    }

    static class SummaryVH extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final MaterialButton btnRemove;

        SummaryVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
