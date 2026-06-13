package com.example.ai_study_assistant_android.ui.result;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.StudySession;
import com.example.ai_study_assistant_android.review.ReviewLaterStore;
import com.google.android.material.button.MaterialButton;

public class SummaryFragment extends Fragment {

    private static final String ARG_SESSION = "session";

    public static SummaryFragment newInstance(StudySession session) {
        SummaryFragment f = new SummaryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SESSION, session);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StudySession session = getArguments() != null
                ? (StudySession) getArguments().getSerializable(ARG_SESSION) : null;

        TextView tvSummary = view.findViewById(R.id.tv_summary);
        MaterialButton btnShare = view.findViewById(R.id.btn_share);
        MaterialButton btnReviewLater = view.findViewById(R.id.btn_review_later);

        if (session != null && session.getSummary() != null) {
            tvSummary.setText(session.getSummary());
        }

        btnShare.setOnClickListener(v -> {
            if (session == null || session.getSummary() == null) return;
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, session.getSummary());
            share.putExtra(Intent.EXTRA_SUBJECT, "AI Summary");
            startActivity(Intent.createChooser(share, "Share Summary"));
        });

        if (session == null || TextUtils.isEmpty(session.getSessionId())) {
            btnReviewLater.setVisibility(View.GONE);
        } else {
            btnReviewLater.setVisibility(View.VISIBLE);
            ReviewLaterStore store = ReviewLaterStore.getInstance();
            Runnable syncLabel = () -> {
                if (!isAdded()) return;
                if (store.isSummaryMarkedForReview(requireContext(), session.getSessionId())) {
                    btnReviewLater.setText(R.string.summary_remove_from_review);
                } else {
                    btnReviewLater.setText(R.string.summary_review_later);
                }
            };
            syncLabel.run();
            btnReviewLater.setOnClickListener(v -> {
                String id = session.getSessionId();
                if (store.isSummaryMarkedForReview(requireContext(), id)) {
                    store.removeSummaryForReview(requireContext(), id);
                } else {
                    store.addSummaryForReview(requireContext(), id, session.getSubject());
                }
                syncLabel.run();
            });
        }
    }
}
